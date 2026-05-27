package com.t2h.ocr.ui.scanner

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R
import org.opencv.core.Point
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
                // Khung chữ nhật mặc định chiếm 80% diện tích tâm ảnh nếu không detect được góc
                listOf(
                    Offset(0.15f, 0.15f),
                    Offset(0.85f, 0.15f),
                    Offset(0.85f, 0.85f),
                    Offset(0.15f, 0.85f)
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1D24)) // Nền tối đồng bộ toàn hệ thống
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Tiêu đề hướng dẫn phía trên cùng
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Điều chỉnh góc cắt",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kéo 4 chấm tròn để khớp với viền tài liệu",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // KHU VỰC HIỂN THỊ ẢNH CẮT (CHIẾM PHẦN LỚN MÀN HÌNH)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF111318)) // Nền tối sâu hơn để làm nổi bật ảnh scan
        ) {
            if (bitmap != null) {
                var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp) // Khoảng đệm nhỏ tránh viền sát box
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

                            // Màu vùng chọn phủ mờ xanh ngọc thương hiệu
                            drawPath(
                                path = path,
                                color = Color(0xFF14B8A6).copy(alpha = 0.15f),
                                style = Fill
                            )

                            // Đường viền nét đứt hoặc nét liền xanh ngọc dày dặn
                            drawPath(
                                path = path,
                                color = Color(0xFF14B8A6),
                                style = Stroke(width = 2.5.dp.toPx())
                            )

                            // Vẽ các điểm mút neo chỉnh góc
                            corners.forEach { corner ->
                                val p = Offset(corner.x * drawWidth + offsetX, corner.y * drawHeight + offsetY)
                                drawCircle(
                                    color = Color.White,
                                    radius = 11.dp.toPx(),
                                    center = p
                                )
                                drawCircle(
                                    color = Color(0xFF14B8A6),
                                    radius = 9.dp.toPx(),
                                    center = p,
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                // Chấm tâm nhỏ bên trong cho tinh tế
                                drawCircle(
                                    color = Color(0xFF14B8A6),
                                    radius = 3.dp.toPx(),
                                    center = p
                                )
                            }
                        }
                    }
                }
            }
        }

        // KHỐI BENTO THANH ĐIỀU KHIỂN PHÍA DƯỚI (BOTTOM ACTION BAR)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF252329)) // Đồng bộ màu hộp bento đen xám
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Hủy bỏ (Thiết kế dạng Low-emphasis)
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.crop_cancel),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Nút Xác nhận cắt (Thiết kế High-emphasis màu chủ đạo Xanh Ngọc)
                Button(
                    onClick = {
                        if (bitmap != null) {
                            val finalPoints = corners.map {
                                Point(it.x.toDouble() * bitmap.width, it.y.toDouble() * bitmap.height)
                            }
                            onConfirm(finalPoints)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14B8A6),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.crop_confirm),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun distance(p1: Offset, p2: Offset): Float {
    return sqrt((p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2))
}