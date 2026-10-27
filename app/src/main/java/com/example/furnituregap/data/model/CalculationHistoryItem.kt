package com.example.furnituregap.data.model

import java.util.UUID

data class CalculationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val calculationMode: String,
    val clearanceMm: Double,
    val requiredClearanceMm: Double,
    val status: ClearanceStatus,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val snapshotJson: String = "" // serializable state for reopening
)
