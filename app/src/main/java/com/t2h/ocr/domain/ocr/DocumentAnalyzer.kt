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
        
        val scale = if (srcW > 640) 640.0 / srcW else 1.0
        val width = (srcW * scale).toInt()
        val height = (srcH * scale).toInt()
        
        Log.d(TAG, "Analyzer: Processing image ${width}x${height} (scale $scale)")
        
        trace("DocumentAnalyzer#resize") {
            if (scale < 1.0) {
                Imgproc.resize(graySource, gray, Size(width.toDouble(), height.toDouble()))
            } else {
                graySource.copyTo(gray)
            }
        }

        try {
            trace("DocumentAnalyzer#preprocessing") {
                // 1. Heavy Noise Reduction: Median blur is excellent for removing text/grain
                Imgproc.medianBlur(gray, blurred, 9)
                
                // 2. Edge Detection
                Imgproc.Canny(blurred, thresholded, 50.0, 150.0)
                
                // 3. Morphological Closing: Join broken edges with a medium kernel for accuracy
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0)).use { kernel ->
                    Imgproc.morphologyEx(thresholded, morph, Imgproc.MORPH_CLOSE, kernel)
                }
            }

            trace("DocumentAnalyzer#contours") {
                val contours = ArrayList<MatOfPoint>()
                Imgproc.findContours(morph, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

                var largestQuad: MatOfPoint2f? = null
                var maxArea = 0.0
                val minArea = width * height * 0.05 // 5% minimum to ignore noise

                val sortedContours = contours.sortedByDescending { Imgproc.contourArea(it) }
                
                for (contour in sortedContours.take(10)) {
                    MatOfPoint2f(*contour.toArray()).use { contour2f ->
                        val peri = Imgproc.arcLength(contour2f, true)
                        val approx = MatOfPoint2f()
                        // Tighter epsilon (0.015) for precise corner fitting
                        Imgproc.approxPolyDP(contour2f, approx, 0.015 * peri, true)
                        
                        val pts = approx.total()
                        val area = Imgproc.contourArea(approx)
                        
                        // Check for 4 points AND convexity (must be a convex polygon)
                        if (pts == 4L && area > minArea && Imgproc.isContourConvex(MatOfPoint(*approx.toArray()))) {
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
                    }
                }

                if (largestQuad != null) {
                    val sortedPoints = sortPoints(largestQuad!!.toList())
                    // Normalize coordinates (0.0 to 1.0) based on the processing resolution
                    val normalizedPoints = sortedPoints.map { p -> 
                        Point(p.x / width, p.y / height)
                    }
                    onDetected(normalizedPoints, srcW, srcH)
                    largestQuad!!.release()
                } else {
                    onDetected(emptyList(), srcW, srcH)
                }

                contours.forEach { it.release() }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Analyzer Error", e)
        }
    }

    /**
     * Sorts points in order: Top-Left, Top-Right, Bottom-Right, Bottom-Left.
     */
    private fun sortPoints(points: List<Point>): List<Point> {
        if (points.size != 4) return points
        
        val sortedBySum = points.sortedBy { it.x + it.y }
        val tl = sortedBySum.first()
        val br = sortedBySum.last()
        
        val sortedByDiff = points.sortedBy { it.x - it.y }
        val bl = sortedByDiff.first()
        val tr = sortedByDiff.last()
        
        return listOf(tl, tr, br, bl)
    }
}
