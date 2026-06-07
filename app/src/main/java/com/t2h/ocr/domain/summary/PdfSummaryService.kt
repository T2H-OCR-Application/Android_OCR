package com.t2h.ocr.domain.summary

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.t2h.ocr.data.local.UserPreferences
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class SummaryState {
    object Idle : SummaryState()
    data class Summarizing(val progress: String = "Đang tóm tắt...") : SummaryState()
    data class Done(val summaryText: String, val sourceScanId: String, val detectedLanguage: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}

object PdfSummaryService {

    private const val TAG = "GEMINI_DEBUG"

    private val prompt = """
Bạn là một chuyên gia tóm tắt văn bản. Hãy tóm tắt văn bản sau theo yêu cầu:
- Giữ lại các ý chính và thông tin quan trọng.
- Loại bỏ nội dung lặp lại và các chi tiết không cần thiết.
- Không tự suy diễn hoặc bổ sung thông tin ngoài văn bản gốc.
- Giữ nguyên số liệu, ngày tháng, tên riêng và thuật ngữ quan trọng.
- Trình bày bằng đoạn văn bản ngắn gọn, rõ ràng và dễ hiểu.
- Độ dài bản tóm tắt từ 3 đến 20 dòng, tự điều chỉnh dựa trên độ dài và mức độ phức tạp của văn bản:
  + Văn bản ngắn: khoảng 3-5 dòng.
  + Văn bản trung bình: khoảng 6-12 dòng.
  + Văn bản dài: khoảng 13-20 dòng.
- Chỉ trả về nội dung tóm tắt, không thêm tiêu đề hay chú thích
- Sau nội dung tóm tắt, thêm 1 dòng trống rồi ghi: LANG:[mã_ngôn_ngữ] (ví dụ: LANG:vi, LANG:en, LANG:ja)

Văn bản:
""".trimIndent()

    suspend fun summarize(context: Context, scan: ScanMetadata): SummaryState = withContext(Dispatchers.IO) {
        val ocrText = scan.ocrText.trim()
        if (ocrText.isBlank()) {
            return@withContext SummaryState.Error("Tài liệu này không có nội dung văn bản để tóm tắt.")
        }

        if (!isNetworkAvailable(context)) {
            return@withContext SummaryState.Error("Không có kết nối mạng. Vui lòng kiểm tra Wifi hoặc dữ liệu di động.")
        }

        // Đọc API key từ DataStore
        val apiKey = UserPreferences(context).geminiApiKey.first()

        if (apiKey.isBlank()) {
            return@withContext SummaryState.Error("Chưa có API Key. Vui lòng vào tab Cài đặt → Cấu hình AI để nhập Gemini API Key.")
        }

        val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        Log.d(TAG, "=== BẮT ĐẦU GỌI GEMINI API ===")
        Log.d(TAG, "Key prefix: ${apiKey.take(10)}...")

        try {
            val requestBody = buildRequestBody("$prompt$ocrText")
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 60_000
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(requestBody); it.flush() }

            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")

            val responseStream = if (responseCode == HttpURLConnection.HTTP_OK)
                connection.inputStream else connection.errorStream

            val responseText = BufferedReader(InputStreamReader(responseStream, "UTF-8")).use { it.readText() }
            Log.d(TAG, "Raw response: $responseText")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "LỖI $responseCode: $responseText")
                val hint = if (responseCode == 403 || responseCode == 401)
                    "API Key không hợp lệ. Vui lòng kiểm tra lại trong phần Cài đặt → Cấu hình AI."
                else
                    "Lỗi API ($responseCode): ${parseErrorMessage(responseText)}"
                return@withContext SummaryState.Error(hint)
            }

            val rawText = parseResponseText(responseText)
            if (rawText.isBlank()) {
                return@withContext SummaryState.Error("Gemini trả về kết quả rỗng. Vui lòng thử lại.")
            }

            val langRegex = Regex("\\nLANG:([a-zA-Z\\-]+)\\s*$")
            val langMatch = langRegex.find(rawText)
            val detectedLanguage = langMatch?.groupValues?.get(1) ?: ""
            val summaryText = rawText.replace(langRegex, "").trim()

            Log.d(TAG, "=== THÀNH CÔNG === lang=$detectedLanguage")

            SummaryState.Done(
                summaryText = summaryText,
                sourceScanId = scan.id,
                detectedLanguage = detectedLanguage
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Exception: ${e.javaClass.simpleName} - ${e.message}")
            SummaryState.Error("Không thể kết nối đến Gemini: ${e.message}")
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun buildRequestBody(fullPrompt: String): String {
        val part = JSONObject().put("text", fullPrompt)
        val parts = JSONArray().put(part)
        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)
        return JSONObject().put("contents", contents).toString()
    }

    private fun parseResponseText(responseJson: String): String {
        return try {
            val root = JSONObject(responseJson)
            val candidates = root.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun parseErrorMessage(errorJson: String): String {
        return try {
            JSONObject(errorJson).getJSONObject("error").getString("message")
        } catch (e: Exception) {
            errorJson.take(200)
        }
    }
}
