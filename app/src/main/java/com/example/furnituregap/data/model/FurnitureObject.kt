package com.example.furnituregap.data.model

import java.util.UUID

data class FurnitureObject(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "Furniture",
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double = 850.0,
    val xMm: Double = 0.0,
    val yMm: Double = 0.0,
    val rotationDegrees: Int = 0, // 0, 90, 180, 270
    val requiredClearanceMm: Double = 600.0,
    val notes: String = "",
    val colorHex: Long = 0xFF2A52BE // architectural slate blue by default
) {
    val normalizedRotation: Int
        get() = ((rotationDegrees % 360) + 360) % 360

    val effectiveWidthMm: Double
        get() = if (normalizedRotation % 180 != 0) depthMm else widthMm

    val effectiveDepthMm: Double
        get() = if (normalizedRotation % 180 != 0) widthMm else depthMm

    fun toEffectiveRect(): Rect {
        return Rect(
            leftMm = xMm,
            topMm = yMm,
            widthMm = effectiveWidthMm,
            depthMm = effectiveDepthMm
        )
    }
}
