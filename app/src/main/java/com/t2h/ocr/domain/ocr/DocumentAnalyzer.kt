package com.t2h.ocr.domain.ocr

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.tracing.trace
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*

/**
 * Analyzes camera frames to detect a 4-corner document quadrilateral.
 * Optimized for performance and memory efficiency.
 */
class DocumentAnalyzer(
    private val onDetected: (List<Point>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val TAG = "DEBUG_OCR"
    private val gray = Mat()
    private val blurred = Mat()
    private val thresholded = Mat()
    private val morph = Mat()
    private val hierarchy = Mat()

    /**
     * Releases native resources. Should be called when the analyzer is no longer needed.
     */
    fun release() {
        gray.release()
        blurred.release()
        thresholded.release()
        morph.release()
        hierarchy.release()
    }

    override fun analyze(image: ImageProxy) {
        trace("DocumentAnalyzer#analyze") {
            try {
                analyzeWithoutClosing(image)
            } catch (e: Exception) {
                Log.e(TAG, "DocumentAnalyzer: CRASH", e)
            } finally {
                image.close()
            }
        }
    }

    @Synchronized
    fun analyzeWithoutClosing(image: ImageProxy) {
        val rotation = image.imageInfo.rotationDegrees
        val yPlane = image.planes[0]
        val yBuffer = yPlane.buffer
        val yRowStride = yPlane.rowStride
        val width = image.width
        val height = image.height
        
        val yData = ByteArray(yBuffer.remaining())
        yBuffer.get(yData)
        
        Mat(height, width, CvType.CV_8UC1).use { rawGray ->
            if (yRowStride == width) {
                rawGray.put(0, 0, yData)
            } else {
                // Buffer is padded, copy row by row to align it correctly
                val rowData = ByteArray(width)
                for (row in 0 until height) {
                    System.arraycopy(yData, row * yRowStride, rowData, 0, width)
                    rawGray.put(row, 0, rowData)
                }
            }
            
            if (rotation != 0) {
                Mat().use { rotated ->
                    val rotateCode = when (rotation) {
                        90 -> Core.ROTATE_90_CLOCKWISE
                        180 -> Core.ROTATE_180
                        270 -> Core.ROTATE_90_COUNTERCLOCKWISE
                        else -> null
                    }
                    if (rotateCode != null) {
                        Core.rotate(rawGray, rotated, rotateCode)
                        performDetection(rotated)
                    } else {
                        performDetection(rawGray)
                    }
                }
            } else {
                performDetection(rawGray)
            }
        }
    }

    private fun performDetection(graySource: Mat) {
        val srcW = graySource.width()
        val srcH = graySource.height()
        
        Log.d(TAG, "Analyzer: Delegating detection for image ${srcW}x${srcH}")
        
        val normalizedPoints = DetectionUtils.detectDocument(
            graySource, gray, blurred, thresholded, morph, hierarchy
        )
        onDetected(normalizedPoints, srcW, srcH)
    }
}
