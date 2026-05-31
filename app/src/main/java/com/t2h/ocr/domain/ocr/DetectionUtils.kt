package com.t2h.ocr.domain.ocr

import android.util.Log
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*

/**
 * Reusable document detection logic extracted from DocumentAnalyzer.
 */
object DetectionUtils {
    private const val TAG = "DetectionUtils"

    /**
     * Detects a document quadrilateral in the given gray [source] Mat.
     * Returns a list of 4 normalized points (0.0 to 1.0) or an empty list if not found.
     * 
     * Note: This method allocates internal Mats for processing. For high-frequency 
     * calls (like camera frames), consider the version that accepts pre-allocated Mats.
     */
    fun detectDocument(source: Mat): List<Point> {
        val gray = Mat()
        val blurred = Mat()
        val thresholded = Mat()
        val morph = Mat()
        val hierarchy = Mat()

        try {
            return detectDocument(source, gray, blurred, thresholded, morph, hierarchy)
        } finally {
            gray.release()
            blurred.release()
            thresholded.release()
            morph.release()
            hierarchy.release()
        }
    }

    /**
     * Performance-optimized version of [detectDocument] that reuses provided Mats.
     */
    fun detectDocument(
        source: Mat,
        gray: Mat,
        blurred: Mat,
        thresholded: Mat,
        morph: Mat,
        hierarchy: Mat
    ): List<Point> {
        val srcW = source.width()
        val srcH = source.height()
        
        val scale = if (srcW > 640) 640.0 / srcW else 1.0
        val width = (srcW * scale).toInt()
        val height = (srcH * scale).toInt()

        try {
            if (scale < 1.0) {
                Imgproc.resize(source, gray, Size(width.toDouble(), height.toDouble()))
            } else {
                source.copyTo(gray)
            }

            // 1. Heavy Noise Reduction: Median blur is excellent for removing text/grain
            Imgproc.medianBlur(gray, blurred, 9)
            
            // 2. Edge Detection
            Imgproc.Canny(blurred, thresholded, 50.0, 150.0)
            
            // 3. Morphological Closing: Join broken edges with a medium kernel for accuracy
            Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0)).use { kernel ->
                Imgproc.morphologyEx(thresholded, morph, Imgproc.MORPH_CLOSE, kernel)
            }

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

            val result = if (largestQuad != null) {
                val sortedPoints = sortPoints(largestQuad!!.toList())
                // Normalize coordinates (0.0 to 1.0) based on the processing resolution
                val normalizedPoints = sortedPoints.map { p -> 
                    Point(p.x / width, p.y / height)
                }
                largestQuad!!.release()
                normalizedPoints
            } else {
                emptyList()
            }

            contours.forEach { it.release() }
            return result

        } catch (e: Exception) {
            Log.e(TAG, "Detection Error", e)
            return emptyList()
        }
    }

    /**
     * Sorts points in order: Top-Left, Top-Right, Bottom-Right, Bottom-Left.
     */
    fun sortPoints(points: List<Point>): List<Point> {
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
