package com.example.furnituregap.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.data.model.AppThemeMode
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.ui.components.DimensionInputField
import com.example.furnituregap.ui.theme.StatusOverlapping
import com.example.furnituregap.ui.viewmodel.FurnitureGapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FurnitureGapViewModel
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val unit = settings.unit
    val decimals = settings.decimalPlaces

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance
            SettingsSectionCard(title = "APPEARANCE") {
                Text(text = "Theme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                            label = { Text(mode.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    label = "Dynamic Color",
                    subtitle = "Use Material You system dynamic colors (Android 12+)",
                    checked = settings.dynamicColor,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                )
            }

            // Units & Precision
            SettingsSectionCard(title = "UNITS & PRECISION") {
                Text(text = "Default Length Unit", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LengthUnit.entries.forEach { u ->
                        FilterChip(
                            selected = settings.unit == u,
                            onClick = { viewModel.updateSettings(settings.copy(unit = u)) },
                            label = { Text(u.symbol) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Decimal Places: ${settings.decimalPlaces}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 1, 2, 3, 4).forEach { d ->
                        FilterChip(
                            selected = settings.decimalPlaces == d,
                            onClick = { viewModel.updateSettings(settings.copy(decimalPlaces = d)) },
                            label = { Text("$d") }
                        )
                    }
                }
            }

            // Grid & Canvas
            SettingsSectionCard(title = "GRID & CANVAS") {
                Text(text = "Grid Spacing", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Pair("10 cm", 100.0),
                        Pair("25 cm", 250.0),
                        Pair("50 cm", 500.0),
                        Pair("1 m", 1000.0)
                    ).forEach { (lbl, mm) ->
                        FilterChip(
                            selected = settings.defaultGridSpacingMm == mm,
                            onClick = { viewModel.updateSettings(settings.copy(defaultGridSpacingMm = mm)) },
                            label = { Text(lbl) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsToggleRow(
                    label = "Snap to Grid",
                    subtitle = "Snap furniture positions to nearest grid intervals while dragging",
                    checked = settings.snapToGrid,
                    onCheckedChange = { viewModel.updateSettings(settings.copy(snapToGrid = it)) }
                )
            }

            // Planning Target Presets
            SettingsSectionCard(title = "CLEARANCE PLANNING TARGETS") {
                Text(
                    text = "User planning targets — not building-code requirements.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                DimensionInputField(
                    label = "Tight Target",
                    valueMm = settings.presets.tightMm,
                    unit = unit,
                    onValueChangeMm = {
                        viewModel.updateSettings(settings.copy(presets = settings.presets.copy(tightMm = it)))
                    },
                    decimals = decimals
                )
                Spacer(modifier = Modifier.height(8.dp))
                DimensionInputField(
                    label = "Comfortable Target",
                    valueMm = settings.presets.comfortableMm,
                    unit = unit,
                    onValueChangeMm = {
                        viewModel.updateSettings(settings.copy(presets = settings.presets.copy(comfortableMm = it)))
                    },
                    decimals = decimals
                )
                Spacer(modifier = Modifier.height(8.dp))
                DimensionInputField(
                    label = "Spacious Target",
                    valueMm = settings.presets.spaciousMm,
                    unit = unit,
                    onValueChangeMm = {
                        viewModel.updateSettings(settings.copy(presets = settings.presets.copy(spaciousMm = it)))
                    },
                    decimals = decimals
                )
            }

            // Display Toggles
            SettingsSectionCard(title = "DISPLAY SETTINGS") {
                SettingsToggleRow("Show Dimensions", "Display dimension callouts on floor plan", settings.showDimensions) {
                    viewModel.updateSettings(settings.copy(showDimensions = it))
                }
                SettingsToggleRow("Show Grid", "Display architectural grid lines", settings.showGrid) {
                    viewModel.updateSettings(settings.copy(showGrid = it))
                }
                SettingsToggleRow("Show Clearance Lines", "Draw measurement arrows to edges", settings.showClearanceLines) {
                    viewModel.updateSettings(settings.copy(showClearanceLines = it))
                }
                SettingsToggleRow("Show Room Occupancy", "Calculate floor occupancy & free floor area", settings.showRoomOccupancy) {
                    viewModel.updateSettings(settings.copy(showRoomOccupancy = it))
                }
                SettingsToggleRow("Show Clearance Matrix", "Show pairwise clearance matrix table", settings.showClearanceMatrix) {
                    viewModel.updateSettings(settings.copy(showClearanceMatrix = it))
                }
                SettingsToggleRow("Show Object Labels", "Render furniture names on plan blocks", settings.showObjectLabels) {
                    viewModel.updateSettings(settings.copy(showObjectLabels = it))
                }
            }

            // Behavior Toggles
            SettingsSectionCard(title = "BEHAVIOR") {
                SettingsToggleRow("Live Calculation", "Recalculate clearances instantly on movement", settings.liveCalculation) {
                    viewModel.updateSettings(settings.copy(liveCalculation = it))
                }
                SettingsToggleRow("Confirm Delete", "Prompt before deleting plans and objects", settings.confirmDelete) {
                    viewModel.updateSettings(settings.copy(confirmDelete = it))
                }
                SettingsToggleRow("Restore Last Plan", "Auto-open the last modified plan on launch", settings.restoreLastPlan) {
                    viewModel.updateSettings(settings.copy(restoreLastPlan = it))
                }
            }

            // Reset Settings
            OutlinedButton(
                onClick = { showResetDialog = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Settings to Default", color = StatusOverlapping)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("All calculator preferences will be reset to defaults. Your saved plans will not be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
