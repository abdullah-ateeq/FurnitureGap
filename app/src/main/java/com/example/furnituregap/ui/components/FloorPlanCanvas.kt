package com.example.furnituregap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.AppSettings
import com.example.furnituregap.data.model.ClearanceDirection
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FloorPlan
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.Window
import com.example.furnituregap.ui.theme.DimensionLineColor
import com.example.furnituregap.ui.theme.GridLineDark
import com.example.furnituregap.ui.theme.GridLineLight
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun FloorPlanCanvas(
    plan: FloorPlan,
    settings: AppSettings,
    selectedObjectId: String?,
    selectedPair: Pair<String, String>?,
    onSelectObject: (String?) -> Unit,
    onMoveObject: (String, Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    var draggingObjectId by remember { mutableStateOf<String?>(null) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var initialObjX by remember { mutableStateOf(0.0) }
    var initialObjY by remember { mutableStateOf(0.0) }

    // Geometry scaling cache
    var currentScale by remember { mutableStateOf(1f) }
    var currentOriginX by remember { mutableStateOf(0f) }
    var currentOriginY by remember { mutableStateOf(0f) }

    val gridColor = if (isDarkTheme) GridLineDark else GridLineLight
    val wallColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1B3B6F)
    val textColor = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1A202C)
    val roomBgColor = if (isDarkTheme) Color(0xFF0F1E36) else Color(0xFFFAFCFF)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(plan) {
                    detectTapGestures { tapOffset ->
                        val localX = (tapOffset.x - currentOriginX) / currentScale
                        val localY = (tapOffset.y - currentOriginY) / currentScale

                        val tapped = plan.objects.lastOrNull { obj ->
                            val r = obj.toEffectiveRect()
                            localX >= r.leftMm && localX <= r.rightMm && localY >= r.topMm && localY <= r.bottomMm
                        }
                        onSelectObject(tapped?.id)
                    }
                }
                .pointerInput(plan, settings.snapToGrid) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val localX = (startOffset.x - currentOriginX) / currentScale
                            val localY = (startOffset.y - currentOriginY) / currentScale

                            val target = plan.objects.lastOrNull { obj ->
                                val r = obj.toEffectiveRect()
                                localX >= r.leftMm && localX <= r.rightMm && localY >= r.topMm && localY <= r.bottomMm
                            }

                            if (target != null) {
                                draggingObjectId = target.id
                                onSelectObject(target.id)
                                dragStartOffset = startOffset
                                initialObjX = target.xMm
                                initialObjY = target.yMm
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragId = draggingObjectId
                            if (dragId != null) {
                                val dxMm = (dragAmount.x) / currentScale
                                val dyMm = (dragAmount.y) / currentScale
                                val obj = plan.objects.find { it.id == dragId }
                                if (obj != null) {
                                    val newX = max(0.0, obj.xMm + dxMm)
                                    val newY = max(0.0, obj.yMm + dyMm)
                                    onMoveObject(dragId, newX, newY)
                                }
                            }
                        },
                        onDragEnd = {
                            draggingObjectId = null
                        },
                        onDragCancel = {
                            draggingObjectId = null
                        }
                    )
                }
        ) {
            val padding = 48f
            val availableW = size.width - (2 * padding)
            val availableH = size.height - (2 * padding)

            val roomW = max(100.0, plan.roomWidthMm)
            val roomD = max(100.0, plan.roomDepthMm)

            val scale = min(availableW / roomW.toFloat(), availableH / roomD.toFloat())
            currentScale = scale

            val scaledRoomW = roomW.toFloat() * scale
            val scaledRoomD = roomD.toFloat() * scale

            val originX = padding + (availableW - scaledRoomW) / 2f
            val originY = padding + (availableH - scaledRoomD) / 2f
            currentOriginX = originX
            currentOriginY = originY

            // 1. Room Floor Background
            drawRect(
                color = roomBgColor,
                topLeft = Offset(originX, originY),
                size = Size(scaledRoomW, scaledRoomD)
            )

            // 2. Blueprint Grid
            if (settings.showGrid) {
                drawGrid(
                    originX = originX,
                    originY = originY,
                    roomWidthPx = scaledRoomW,
                    roomDepthPx = scaledRoomD,
                    spacingPx = settings.defaultGridSpacingMm.toFloat() * scale,
                    gridColor = gridColor
                )
            }

            // 3. Room Outer Walls
            drawRect(
                color = wallColor,
                topLeft = Offset(originX, originY),
                size = Size(scaledRoomW, scaledRoomD),
                style = Stroke(width = 6f)
            )
            // Inner architectural line
            drawRect(
                color = wallColor.copy(alpha = 0.4f),
                topLeft = Offset(originX + 4f, originY + 4f),
                size = Size(scaledRoomW - 8f, scaledRoomD - 8f),
                style = Stroke(width = 1.5f)
            )

            // 4. Windows
            plan.windows.forEach { win ->
                drawWindowElement(
                    window = win,
                    originX = originX,
                    originY = originY,
                    scale = scale,
                    wallColor = wallColor
                )
            }

            // 5. Doors
            plan.doors.forEach { door ->
                drawDoorElement(
                    door = door,
                    originX = originX,
                    originY = originY,
                    scale = scale,
                    wallColor = wallColor
                )
            }

            // Precompute overlaps among furniture
            val overlapsSet = mutableSetOf<String>()
            for (i in plan.objects.indices) {
                for (j in i + 1 until plan.objects.size) {
                    val rA = plan.objects[i].toEffectiveRect()
                    val rB = plan.objects[j].toEffectiveRect()
                    if (FurnitureGapCalculator.doRectanglesOverlap(rA, rB)) {
                        overlapsSet.add(plan.objects[i].id)
                        overlapsSet.add(plan.objects[j].id)
                    }
                }
            }

            // 6. Draw Furniture Objects
            plan.objects.forEach { obj ->
                val isSelected = obj.id == selectedObjectId
                val isOverlapping = overlapsSet.contains(obj.id)
                drawFurnitureObject(
                    obj = obj,
                    originX = originX,
                    originY = originY,
                    scale = scale,
                    isSelected = isSelected,
                    isOverlapping = isOverlapping,
                    unit = settings.unit,
                    decimals = settings.decimalPlaces,
                    showLabels = settings.showObjectLabels,
                    textColor = textColor,
                    isDark = isDarkTheme
                )
            }

            // 7. Clearance Lines & Dimension Arrows
            val selectedObj = plan.objects.find { it.id == selectedObjectId }
            if (settings.showClearanceLines && selectedObj != null) {
                // If a pair is selected, draw clearance line between them
                val pair = selectedPair
                if (pair != null) {
                    val pairA = plan.objects.find { it.id == pair.first }
                    val pairB = plan.objects.find { it.id == pair.second }
                    if (pairA != null && pairB != null) {
                        drawPairClearanceLine(
                            a = pairA,
                            b = pairB,
                            originX = originX,
                            originY = originY,
                            scale = scale,
                            unit = settings.unit,
                            decimals = settings.decimalPlaces,
                            isDark = isDarkTheme
                        )
                    }
                } else {
                    // Draw wall clearances for selected object
                    drawWallClearanceLines(
                        obj = selectedObj,
                        roomW = roomW,
                        roomD = roomD,
                        originX = originX,
                        originY = originY,
                        scale = scale,
                        unit = settings.unit,
                        decimals = settings.decimalPlaces,
                        isDark = isDarkTheme
                    )
                }
            }

            // 8. Overall Room Dimension Labels
            if (settings.showDimensions) {
                drawRoomDimensions(
                    roomWidthMm = roomW,
                    roomDepthMm = roomD,
                    originX = originX,
                    originY = originY,
                    scaledRoomW = scaledRoomW,
                    scaledRoomD = scaledRoomD,
                    unit = settings.unit,
                    decimals = settings.decimalPlaces,
                    textColor = textColor
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    originX: Float,
    originY: Float,
    roomWidthPx: Float,
    roomDepthPx: Float,
    spacingPx: Float,
    gridColor: Color
) {
    if (spacingPx <= 0f) return
    var x = originX + spacingPx
    while (x < originX + roomWidthPx) {
        drawLine(
            color = gridColor,
            start = Offset(x, originY),
            end = Offset(x, originY + roomDepthPx),
            strokeWidth = 1f
        )
        x += spacingPx
    }

    var y = originY + spacingPx
    while (y < originY + roomDepthPx) {
        drawLine(
            color = gridColor,
            start = Offset(originX, y),
            end = Offset(originX + roomWidthPx, y),
            strokeWidth = 1f
        )
        y += spacingPx
    }
}

private fun DrawScope.drawWindowElement(
    window: Window,
    originX: Float,
    originY: Float,
    scale: Float,
    wallColor: Color
) {
    val winX = originX + (window.xMm.toFloat() * scale)
    val winY = originY + (window.yMm.toFloat() * scale)
    val winW = window.widthMm.toFloat() * scale

    // Double glass lines on wall
    drawLine(
        color = Color(0xFF0288D1),
        start = Offset(winX, winY - 3f),
        end = Offset(winX + winW, winY - 3f),
        strokeWidth = 4f
    )
    drawLine(
        color = Color.White,
        start = Offset(winX, winY + 3f),
        end = Offset(winX + winW, winY + 3f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawDoorElement(
    door: Door,
    originX: Float,
    originY: Float,
    scale: Float,
    wallColor: Color
) {
    val hx = originX + (door.hingeXmm.toFloat() * scale)
    val hy = originY + (door.hingeYmm.toFloat() * scale)
    val r = door.widthMm.toFloat() * scale

    // Draw hinge dot
    drawCircle(
        color = wallColor,
        radius = 4f,
        center = Offset(hx, hy)
    )

    // Swing arc
    val startAngle = door.rotation.toFloat()
    val sweepAngle = door.swingDegrees.toFloat()
    drawArc(
        color = wallColor.copy(alpha = 0.5f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(hx - r, hy - r),
        size = Size(2 * r, 2 * r),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
    )

    // Closed door leaf line
    val rad0 = Math.toRadians(startAngle.toDouble())
    drawLine(
        color = wallColor,
        start = Offset(hx, hy),
        end = Offset((hx + r * cos(rad0)).toFloat(), (hy + r * sin(rad0)).toFloat()),
        strokeWidth = 3f
    )

    // Open door leaf line
    val radEnd = Math.toRadians((startAngle + sweepAngle).toDouble())
    drawLine(
        color = wallColor,
        start = Offset(hx, hy),
        end = Offset((hx + r * cos(radEnd)).toFloat(), (hy + r * sin(radEnd)).toFloat()),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawFurnitureObject(
    obj: FurnitureObject,
    originX: Float,
    originY: Float,
    scale: Float,
    isSelected: Boolean,
    isOverlapping: Boolean,
    unit: LengthUnit,
    decimals: Int,
    showLabels: Boolean,
    textColor: Color,
    isDark: Boolean
) {
    val r = obj.toEffectiveRect()
    val px = originX + (r.leftMm.toFloat() * scale)
    val py = originY + (r.topMm.toFloat() * scale)
    val pw = r.widthMm.toFloat() * scale
    val pd = r.depthMm.toFloat() * scale

    val baseColor = Color(obj.colorHex)
    val fillColor = when {
        isOverlapping -> StatusOverlapping.copy(alpha = 0.4f)
        isSelected -> baseColor.copy(alpha = 0.75f)
        else -> baseColor.copy(alpha = 0.5f)
    }

    val strokeColor = when {
        isOverlapping -> StatusOverlapping
        isSelected -> Color(0xFF64B5F6)
        else -> baseColor
    }

    // Furniture fill
    drawRect(
        color = fillColor,
        topLeft = Offset(px, py),
        size = Size(pw, pd)
    )

    // Furniture outline
    drawRect(
        color = strokeColor,
        topLeft = Offset(px, py),
        size = Size(pw, pd),
        style = Stroke(width = if (isSelected) 3.5f else 2f)
    )

    // Selection corner markers
    if (isSelected) {
        val cornerSize = 8f
        val markerColor = Color(0xFF64B5F6)
        drawRect(markerColor, Offset(px - 4f, py - 4f), Size(cornerSize, cornerSize))
        drawRect(markerColor, Offset(px + pw - 4f, py - 4f), Size(cornerSize, cornerSize))
        drawRect(markerColor, Offset(px - 4f, py + pd - 4f), Size(cornerSize, cornerSize))
        drawRect(markerColor, Offset(px + pw - 4f, py + pd - 4f), Size(cornerSize, cornerSize))
    }

    // Text labels inside the block
    if (showLabels && pw > 30f && pd > 20f) {
        val paint = android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            textSize = max(18f, min(26f, pw * 0.14f))
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val subPaint = android.graphics.Paint().apply {
            color = if (isDark) android.graphics.Color.LTGRAY else android.graphics.Color.DKGRAY
            textSize = max(14f, min(20f, pw * 0.10f))
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        drawContext.canvas.nativeCanvas.drawText(
            obj.name,
            px + (pw / 2f),
            py + (pd / 2f) - (if (pd > 40f) 6f else 0f),
            paint
        )

        if (pd > 45f) {
            val dimStr = "${unit.format(obj.effectiveWidthMm, decimals)} × ${unit.format(obj.effectiveDepthMm, decimals)}"
            drawContext.canvas.nativeCanvas.drawText(
                dimStr,
                px + (pw / 2f),
                py + (pd / 2f) + 16f,
                subPaint
            )
        }
    }
}

private fun DrawScope.drawWallClearanceLines(
    obj: FurnitureObject,
    roomW: Double,
    roomD: Double,
    originX: Float,
    originY: Float,
    scale: Float,
    unit: LengthUnit,
    decimals: Int,
    isDark: Boolean
) {
    val r = obj.toEffectiveRect()
    val px = originX + (r.leftMm.toFloat() * scale)
    val py = originY + (r.topMm.toFloat() * scale)
    val pw = r.widthMm.toFloat() * scale
    val pd = r.depthMm.toFloat() * scale

    val leftMm = r.leftMm
    val rightMm = roomW - r.rightMm
    val topMm = r.topMm
    val bottomMm = roomD - r.bottomMm

    val dimColor = DimensionLineColor

    // Left gap line
    if (leftMm > 0) {
        val midY = py + (pd / 2f)
        drawDimensionArrow(
            start = Offset(originX, midY),
            end = Offset(px, midY),
            label = unit.format(leftMm, decimals),
            color = dimColor,
            isDark = isDark
        )
    }

    // Right gap line
    if (rightMm > 0) {
        val midY = py + (pd / 2f)
        drawDimensionArrow(
            start = Offset(px + pw, midY),
            end = Offset(originX + (roomW.toFloat() * scale), midY),
            label = unit.format(rightMm, decimals),
            color = dimColor,
            isDark = isDark
        )
    }

    // Top gap line
    if (topMm > 0) {
        val midX = px + (pw / 2f)
        drawDimensionArrow(
            start = Offset(midX, originY),
            end = Offset(midX, py),
            label = unit.format(topMm, decimals),
            color = dimColor,
            isDark = isDark
        )
    }

    // Bottom gap line
    if (bottomMm > 0) {
        val midX = px + (pw / 2f)
        drawDimensionArrow(
            start = Offset(midX, py + pd),
            end = Offset(midX, originY + (roomD.toFloat() * scale)),
            label = unit.format(bottomMm, decimals),
            color = dimColor,
            isDark = isDark
        )
    }
}

private fun DrawScope.drawPairClearanceLine(
    a: FurnitureObject,
    b: FurnitureObject,
    originX: Float,
    originY: Float,
    scale: Float,
    unit: LengthUnit,
    decimals: Int,
    isDark: Boolean
) {
    val rA = a.toEffectiveRect()
    val rB = b.toEffectiveRect()

    val analysis = FurnitureGapCalculator.analyzePair(a, b)
    if (analysis.overlaps) return

    val pAx = originX + (rA.leftMm.toFloat() * scale)
    val pAy = originY + (rA.topMm.toFloat() * scale)
    val pAw = rA.widthMm.toFloat() * scale
    val pAd = rA.depthMm.toFloat() * scale

    val pBx = originX + (rB.leftMm.toFloat() * scale)
    val pBy = originY + (rB.topMm.toFloat() * scale)
    val pBw = rB.widthMm.toFloat() * scale
    val pBd = rB.depthMm.toFloat() * scale

    val (start, end) = when (analysis.direction) {
        ClearanceDirection.RIGHT -> {
            val y = (max(pAy, pBy) + min(pAy + pAd, pBy + pBd)) / 2f
            Pair(Offset(pAx + pAw, y), Offset(pBx, y))
        }
        ClearanceDirection.LEFT -> {
            val y = (max(pAy, pBy) + min(pAy + pAd, pBy + pBd)) / 2f
            Pair(Offset(pAx, y), Offset(pBx + pBw, y))
        }
        ClearanceDirection.FRONT -> {
            val x = (max(pAx, pBx) + min(pAx + pAw, pBx + pBw)) / 2f
            Pair(Offset(x, pAy + pAd), Offset(x, pBy))
        }
        ClearanceDirection.BACK -> {
            val x = (max(pAx, pBx) + min(pAx + pAw, pBx + pBw)) / 2f
            Pair(Offset(x, pAy), Offset(x, pBy + pBd))
        }
        else -> {
            // Diagonal nearest corner connection
            val cornerAx = if (rA.centerX < rB.centerX) pAx + pAw else pAx
            val cornerAy = if (rA.centerY < rB.centerY) pAy + pAd else pAy
            val cornerBx = if (rA.centerX < rB.centerX) pBx else pBx + pBw
            val cornerBy = if (rA.centerY < rB.centerY) pBy else pBy + pBd
            Pair(Offset(cornerAx, cornerAy), Offset(cornerBx, cornerBy))
        }
    }

    drawDimensionArrow(
        start = start,
        end = end,
        label = unit.format(analysis.distanceMm, decimals),
        color = if (analysis.status == ClearanceStatus.BELOW_TARGET) StatusBelowTarget else StatusAdequate,
        isDark = isDark
    )
}

private fun DrawScope.drawDimensionArrow(
    start: Offset,
    end: Offset,
    label: String,
    color: Color,
    isDark: Boolean
) {
    // Dimension line
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
    )

    // Arrow ticks at endpoints
    drawCircle(color = color, radius = 3.5f, center = start)
    drawCircle(color = color, radius = 3.5f, center = end)

    // Midpoint label pill
    val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
    val paint = android.graphics.Paint().apply {
        this.color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        textSize = 24f
        isAntiAlias = true
        isFakeBoldText = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val bgPaint = android.graphics.Paint().apply {
        this.color = if (isDark) android.graphics.Color.parseColor("#1C2C46") else android.graphics.Color.parseColor("#FFFFFF")
        style = android.graphics.Paint.Style.FILL
    }
    val borderPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.parseColor("#D32F2F")
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2f
    }

    val textBounds = android.graphics.Rect()
    paint.getTextBounds(label, 0, label.length, textBounds)
    val pillRect = android.graphics.RectF(
        mid.x - (textBounds.width() / 2f) - 10f,
        mid.y - (textBounds.height() / 2f) - 8f,
        mid.x + (textBounds.width() / 2f) + 10f,
        mid.y + (textBounds.height() / 2f) + 8f
    )

    drawContext.canvas.nativeCanvas.drawRoundRect(pillRect, 8f, 8f, bgPaint)
    drawContext.canvas.nativeCanvas.drawRoundRect(pillRect, 8f, 8f, borderPaint)
    drawContext.canvas.nativeCanvas.drawText(
        label,
        mid.x,
        mid.y + (textBounds.height() / 2f) - 2f,
        paint
    )
}

private fun DrawScope.drawRoomDimensions(
    roomWidthMm: Double,
    roomDepthMm: Double,
    originX: Float,
    originY: Float,
    scaledRoomW: Float,
    scaledRoomD: Float,
    unit: LengthUnit,
    decimals: Int,
    textColor: Color
) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 22f
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }

    // Top Width indicator
    val widthStr = "ROOM WIDTH: ${unit.format(roomWidthMm, decimals)}"
    drawContext.canvas.nativeCanvas.drawText(
        widthStr,
        originX + (scaledRoomW / 2f),
        originY - 16f,
        paint
    )

    // Bottom Depth indicator
    val depthStr = "ROOM DEPTH: ${unit.format(roomDepthMm, decimals)}"
    drawContext.canvas.nativeCanvas.drawText(
        depthStr,
        originX + (scaledRoomW / 2f),
        originY + scaledRoomD + 28f,
        paint
    )
}
