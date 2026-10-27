package com.example.furnituregap.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.FurniturePresets
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.Window
import com.example.furnituregap.ui.components.ClearanceStatusBadge
import com.example.furnituregap.ui.components.DimensionInputField
import com.example.furnituregap.ui.components.FloorPlanCanvas
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping
import com.example.furnituregap.ui.viewmodel.FurnitureGapViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorPlanScreen(
    viewModel: FurnitureGapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val plan by viewModel.currentPlan.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedId by viewModel.selectedObjectId.collectAsState()
    val selectedPair by viewModel.selectedPair.collectAsState()

    val unit = settings.unit
    val decimals = settings.decimalPlaces

    var showAddFurnitureDialog by remember { mutableStateOf(false) }
    var showRoomDimDialog by remember { mutableStateOf(false) }
    var showSavePlanDialog by remember { mutableStateOf(false) }

    val selectedObj = plan.objects.find { it.id == selectedId }

    // Live occupancy calculation
    val (occupancyPercent, freeArea) = FurnitureGapCalculator.calculateRoomOccupancy(
        roomWidthMm = plan.roomWidthMm,
        roomDepthMm = plan.roomDepthMm,
        objects = plan.objects
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = plan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "Room: ${unit.format(plan.roomWidthMm, decimals)} × ${unit.format(plan.roomDepthMm, decimals)} • Occupancy: ${"%.1f".format(occupancyPercent)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Room dimensions button
                    IconButton(onClick = { showRoomDimDialog = true }) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = "Room Dimensions")
                    }
                    // Save plan button
                    IconButton(onClick = { showSavePlanDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Plan")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFurnitureDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Furniture")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Control Bar: Grid Toggle, Snap Toggle, Object Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = settings.showGrid,
                        onClick = { viewModel.updateSettings(settings.copy(showGrid = !settings.showGrid)) },
                        label = { Text("Grid", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.snapToGrid,
                        onClick = { viewModel.updateSettings(settings.copy(snapToGrid = !settings.snapToGrid)) },
                        label = { Text("Snap (${unit.format(settings.defaultGridSpacingMm, 0)})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = settings.showClearanceLines,
                        onClick = { viewModel.updateSettings(settings.copy(showClearanceLines = !settings.showClearanceLines)) },
                        label = { Text("Dimension Lines", fontSize = 12.sp) }
                    )
                }
                Text(
                    text = "${plan.objects.size} Objects",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Canvas Floor Plan Area (flexibly weighted)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                FloorPlanCanvas(
                    plan = plan,
                    settings = settings,
                    selectedObjectId = selectedId,
                    selectedPair = selectedPair,
                    onSelectObject = { viewModel.selectObject(it) },
                    onMoveObject = { id, x, y -> viewModel.moveFurniture(id, x, y) },
                    modifier = Modifier.fillMaxSize(),
                    isDarkTheme = isDark
                )
            }

            // Bottom Selected Object Control Panel
            if (selectedObj != null) {
                val wallClearances = FurnitureGapCalculator.calculateWallClearances(
                    roomWidthMm = plan.roomWidthMm,
                    roomDepthMm = plan.roomDepthMm,
                    furniture = selectedObj.toEffectiveRect()
                )
                val minWallClearance = wallClearances.minimumClearanceMm

                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedObj.name} (${selectedObj.normalizedRotation}°)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Effective: ${unit.format(selectedObj.effectiveWidthMm, decimals)} × ${unit.format(selectedObj.effectiveDepthMm, decimals)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rotate 90° button
                                IconButton(onClick = { viewModel.rotateFurniture(selectedObj.id) }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90°")
                                }
                                // Delete button
                                IconButton(onClick = { viewModel.removeFurniture(selectedObj.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = StatusOverlapping)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Wall Clearance summary for selected piece
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wall Clearance: L: ${unit.format(wallClearances.leftMm, decimals)} | R: ${unit.format(wallClearances.rightMm, decimals)} | T: ${unit.format(wallClearances.topMm, decimals)} | B: ${unit.format(wallClearances.bottomMm, decimals)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val isAdequate = minWallClearance >= selectedObj.requiredClearanceMm
                            ClearanceStatusBadge(
                                status = if (minWallClearance < 0) ClearanceStatus.OVERLAPPING
                                else if (isAdequate) ClearanceStatus.ADEQUATE else ClearanceStatus.BELOW_TARGET
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Numeric Coordinates and Nudge controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DimensionInputField(
                                label = "X",
                                valueMm = selectedObj.xMm,
                                unit = unit,
                                onValueChangeMm = { viewModel.moveFurniture(selectedObj.id, it, selectedObj.yMm, snap = false) },
                                modifier = Modifier.weight(1f),
                                decimals = decimals
                            )
                            DimensionInputField(
                                label = "Y",
                                valueMm = selectedObj.yMm,
                                unit = unit,
                                onValueChangeMm = { viewModel.moveFurniture(selectedObj.id, selectedObj.xMm, it, snap = false) },
                                modifier = Modifier.weight(1f),
                                decimals = decimals
                            )

                            // Nudge buttons (moves by 5 cm = 50 mm)
                            val nudgeStep = 50.0
                            Row {
                                IconButton(onClick = {
                                    viewModel.moveFurniture(selectedObj.id, max(0.0, selectedObj.xMm - nudgeStep), selectedObj.yMm, snap = false)
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Left", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    viewModel.moveFurniture(selectedObj.id, selectedObj.xMm + nudgeStep, selectedObj.yMm, snap = false)
                                }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Right", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    viewModel.moveFurniture(selectedObj.id, selectedObj.xMm, max(0.0, selectedObj.yMm - nudgeStep), snap = false)
                                }) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Up", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    viewModel.moveFurniture(selectedObj.id, selectedObj.xMm, selectedObj.yMm + nudgeStep, snap = false)
                                }) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Furniture Dialog (with preset catalog & custom object tabs)
    if (showAddFurnitureDialog) {
        AddFurnitureModal(
            unit = unit,
            decimals = decimals,
            onDismiss = { showAddFurnitureDialog = false },
            onAddFurniture = { newObj ->
                viewModel.addFurniture(newObj)
                showAddFurnitureDialog = false
            }
        )
    }

    // Room Dimensions Dialog
    if (showRoomDimDialog) {
        var roomW by remember { mutableStateOf(plan.roomWidthMm) }
        var roomD by remember { mutableStateOf(plan.roomDepthMm) }
        AlertDialog(
            onDismissRequest = { showRoomDimDialog = false },
            title = { Text("Edit Room Dimensions") },
            text = {
                Column {
                    DimensionInputField(
                        label = "Room Width",
                        valueMm = roomW,
                        unit = unit,
                        onValueChangeMm = { roomW = it },
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    DimensionInputField(
                        label = "Room Depth",
                        valueMm = roomD,
                        unit = unit,
                        onValueChangeMm = { roomD = it },
                        decimals = decimals
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateRoomDimensions(roomW, roomD)
                    showRoomDimDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoomDimDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Save Plan Dialog
    if (showSavePlanDialog) {
        var planName by remember { mutableStateOf(plan.name) }
        AlertDialog(
            onDismissRequest = { showSavePlanDialog = false },
            title = { Text("Save Floor Plan") },
            text = {
                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveCurrentPlan(planName)
                    showSavePlanDialog = false
                    Toast.makeText(context, "Plan Saved", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AddFurnitureModal(
    unit: LengthUnit,
    decimals: Int,
    onDismiss: () -> Unit,
    onAddFurniture: (FurnitureObject) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Living Room") }
    var isCustom by remember { mutableStateOf(false) }

    // Custom object fields
    var customName by remember { mutableStateOf("Custom Item") }
    var customWidthMm by remember { mutableStateOf(1000.0) }
    var customDepthMm by remember { mutableStateOf(800.0) }
    var customHeightMm by remember { mutableStateOf(850.0) }
    var customTargetMm by remember { mutableStateOf(600.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isCustom) "Create Custom Object" else "Add Furniture Preset") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isCustom,
                        onClick = { isCustom = false },
                        label = { Text("Presets") }
                    )
                    FilterChip(
                        selected = isCustom,
                        onClick = { isCustom = true },
                        label = { Text("Custom Object") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isCustom) {
                    // Category selector chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FurniturePresets.all.keys.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Preset list in selected category
                    val items = FurniturePresets.all[selectedCategory] ?: emptyList()
                    items.forEach { preset ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = preset.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "${unit.format(preset.widthMm, decimals)} × ${unit.format(preset.depthMm, decimals)} (H: ${unit.format(preset.heightMm, decimals)})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = { onAddFurniture(preset.toFurnitureObject(xMm = 500.0, yMm = 500.0)) },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Custom Object Form
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionInputField(
                        label = "Width",
                        valueMm = customWidthMm,
                        unit = unit,
                        onValueChangeMm = { customWidthMm = it },
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionInputField(
                        label = "Depth",
                        valueMm = customDepthMm,
                        unit = unit,
                        onValueChangeMm = { customDepthMm = it },
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionInputField(
                        label = "Height",
                        valueMm = customHeightMm,
                        unit = unit,
                        onValueChangeMm = { customHeightMm = it },
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DimensionInputField(
                        label = "Target Clearance",
                        valueMm = customTargetMm,
                        unit = unit,
                        onValueChangeMm = { customTargetMm = it },
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val newObj = FurnitureObject(
                                name = customName.ifBlank { "Custom Object" },
                                category = "Custom",
                                widthMm = max(100.0, customWidthMm),
                                depthMm = max(100.0, customDepthMm),
                                heightMm = max(0.0, customHeightMm),
                                xMm = 500.0,
                                yMm = 500.0,
                                requiredClearanceMm = customTargetMm
                            )
                            onAddFurniture(newObj)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Floor Plan")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
