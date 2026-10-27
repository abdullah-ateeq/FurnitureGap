package com.example.furnituregap.data.model

import kotlin.math.max
import kotlin.math.min

data class Rect(
    val leftMm: Double,
    val topMm: Double,
    val widthMm: Double,
    val depthMm: Double
) {
    val rightMm: Double
        get() = leftMm + widthMm

    val bottomMm: Double
        get() = topMm + depthMm

    val centerX: Double
        get() = leftMm + (widthMm / 2.0)

    val centerY: Double
        get() = topMm + (depthMm / 2.0)

    val areaSquareMm: Double
        get() = max(0.0, widthMm) * max(0.0, depthMm)
}

enum class ClearanceDirection(val label: String) {
    LEFT("Left"),
    RIGHT("Right"),
    FRONT("Front"),
    BACK("Back"),
    DIAGONAL("Diagonal"),
    OVERLAP("Overlap"),
    TOUCHING("Touching")
}

enum class ClearanceStatus(val label: String, val badge: String) {
    ADEQUATE("Adequate", "✓ ADEQUATE"),
    BELOW_TARGET("Below Target", "⚠ BELOW TARGET"),
    TOUCHING("Touching", "✓ TOUCHING"),
    OVERLAPPING("Overlapping", "✕ OVERLAPPING")
}

data class WallClearances(
    val leftMm: Double,
    val rightMm: Double,
    val topMm: Double,
    val bottomMm: Double
) {
    val minimumClearanceMm: Double
        get() = min(min(leftMm, rightMm), min(topMm, bottomMm))

    val maximumClearanceMm: Double
        get() = max(max(leftMm, rightMm), max(topMm, bottomMm))

    fun tightestSide(): String {
        val minVal = minimumClearanceMm
        return when (minVal) {
            leftMm -> "Left Wall"
            rightMm -> "Right Wall"
            topMm -> "Top / Front Wall"
            else -> "Bottom / Back Wall"
        }
    }
}

data class BoundaryCheck(
    val isOutside: Boolean,
    val exceededLeftMm: Double = 0.0,
    val exceededRightMm: Double = 0.0,
    val exceededTopMm: Double = 0.0,
    val exceededBottomMm: Double = 0.0
) {
    fun summary(unit: LengthUnit = LengthUnit.CENTIMETERS, decimals: Int = 1): String {
        if (!isOutside) return "Within room boundaries"
        val parts = mutableListOf<String>()
        if (exceededLeftMm > 0) parts.add("Left by ${unit.format(exceededLeftMm, decimals)}")
        if (exceededRightMm > 0) parts.add("Right by ${unit.format(exceededRightMm, decimals)}")
        if (exceededTopMm > 0) parts.add("Top by ${unit.format(exceededTopMm, decimals)}")
        if (exceededBottomMm > 0) parts.add("Bottom by ${unit.format(exceededBottomMm, decimals)}")
        return "Exceeds room: ${parts.joinToString(", ")}"
    }
}

data class ClearanceResult(
    val objectAId: String,
    val objectAName: String = "",
    val objectBId: String,
    val objectBName: String = "",
    val distanceMm: Double,
    val direction: ClearanceDirection,
    val overlaps: Boolean,
    val overlapWidthMm: Double = 0.0,
    val overlapDepthMm: Double = 0.0,
    val overlapAreaSquareMm: Double = 0.0,
    val requiredClearanceMm: Double = 600.0, // default 60 cm
    val rectA: Rect = Rect(0.0, 0.0, 0.0, 0.0),
    val rectB: Rect = Rect(0.0, 0.0, 0.0, 0.0)
) {
    val marginMm: Double
        get() = distanceMm - requiredClearanceMm

    val status: ClearanceStatus
        get() = when {
            overlaps -> ClearanceStatus.OVERLAPPING
            distanceMm <= 0.0 -> ClearanceStatus.TOUCHING
            distanceMm >= requiredClearanceMm -> ClearanceStatus.ADEQUATE
            else -> ClearanceStatus.BELOW_TARGET
        }

    val isAdequate: Boolean
        get() = status == ClearanceStatus.ADEQUATE || status == ClearanceStatus.TOUCHING
}

data class WalkwayResult(
    val availableWidthMm: Double,
    val requiredWidthMm: Double,
    val remainingWidthMm: Double,
    val fits: Boolean
) {
    val marginMm: Double
        get() = remainingWidthMm
}

data class DoorSwingCheck(
    val intersects: Boolean,
    val description: String
)
