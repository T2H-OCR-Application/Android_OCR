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

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        val rect = element.boundingBox ?: continue
                        
                        // Adjust text size to match the bounding box height
                        paint.textSize = rect.height().toFloat()
                        
                        // Draw text at the position of the element
                        // Note: drawText uses the baseline, so bottom is a decent approximation
                        canvas.drawText(
                            element.text,
                            rect.left.toFloat(),
                            rect.bottom.toFloat(),
                            paint
                        )
                    }
                }
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }
}
