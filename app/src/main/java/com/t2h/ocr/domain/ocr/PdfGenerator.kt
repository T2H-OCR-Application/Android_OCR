package com.t2h.ocr.domain.ocr

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Color
import com.google.mlkit.vision.text.Text
import java.io.OutputStream

/**
 * Generates a searchable PDF by layering transparent text over an image.
 */
object PdfGenerator {
    fun generateSearchablePdf(
        bitmap: Bitmap,
        visionText: Text,
        editedText: String,
        outputStream: OutputStream
    ) {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // 1. Draw the image as the base layer
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            // 2. Draw invisible text on top for searchability
            val paint = Paint().apply {
                color = Color.TRANSPARENT
                isAntiAlias = true
            }

            // Simple line-matching to attempt fixing CR-01
            val editedLines = editedText.split("\n").filter { it.isNotBlank() }
            val originalLines = visionText.textBlocks.flatMap { it.lines }
            
            val useEdited = editedLines.size == originalLines.size

            var lineIndex = 0
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val textToDraw = if (useEdited && lineIndex < editedLines.size) {
                        editedLines[lineIndex]
                    } else {
                        line.text
                    }
                    lineIndex++

                    val rect = line.boundingBox ?: continue
                    
                    // Adjust text size to match the bounding box height
                    paint.textSize = rect.height().toFloat()
                    
                    // Calculate baseline using FontMetrics for WR-01
                    val metrics = paint.fontMetrics
                    val baseline = rect.bottom.toFloat() - metrics.descent

                    canvas.drawText(
                        textToDraw,
                        rect.left.toFloat(),
                        baseline,
                        paint
                    )
                }
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }
}
