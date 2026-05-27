package com.t2h.ocr.domain.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.google.mlkit.vision.text.Text
import java.io.OutputStream

/**
 * Generates PDFs from OCR results.
 */
object PdfGenerator {

    /**
     * Generates a clean text PDF from multiple pages of text.
     * Uses StaticLayout for proper text wrapping and pagination.
     */
    fun generateTextPdf(
        pages: List<String>,
        outputStream: OutputStream
    ) {
        val pdfDocument = PdfDocument()
        val paint = TextPaint().apply {
            textSize = 12f
            color = Color.BLACK
            isAntiAlias = true
        }
        
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val margin = 50f
        val contentWidth = (pageWidth - 2 * margin).toInt()
        val maxContentHeight = pageHeight - 2 * margin

        try {
            for (text in pages) {
                if (text.isBlank()) continue

                val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .build()

                var currentLine = 0
                val totalLines = staticLayout.lineCount
                
                while (currentLine < totalLines) {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    
                    canvas.translate(margin, margin)
                    
                    val startLine = currentLine
                    var endLine = startLine
                    var currentHeight = 0f
                    
                    while (endLine < totalLines) {
                        val lineHeight = staticLayout.getLineBottom(endLine) - staticLayout.getLineTop(endLine)
                        if (currentHeight + lineHeight > maxContentHeight) {
                            break
                        }
                        currentHeight += lineHeight
                        endLine++
                    }
                    
                    if (endLine > startLine) {
                        canvas.save()
                        val startY = staticLayout.getLineTop(startLine)
                        canvas.translate(0f, -startY.toFloat())
                        
                        canvas.clipRect(0f, startY.toFloat(), contentWidth.toFloat(), staticLayout.getLineBottom(endLine - 1).toFloat())
                        staticLayout.draw(canvas)
                        canvas.restore()
                        
                        currentLine = endLine
                    } else {
                        // Safety break for extremely long single lines (should not happen with normal text)
                        currentLine++
                    }
                    
                    pdfDocument.finishPage(page)
                }
            }
            
            pdfDocument.writeTo(outputStream)
        } finally {
            pdfDocument.close()
        }
    }

    /**
     * @deprecated Use generateTextPdf for multi-page batch support and cleaner text rendering.
     */
    @Deprecated("Use generateTextPdf")
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
                    
                    paint.textSize = rect.height().toFloat()
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
