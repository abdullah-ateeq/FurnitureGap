package com.example.furnituregap.data.model

import java.util.Locale

enum class LengthUnit(val label: String, val symbol: String, val mmPerUnit: Double) {
    MILLIMETERS("Millimeters", "mm", 1.0),
    CENTIMETERS("Centimeters", "cm", 10.0),
    METERS("Meters", "m", 1000.0),
    INCHES("Inches", "in", 25.4),
    FEET("Feet", "ft", 304.8);

    fun fromMm(mm: Double): Double = mm / mmPerUnit

    fun toMm(value: Double): Double = value * mmPerUnit

    fun format(mm: Double, decimals: Int = 2, includeSymbol: Boolean = true): String {
        val converted = fromMm(mm)
        val formatStr = "%.${decimals}f"
        val formattedNumber = String.format(Locale.US, formatStr, converted)
        return if (includeSymbol) "$formattedNumber $symbol" else formattedNumber
    }

    companion object {
        fun fromSymbol(symbol: String): LengthUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: CENTIMETERS
        }
    }
}
