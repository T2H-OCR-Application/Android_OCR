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
        val yBuffer = image.planes[0].buffer
        val ySize = yBuffer.remaining()
        val yData = ByteArray(ySize)
        yBuffer.get(yData)
        
        Mat(image.height, image.width, CvType.CV_8UC1).use { rawGray ->
            rawGray.put(0, 0, yData)
            
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
        
        trace("DocumentAnalyzer#resize") {
            if (scale < 1.0) {
                Imgproc.resize(graySource, gray, Size(width.toDouble(), height.toDouble()))
            } else {
                graySource.copyTo(gray)
            }
        }

        try {
            trace("DocumentAnalyzer#preprocessing") {
                // 1. Preprocessing: Blur + Adaptive Threshold
                Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
                
                // Adaptive threshold is often more robust than Canny for varying lighting
                Imgproc.adaptiveThreshold(
                    blurred, thresholded, 255.0,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY, 11, 2.0
                )
                
                // 2. Morphological closing with a large kernel to join edges
                Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(11.0, 11.0)).use { kernel ->
                    Imgproc.morphologyEx(thresholded, morph, Imgproc.MORPH_CLOSE, kernel)
                }
            }

            trace("DocumentAnalyzer#contours") {
                // 3. Find contours
                val contours = ArrayList<MatOfPoint>()
                Imgproc.findContours(morph, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

                var largestQuad: MatOfPoint2f? = null
                var maxArea = 0.0

                val sortedContours = contours.sortedByDescending { Imgproc.contourArea(it) }
                
                for (contour in sortedContours.take(5)) {
                    MatOfPoint2f(*contour.toArray()).use { contour2f ->
                        val peri = Imgproc.arcLength(contour2f, true)
                        val approx = MatOfPoint2f()
                        // Higher epsilon (0.04) to handle distortion
                        Imgproc.approxPolyDP(contour2f, approx, 0.04 * peri, true)
                        val pts = approx.total()
                        val area = Imgproc.contourArea(approx)
                        
                        if (pts == 4L && area > (width * height * 0.01)) {
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
                    val points = largestQuad!!.toList().map { p -> 
                        Point(p.x / scale, p.y / scale)
                    }
                    onDetected(points, srcW, srcH)
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
}
