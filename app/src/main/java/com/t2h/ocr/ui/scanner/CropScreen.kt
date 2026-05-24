package com.t2h.ocr.ui.scanner

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.opencv.core.Point
import androidx.compose.foundation.Image
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onGloballyPositioned
import com.t2h.ocr.R
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun CropScreen(
    imagePath: String,
    initialPoints: List<Point>,
    onConfirm: (List<Point>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(imagePath) {
        BitmapFactory.decodeFile(imagePath)
    }

    var corners by remember {
        mutableStateOf(
            if (initialPoints.size == 4) {
                initialPoints.map { Offset(it.x.toFloat(), it.y.toFloat()) }
            } else {
                // Default rectangle if no detection provided
                listOf(
                    Offset(0.1f, 0.1f),
                    Offset(0.9f, 0.1f),
                    Offset(0.9f, 0.9f),
                    Offset(0.1f, 0.9f)
                )
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (bitmap != null) {
            var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                .onGloballyPositioned { layoutCoordinates ->
                    canvasSize = androidx.compose.ui.geometry.Size(
                        layoutCoordinates.size.width.toFloat(),
                        layoutCoordinates.size.height.toFloat()
                    )
                }
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                val imageWidth = bitmap.width.toFloat()
                val imageHeight = bitmap.height.toFloat()
                val containerWidth = canvasSize.width
                val containerHeight = canvasSize.height

                if (containerWidth > 0 && containerHeight > 0) {
                    val scale = minOf(containerWidth / imageWidth, containerHeight / imageHeight)
                    val drawWidth = imageWidth * scale
                    val drawHeight = imageHeight * scale
                    val offsetX = (containerWidth - drawWidth) / 2
                    val offsetY = (containerHeight - drawHeight) / 2

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    val touchPoint = change.position
                                    val nearestIdx = corners.indices.minByOrNull { idx ->
                                        val cornerPx = Offset(
                                            corners[idx].x * drawWidth + offsetX,
                                            corners[idx].y * drawHeight + offsetY
                                        )
                                        distance(touchPoint, cornerPx)
                                    } ?: -1

                                    if (nearestIdx != -1) {
                                        val currentCornerPx = Offset(
                                            corners[nearestIdx].x * drawWidth + offsetX,
                                            corners[nearestIdx].y * drawHeight + offsetY
                                        )
                                        val newCornerPx = currentCornerPx + dragAmount

                                        val newNormalized = Offset(
                                            ((newCornerPx.x - offsetX) / drawWidth).coerceIn(0f, 1f),
                                            ((newCornerPx.y - offsetY) / drawHeight).coerceIn(0f, 1f)
                                        )

                                        val newCorners = corners.toMutableList()
                                        newCorners[nearestIdx] = newNormalized
                                        corners = newCorners
                                    }
                                    change.consume()
                                }
                            }
                    ) {
                        val path = Path().apply {
                            val p0 = Offset(corners[0].x * drawWidth + offsetX, corners[0].y * drawHeight + offsetY)
                            moveTo(p0.x, p0.y)
                            for (i in 1 until 4) {
                                val p = Offset(corners[i].x * drawWidth + offsetX, corners[i].y * drawHeight + offsetY)
                                lineTo(p.x, p.y)
                            }
                            close()
                        }

                        drawPath(
                            path = path,
                            color = Color.Green.copy(alpha = 0.2f),
                            style = Fill
                        )

                        drawPath(
                            path = path,
                            color = Color.Green,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        corners.forEach { corner ->
                            val p = Offset(corner.x * drawWidth + offsetX, corner.y * drawHeight + offsetY)
                            drawCircle(
                                color = Color.White,
                                radius = 12.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = Color.Green,
                                radius = 10.dp.toPx(),
                                center = p,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.crop_cancel))
            }
            Button(
                onClick = {
                    if (bitmap != null) {
                        val finalPoints = corners.map {
                            Point(it.x.toDouble() * bitmap.width, it.y.toDouble() * bitmap.height)
                        }
                        onConfirm(finalPoints)
                    }
                },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.crop_confirm))
            }
        }
    }
}

private fun distance(p1: Offset, p2: Offset): Float {
    return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
}
