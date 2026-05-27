package com.t2h.ocr.domain.ocr

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Handles image processing tasks such as perspective warping.
 * Ensures native resources are released to prevent OOM.
 */
object ImageProcessor {

    /**
     * Warps the perspective of the [input] Mat based on the [corners] provided.
     * Returns a new Mat with the top-down view of the document.
     * The caller is responsible for releasing the returned Mat.
     */
    fun warpPerspective(input: Mat, corners: List<Point>): Mat {
        val sortedCorners = sortCorners(corners)
        
        val tl = sortedCorners[0]
        val tr = sortedCorners[1]
        val br = sortedCorners[2]
        val bl = sortedCorners[3]

        // Calculate width of the new image
        val widthA = sqrt((br.x - bl.x).pow(2.0) + (br.y - bl.y).pow(2.0))
        val widthB = sqrt((tr.x - tl.x).pow(2.0) + (tr.y - tl.y).pow(2.0))
        val maxWidth = max(widthA, widthB).toInt()

        // Calculate height of the new image
        val heightA = sqrt((tr.x - br.x).pow(2.0) + (tr.y - br.y).pow(2.0))
        val heightB = sqrt((tl.x - bl.x).pow(2.0) + (tl.y - bl.y).pow(2.0))
        val maxHeight = max(heightA, heightB).toInt()

        val output = Mat()

        MatOfPoint2f(
            Point(0.0, 0.0),
            Point((maxWidth - 1).toDouble(), 0.0),
            Point((maxWidth - 1).toDouble(), (maxHeight - 1).toDouble()),
            Point(0.0, (maxHeight - 1).toDouble())
        ).use { dst ->
            MatOfPoint2f(*sortedCorners.toTypedArray()).use { src ->
                Imgproc.getPerspectiveTransform(src, dst).use { transform ->
                    Imgproc.warpPerspective(input, output, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))
                }
            }
        }

        return output
    }

    /**
     * Helper to warp a Bitmap directly.
     */
    fun warpBitmap(input: android.graphics.Bitmap, corners: List<Point>): android.graphics.Bitmap {
        val src = Mat()
        org.opencv.android.Utils.bitmapToMat(input, src)
        
        val warped = warpPerspective(src, corners)
        src.release()
        
        val output = android.graphics.Bitmap.createBitmap(warped.width(), warped.height(), android.graphics.Bitmap.Config.ARGB_8888)
        org.opencv.android.Utils.matToBitmap(warped, output)
        warped.release()
        
        return output
    }

    /**
     * Sorts corners in order: top-left, top-right, bottom-right, bottom-left.
     */
    private fun sortCorners(points: List<Point>): List<Point> {
        val sorted = points.sortedBy { it.x + it.y } // top-left is min sum, bottom-right is max sum
        val tl = sorted[0]
        val br = sorted[3]

        val remaining = points.filter { it != tl && it != br }
        val sortedDiff = remaining.sortedBy { it.y - it.x } // top-right is min diff, bottom-left is max diff
        val tr = sortedDiff[0]
        val bl = sortedDiff[1]

        return listOf(tl, tr, br, bl)
    }
}
