package com.example.furnituregap.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.furnituregap.data.model.LengthUnit
import java.util.Locale

@Composable
fun DimensionInputField(
    label: String,
    valueMm: Double,
    unit: LengthUnit,
    onValueChangeMm: (Double) -> Unit,
    modifier: Modifier = Modifier,
    decimals: Int = 1,
    isError: Boolean = false,
    supportingText: String? = null
) {
    // Format initial value in current unit
    var textValue by remember(valueMm, unit) {
        val converted = unit.fromMm(valueMm)
        val formatted = if (converted % 1.0 == 0.0) {
            converted.toLong().toString()
        } else {
            String.format(Locale.US, "%.${decimals}f", converted)
        }
        mutableStateOf(formatted)
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { input ->
            textValue = input
            val parsed = input.toDoubleOrNull()
            if (parsed != null && parsed >= 0) {
                onValueChangeMm(unit.toMm(parsed))
            }
        },
        label = { Text(label) },
        suffix = { Text(unit.symbol, fontWeight = FontWeight.Bold) },
        singleLine = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}
