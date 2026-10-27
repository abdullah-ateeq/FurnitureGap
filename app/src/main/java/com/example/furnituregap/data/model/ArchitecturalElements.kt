package com.example.furnituregap.data.model

import java.util.UUID

data class Door(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Door",
    val hingeXmm: Double,
    val hingeYmm: Double,
    val widthMm: Double = 900.0,
    val swingDegrees: Double = 90.0, // 90.0, 180.0
    val rotation: Double = 0.0 // Base angle of closed door: 0° = along +X, 90° = along +Y, 180° = -X, 270° = -Y
)

data class Window(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Window",
    val xMm: Double,
    val yMm: Double,
    val widthMm: Double = 1500.0,
    val sillHeightMm: Double = 900.0,
    val heightMm: Double = 1200.0
)
