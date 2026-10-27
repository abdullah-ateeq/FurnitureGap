package com.example.furnituregap.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.ui.theme.ArchitecturalNavy
import com.example.furnituregap.ui.theme.BlueprintCanvasDark
import com.example.furnituregap.ui.theme.BlueprintPrimary
import com.example.furnituregap.ui.theme.DimensionLineColor
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
        delay(350) // Total duration ~1.2 seconds
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueprintCanvasDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "FurnitureGap",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color.White
            )

            Text(
                text = "Measure furniture clearance and available space",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF90CAF9).copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Architectural diagram canvas
            Canvas(
                modifier = Modifier
                    .size(width = 280.dp, height = 220.dp)
            ) {
                val w = size.width
                val h = size.height

                // Outer Blueprint Border
                drawRect(
                    color = Color(0xFF64B5F6).copy(alpha = 0.7f),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h),
                    style = Stroke(width = 3f)
                )

                // Background grid lines
                for (x in 20..w.toInt() step 40) {
                    drawLine(
                        color = Color(0xFF1E3A5F).copy(alpha = 0.4f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), h),
                        strokeWidth = 1f
                    )
                }
                for (y in 20..h.toInt() step 40) {
                    drawLine(
                        color = Color(0xFF1E3A5F).copy(alpha = 0.4f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(w, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // SOFA block
                val sofaX = w * 0.18f
                val sofaY = h * 0.14f
                val sofaW = w * 0.64f
                val sofaH = h * 0.28f

                drawRect(
                    color = Color(0xFF1B3B6F),
                    topLeft = Offset(sofaX, sofaY),
                    size = Size(sofaW, sofaH)
                )
                drawRect(
                    color = Color(0xFF90CAF9),
                    topLeft = Offset(sofaX, sofaY),
                    size = Size(sofaW, sofaH),
                    style = Stroke(width = 2f)
                )

                // Native text for SOFA
                val paintSofa = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "SOFA",
                    sofaX + (sofaW / 2f),
                    sofaY + (sofaH / 2f) + 10f,
                    paintSofa
                )

                // WALL block at bottom
                val wallY = h * 0.78f
                val wallH = h * 0.22f

                drawRect(
                    color = Color(0xFF0F1E36),
                    topLeft = Offset(0f, wallY),
                    size = Size(w, wallH)
                )
                drawLine(
                    color = Color(0xFF64B5F6),
                    start = Offset(0f, wallY),
                    end = Offset(w, wallY),
                    strokeWidth = 4f
                )

                val paintWall = android.graphics.Paint().apply {
                    color = android.graphics.Color.LTGRAY
                    textSize = 24f
                    isFakeBoldText = true
                    letterSpacing = 0.1f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "WALL",
                    w / 2f,
                    wallY + (wallH / 2f) + 8f,
                    paintWall
                )

                // Dimension Line Animation: Arrow between SOFA bottom and WALL top
                val dimY = (sofaY + sofaH + wallY) / 2f
                val arrowProgress = lineProgress.value
                val currentWidth = (sofaW * arrowProgress)
                val lineStartX = (w / 2f) - (currentWidth / 2f)
                val lineEndX = (w / 2f) + (currentWidth / 2f)

                if (currentWidth > 10f) {
                    drawLine(
                        color = DimensionLineColor,
                        start = Offset(lineStartX, dimY),
                        end = Offset(lineEndX, dimY),
                        strokeWidth = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )

                    // Dimension label pill in center
                    if (arrowProgress > 0.6f) {
                        val pillPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#1C2C46")
                            style = android.graphics.Paint.Style.FILL
                        }
                        val pillTextPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 22f
                            isFakeBoldText = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        val text = "← 60 cm →"
                        drawContext.canvas.nativeCanvas.drawRoundRect(
                            (w / 2f) - 60f, dimY - 16f, (w / 2f) + 60f, dimY + 16f,
                            8f, 8f, pillPaint
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            text,
                            w / 2f,
                            dimY + 8f,
                            pillTextPaint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Furniture Clearance Calculator",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = Color(0xFFBAC7D5)
            )
        }
    }
}
