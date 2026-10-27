package com.example.furnituregap.data.model

import java.util.UUID

data class FloorPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Layout",
    val roomWidthMm: Double = 5000.0, // 500 cm
    val roomDepthMm: Double = 4000.0, // 400 cm
    val objects: List<FurnitureObject> = emptyList(),
    val doors: List<Door> = emptyList(),
    val windows: List<Window> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
