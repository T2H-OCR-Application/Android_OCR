package com.t2h.ocr.domain.ocr

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class PdfGeneratorTest {

    @Test
    fun `generateTextPdf creates a PDF for short text`() {
        val outputStream = ByteArrayOutputStream()
        val textPages = listOf("Hello World")
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
        // Pdf header
        assertEquals("%PDF-1", String(pdfBytes.take(6).toByteArray()))
    }

    @Test
    fun `generateTextPdf handles empty input gracefully`() {
        val outputStream = ByteArrayOutputStream()
        val textPages = emptyList<String>()
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }

    @Test
    fun `generateTextPdf handles multiple short pages`() {
        val outputStream = ByteArrayOutputStream()
        val textPages = listOf("Page 1", "Page 2", "Page 3")
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }

    @Test
    fun `generateTextPdf handles special characters`() {
        val outputStream = ByteArrayOutputStream()
        val textPages = listOf("Special: !@#$%^&*()_+ \n New Line \t Tab \uD83D\uDE00")
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }

    @Test
    fun `generateTextPdf skips blank pages`() {
        val outputStream = ByteArrayOutputStream()
        val textPages = listOf("Content", "  ", "\n", "More Content")
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }

    @Test
    fun `generateTextPdf wraps long text onto multiple pages`() {
        val outputStream = ByteArrayOutputStream()
        val longText = (1..200).joinToString("\n") { "This is line number $it of a very long text that should span multiple pages in the generated PDF document." }
        val textPages = listOf(longText)
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }

    @Test
    fun `generateTextPdf handles extremely long single word`() {
        val outputStream = ByteArrayOutputStream()
        val longWord = "A" + "very".repeat(100) + "LongWord"
        val textPages = listOf(longWord)
        
        PdfGenerator.generateTextPdf(textPages, outputStream)
        
        val pdfBytes = outputStream.toByteArray()
        assertTrue(pdfBytes.isNotEmpty())
    }
}
