package com.example.furnituregap.data.model

enum class AppThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

data class ClearancePresets(
    val tightMm: Double = 450.0,      // 45 cm
    val comfortableMm: Double = 600.0, // 60 cm
    val spaciousMm: Double = 900.0    // 90 cm
)

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val unit: LengthUnit = LengthUnit.CENTIMETERS,
    val defaultGridSpacingMm: Double = 500.0, // 50 cm
    val snapToGrid: Boolean = false,
    val defaultTargetClearanceMm: Double = 600.0, // 60 cm
    val presets: ClearancePresets = ClearancePresets(),
    val decimalPlaces: Int = 2,
    // Display flags
    val showDimensions: Boolean = true,
    val showGrid: Boolean = true,
    val showClearanceLines: Boolean = true,
    val showRoomOccupancy: Boolean = true,
    val showRoomDiagram: Boolean = true,
    val showClearanceMatrix: Boolean = true,
    val showDimensionLines: Boolean = true,
    val showObjectLabels: Boolean = true,
    val showCalculationBreakdown: Boolean = true,
    // Behavior flags
    val liveCalculation: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastPlan: Boolean = true
)
