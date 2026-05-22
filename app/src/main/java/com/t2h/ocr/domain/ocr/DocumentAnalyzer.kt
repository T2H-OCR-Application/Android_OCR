package com.t2h.ocr.domain.ocr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.util.*

/**
 * Analyzes camera frames to detect a 4-corner document quadrilateral.
 * Follows T-03-01 to strictly release Mat instances.
 */
class DocumentAnalyzer(
    private val onDetected: (List<Point>) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val mat = imageToMat(image)
        if (mat == null) {
            image.close()
            return
        }

        val gray = Mat()
        val blurred = Mat()
        val edged = Mat()
        val hierarchy = Mat()
        
        try {
            // 1. Preprocessing for edge detection
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
            Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(blurred, edged, 75.0, 200.0)

            // 2. Find contours
            val contours = ArrayList<MatOfPoint>()
            Imgproc.findContours(edged, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

            // 3. Locate the largest 4-point quadrilateral
            var largestQuad: MatOfPoint2f? = null
            var maxArea = 0.0

            // Sort by area descending to find the largest candidate quickly
            val sortedContours = contours.sortedByDescending { Imgproc.contourArea(it) }
            
            for (contour in sortedContours.take(10)) {
                val contour2f = MatOfPoint2f(*contour.toArray())
                val peri = Imgproc.arcLength(contour2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true)

                if (approx.total() == 4L) {
                    val area = Imgproc.contourArea(approx)
                    if (area > maxArea) {
                        maxArea = area
                        largestQuad?.release()
                        largestQuad = approx
                    } else {
                        approx.release()
                    }
                } else {
                    approx.release()
                }
                contour2f.release()
            }

            largestQuad?.let {
                onDetected(it.toList())
                it.release()
            }

            // Cleanup contours
            contours.forEach { it.release() }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Strict release protocol (T-03-01)
            mat.release()
            gray.release()
            blurred.release()
            edged.release()
            hierarchy.release()
            image.close()
        }
    }

    private fun imageToMat(image: ImageProxy): Mat? {
        return try {
            val nv21 = yuv420888ToNv21(image)
            val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
            yuvMat.put(0, 0, nv21)
            val rgbMat = Mat()
            Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21, 3)
            yuvMat.release()
            rgbMat
        } catch (e: Exception) {
            null
        }
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val pixelCount = image.width * image.height
        val pixelSizeBits = 12 // NV21 is 12 bits per pixel
        val outputBuffer = ByteArray(pixelCount * pixelSizeBits / 8)

        val yBuffer = image.planes[0].buffer
        val vBuffer = image.planes[2].buffer
        val uBuffer = image.planes[1].buffer

        yBuffer.get(outputBuffer, 0, pixelCount)

        // Interleave V and U
        var outputOffset = pixelCount
        val vRowStride = image.planes[2].rowStride
        val vPixelStride = image.planes[2].pixelStride
        val uRowStride = image.planes[1].rowStride
        val uPixelStride = image.planes[1].pixelStride

        for (row in 0 until image.height / 2) {
            for (col in 0 until image.width / 2) {
                val vIndex = row * vRowStride + col * vPixelStride
                val uIndex = row * uRowStride + col * uPixelStride
                
                outputBuffer[outputOffset++] = vBuffer.get(vIndex)
                outputBuffer[outputOffset++] = uBuffer.get(uIndex)
            }
        }

        return outputBuffer
    }
}
