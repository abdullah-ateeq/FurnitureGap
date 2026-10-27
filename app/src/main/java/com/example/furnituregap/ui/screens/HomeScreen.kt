package com.example.furnituregap.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.Rect
import com.example.furnituregap.data.model.Window
import com.example.furnituregap.ui.components.ClearanceStatusBadge
import com.example.furnituregap.ui.components.DimensionInputField
import com.example.furnituregap.ui.components.FloorPlanCanvas
import com.example.furnituregap.ui.components.LargeMetricCard
import com.example.furnituregap.ui.components.WallClearanceDisplayCard
import com.example.furnituregap.ui.theme.BlueprintPrimary
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping
import com.example.furnituregap.ui.viewmodel.CalculationMode
import com.example.furnituregap.ui.viewmodel.FurnitureGapViewModel
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: FurnitureGapViewModel,
    onNavigateToFloorPlan: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val settings by viewModel.settings.collectAsState()
    val calcMode by viewModel.calculationMode.collectAsState()
    val wallState by viewModel.wallCalcState.collectAsState()
    val pairState by viewModel.pairCalcState.collectAsState()
    val walkwayState by viewModel.walkwayCalcState.collectAsState()
    val doorWindowState by viewModel.doorWindowCalcState.collectAsState()
    val currentPlan by viewModel.currentPlan.collectAsState()

    val unit = settings.unit
    val decimals = settings.decimalPlaces

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "FurnitureGap",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Measure the space between furniture and objects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Unit Selector Dropdown
            var unitMenuExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { unitMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "[ ${unit.symbol} ▼ ]",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    LengthUnit.entries.forEach { u ->
                        DropdownMenuItem(
                            text = { Text("${u.label} (${u.symbol})") },
                            onClick = {
                                viewModel.updateSettings(settings.copy(unit = u))
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculation Mode Tabs
        Text(
            text = "CALCULATION MODE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        ScrollableTabRow(
            selectedTabIndex = calcMode.ordinal,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            CalculationMode.entries.forEach { mode ->
                Tab(
                    selected = calcMode == mode,
                    onClick = { viewModel.setCalculationMode(mode) },
                    text = {
                        Text(
                            text = mode.title,
                            fontWeight = if (calcMode == mode) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (calcMode) {
            CalculationMode.FURNITURE_TO_WALL -> {
                FurnitureToWallModeView(
                    viewModel = viewModel,
                    unit = unit,
                    decimals = decimals,
                    onSaveHistory = { title, sub, cl, req, st, det ->
                        viewModel.saveCurrentCalculationToHistory(title, sub, "Furniture → Wall", cl, req, st, det)
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            CalculationMode.FURNITURE_TO_FURNITURE -> {
                FurnitureToFurnitureModeView(
                    viewModel = viewModel,
                    unit = unit,
                    decimals = decimals,
                    onSaveHistory = { title, sub, cl, req, st, det ->
                        viewModel.saveCurrentCalculationToHistory(title, sub, "Furniture → Furniture", cl, req, st, det)
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            CalculationMode.FURNITURE_TO_OBJECT -> {
                FurnitureToObjectModeView(
                    viewModel = viewModel,
                    unit = unit,
                    decimals = decimals,
                    onSaveHistory = { title, sub, cl, req, st, det ->
                        viewModel.saveCurrentCalculationToHistory(title, sub, "Furniture → Object", cl, req, st, det)
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            CalculationMode.ROOM_LAYOUT -> {
                RoomLayoutSummaryModeView(
                    viewModel = viewModel,
                    unit = unit,
                    decimals = decimals,
                    onNavigateToFloorPlan = onNavigateToFloorPlan,
                    onNavigateToAnalysis = onNavigateToAnalysis
                )
            }
            CalculationMode.WALKWAY -> {
                WalkwayModeView(
                    viewModel = viewModel,
                    unit = unit,
                    decimals = decimals,
                    onSaveHistory = { title, sub, cl, req, st, det ->
                        viewModel.saveCurrentCalculationToHistory(title, sub, "Walkway", cl, req, st, det)
                        Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. FURNITURE TO WALL MODE VIEW
// -------------------------------------------------------------
@Composable
private fun FurnitureToWallModeView(
    viewModel: FurnitureGapViewModel,
    unit: LengthUnit,
    decimals: Int,
    onSaveHistory: (String, String, Double, Double, ClearanceStatus, String) -> Unit
) {
    val state by viewModel.wallCalcState.collectAsState()

    val furnitureRect = Rect(
        leftMm = state.leftOffsetMm,
        topMm = state.frontOffsetMm,
        widthMm = state.furnitureWidthMm,
        depthMm = state.furnitureDepthMm
    )

    val clearances = FurnitureGapCalculator.calculateWallClearances(
        roomWidthMm = state.roomWidthMm,
        roomDepthMm = state.roomDepthMm,
        furniture = furnitureRect
    )

    val boundaryCheck = FurnitureGapCalculator.checkRoomBoundaries(
        roomWidthMm = state.roomWidthMm,
        roomDepthMm = state.roomDepthMm,
        furniture = furnitureRect
    )

    val minClearance = clearances.minimumClearanceMm
    val margin = minClearance - state.requiredClearanceMm

    val clearanceStatus = when {
        boundaryCheck.isOutside -> ClearanceStatus.OVERLAPPING
        minClearance < 0 -> ClearanceStatus.OVERLAPPING
        minClearance == 0.0 -> ClearanceStatus.TOUCHING
        minClearance >= state.requiredClearanceMm -> ClearanceStatus.ADEQUATE
        else -> ClearanceStatus.BELOW_TARGET
    }

    // Boundary warning banner if outside
    if (boundaryCheck.isOutside) {
        Card(
            colors = CardDefaults.cardColors(containerColor = StatusOverlapping.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOverlapping)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "⚠ FURNITURE EXCEEDS AVAILABLE SPACE",
                        fontWeight = FontWeight.Bold,
                        color = StatusOverlapping
                    )
                    Text(
                        text = boundaryCheck.summary(unit, decimals),
                        fontSize = 12.sp,
                        color = StatusOverlapping
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Primary Clearance Result Card
    LargeMetricCard(
        title = "MINIMUM CLEARANCE",
        value = unit.format(minClearance, decimals),
        subtitle = "Tightest: ${clearances.tightestSide()}",
        status = clearanceStatus,
        marginText = "Target: ${unit.format(state.requiredClearanceMm, decimals)} (${if (margin >= 0) "+${unit.format(margin, decimals)}" else unit.format(margin, decimals)})"
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Wall clearances detail card
    WallClearanceDisplayCard(
        clearances = clearances,
        unit = unit,
        decimals = decimals,
        requiredTargetMm = state.requiredClearanceMm
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Inputs: Furniture
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "FURNITURE DIMENSIONS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Width",
                    valueMm = state.furnitureWidthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(furnitureWidthMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Depth",
                    valueMm = state.furnitureDepthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(furnitureDepthMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Height",
                    valueMm = state.furnitureHeightMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(furnitureHeightMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Inputs: Room
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ROOM / WALL DIMENSIONS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Room Width",
                    valueMm = state.roomWidthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(roomWidthMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Room Depth",
                    valueMm = state.roomDepthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(roomDepthMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Inputs: Furniture Position
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "FURNITURE POSITION", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Distance from Left Wall",
                    valueMm = state.leftOffsetMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(leftOffsetMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Distance from Front Wall",
                    valueMm = state.frontOffsetMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(frontOffsetMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Planning Target Input
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "REQUIRED CLEARANCE (PLANNING TARGET)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "User planning targets — not building-code requirements.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DimensionInputField(
                    label = "Target Clearance",
                    valueMm = state.requiredClearanceMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updateWallCalculator(requiredClearanceMm = it) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                Button(
                    onClick = {
                        onSaveHistory(
                            "Wall Clearance",
                            "Furniture ↔ Walls",
                            minClearance,
                            state.requiredClearanceMm,
                            clearanceStatus,
                            "Left: ${unit.format(clearances.leftMm, decimals)}, Right: ${unit.format(clearances.rightMm, decimals)}, Top: ${unit.format(clearances.topMm, decimals)}, Bottom: ${unit.format(clearances.bottomMm, decimals)}"
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. FURNITURE TO FURNITURE MODE VIEW
// -------------------------------------------------------------
@Composable
private fun FurnitureToFurnitureModeView(
    viewModel: FurnitureGapViewModel,
    unit: LengthUnit,
    decimals: Int,
    onSaveHistory: (String, String, Double, Double, ClearanceStatus, String) -> Unit
) {
    val state by viewModel.pairCalcState.collectAsState()
    val objA = state.objectA
    val objB = state.objectB

    val pairResult = FurnitureGapCalculator.analyzePair(objA, objB)

    // Overlap Warning Card
    if (pairResult.overlaps) {
        Card(
            colors = CardDefaults.cardColors(containerColor = StatusOverlapping.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOverlapping)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚠ OVERLAP DETECTED",
                        fontWeight = FontWeight.Bold,
                        color = StatusOverlapping
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Overlap width: ${unit.format(pairResult.overlapWidthMm, decimals)}\nOverlap depth: ${unit.format(pairResult.overlapDepthMm, decimals)}",
                    fontSize = 13.sp,
                    color = StatusOverlapping
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Large Metric Result
    val marginSign = if (pairResult.marginMm >= 0) "+" else ""
    LargeMetricCard(
        title = "CLEARANCE (${pairResult.direction.label.uppercase()})",
        value = if (pairResult.overlaps) "OVERLAP" else unit.format(pairResult.distanceMm, decimals),
        subtitle = "${objA.name} ↔ ${objB.name}",
        status = pairResult.status,
        marginText = if (pairResult.overlaps) "Objects Intersect" else "Target: ${unit.format(pairResult.requiredClearanceMm, decimals)} ($marginSign${unit.format(pairResult.marginMm, decimals)})"
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Mini Floor Plan Representation
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "INTERACTIVE VISUALIZATION (DRAG OR NUDGE)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                val dummyPlan = com.example.furnituregap.data.model.FloorPlan(
                    name = "Pair View",
                    roomWidthMm = 3500.0,
                    roomDepthMm = 3500.0,
                    objects = listOf(objA, objB)
                )
                FloorPlanCanvas(
                    plan = dummyPlan,
                    settings = viewModel.settings.collectAsState().value,
                    selectedObjectId = objB.id,
                    selectedPair = Pair(objA.id, objB.id),
                    onSelectObject = {},
                    onMoveObject = { id, x, y ->
                        if (id == objA.id) {
                            viewModel.updatePairCalculator(objectA = objA.copy(xMm = x, yMm = y))
                        } else {
                            viewModel.updatePairCalculator(objectB = objB.copy(xMm = x, yMm = y))
                        }
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Furniture A Editor
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "FURNITURE A: ${objA.name}", fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.rotateObjectA() }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Width",
                    valueMm = objA.widthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectA = objA.copy(widthMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Depth",
                    valueMm = objA.depthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectA = objA.copy(depthMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Position X",
                    valueMm = objA.xMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectA = objA.copy(xMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Position Y",
                    valueMm = objA.yMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectA = objA.copy(yMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Furniture B Editor
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "FURNITURE B: ${objB.name}", fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.rotateObjectB() }) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Width",
                    valueMm = objB.widthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectB = objB.copy(widthMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Depth",
                    valueMm = objB.depthMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectB = objB.copy(depthMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DimensionInputField(
                    label = "Position X",
                    valueMm = objB.xMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectB = objB.copy(xMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
                DimensionInputField(
                    label = "Position Y",
                    valueMm = objB.yMm,
                    unit = unit,
                    onValueChangeMm = { viewModel.updatePairCalculator(objectB = objB.copy(yMm = it)) },
                    modifier = Modifier.weight(1f),
                    decimals = decimals
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Target Clearance & Save
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DimensionInputField(
                label = "Target Clearance",
                valueMm = objA.requiredClearanceMm,
                unit = unit,
                onValueChangeMm = {
                    viewModel.updatePairCalculator(
                        objectA = objA.copy(requiredClearanceMm = it),
                        objectB = objB.copy(requiredClearanceMm = it)
                    )
                },
                modifier = Modifier.weight(1f),
                decimals = decimals
            )
            Button(
                onClick = {
                    onSaveHistory(
                        "${objA.name} ↔ ${objB.name}",
                        "Pair Gap: ${pairResult.direction.label}",
                        pairResult.distanceMm,
                        pairResult.requiredClearanceMm,
                        pairResult.status,
                        "Edge-to-edge distance: ${unit.format(pairResult.distanceMm, decimals)}"
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }
        }
    }
}

// -------------------------------------------------------------
// 3. FURNITURE TO OBJECT (DOOR / WINDOW) MODE VIEW
// -------------------------------------------------------------
@Composable
private fun FurnitureToObjectModeView(
    viewModel: FurnitureGapViewModel,
    unit: LengthUnit,
    decimals: Int,
    onSaveHistory: (String, String, Double, Double, ClearanceStatus, String) -> Unit
) {
    val state by viewModel.doorWindowCalcState.collectAsState()

    // Door check
    val door = Door(
        hingeXmm = state.doorHingeXmm,
        hingeYmm = state.doorHingeYmm,
        widthMm = state.doorWidthMm,
        swingDegrees = state.doorSwingDegrees,
        rotation = 0.0
    )
    val doorCheck = FurnitureGapCalculator.checkDoorSwingInterference(door, state.furnitureNearDoor)
    val passageClear = state.doorWidthMm >= state.furniturePassageWidthMm
    val passageRemaining = state.doorWidthMm - state.furniturePassageWidthMm

    // Window check
    val window = Window(
        xMm = 500.0,
        yMm = 0.0,
        widthMm = state.windowWidthMm,
        sillHeightMm = state.windowSillHeightMm
    )
    val (winDistance, sillDiff) = FurnitureGapCalculator.evaluateWindowClearance(window, state.furnitureNearWindow)

    // Door Section
    Text(
        text = "DOOR CLEARANCE & PASSAGE",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "DOOR SWING STATUS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (doorCheck.intersects) "⚠ DOOR SWING BLOCKED" else "✓ DOOR SWING CLEAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (doorCheck.intersects) StatusOverlapping else StatusAdequate
                    )
                }
                ClearanceStatusBadge(
                    status = if (doorCheck.intersects) ClearanceStatus.OVERLAPPING else ClearanceStatus.ADEQUATE,
                    customText = if (doorCheck.intersects) "BLOCKED" else "CLEAR"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (doorCheck.intersects) "Furniture intersects the door's swing arc." else "Door can open fully without obstruction.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Passage check
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "DOOR OPENING", fontSize = 11.sp)
                    Text(text = unit.format(state.doorWidthMm, decimals), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(text = "PASSAGE WIDTH", fontSize = 11.sp)
                    Text(text = unit.format(state.furniturePassageWidthMm, decimals), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(text = "PASSAGE EXTRA", fontSize = 11.sp)
                    Text(
                        text = (if (passageRemaining >= 0) "+" else "") + unit.format(passageRemaining, decimals),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (passageClear) StatusAdequate else StatusBelowTarget
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Door Inputs
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DimensionInputField(
            label = "Door Width",
            valueMm = state.doorWidthMm,
            unit = unit,
            onValueChangeMm = { viewModel.updateDoorWindowCalculator(doorWidthMm = it) },
            modifier = Modifier.weight(1f),
            decimals = decimals
        )
        DimensionInputField(
            label = "Furniture / Passage",
            valueMm = state.furniturePassageWidthMm,
            unit = unit,
            onValueChangeMm = { viewModel.updateDoorWindowCalculator(furniturePassageWidthMm = it) },
            modifier = Modifier.weight(1f),
            decimals = decimals
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Window Section
    Text(
        text = "WINDOW & SILL CLEARANCE",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "FURNITURE TOP", fontSize = 11.sp)
                    Text(text = unit.format(state.furnitureNearWindow.heightMm, decimals), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(text = "WINDOW SILL", fontSize = 11.sp)
                    Text(text = unit.format(state.windowSillHeightMm, decimals), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(text = "HEIGHT DIFFERENCE", fontSize = 11.sp)
                    val diffSign = if (sillDiff >= 0) "+" else ""
                    Text(
                        text = "$diffSign${unit.format(sillDiff, decimals)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (sillDiff <= 0) StatusAdequate else StatusBelowTarget
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sillDiff <= 0) "Furniture is ${unit.format(abs(sillDiff), decimals)} lower than window sill."
                else "Furniture exceeds window sill height by ${unit.format(sillDiff, decimals)}.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Window Inputs
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DimensionInputField(
            label = "Window Width",
            valueMm = state.windowWidthMm,
            unit = unit,
            onValueChangeMm = { viewModel.updateDoorWindowCalculator(windowWidthMm = it) },
            modifier = Modifier.weight(1f),
            decimals = decimals
        )
        DimensionInputField(
            label = "Window Sill Height",
            valueMm = state.windowSillHeightMm,
            unit = unit,
            onValueChangeMm = { viewModel.updateDoorWindowCalculator(windowSillHeightMm = it) },
            modifier = Modifier.weight(1f),
            decimals = decimals
        )
    }
}

// -------------------------------------------------------------
// 4. WALKWAY MODE VIEW
// -------------------------------------------------------------
@Composable
private fun WalkwayModeView(
    viewModel: FurnitureGapViewModel,
    unit: LengthUnit,
    decimals: Int,
    onSaveHistory: (String, String, Double, Double, ClearanceStatus, String) -> Unit
) {
    val state by viewModel.walkwayCalcState.collectAsState()
    val walkwayResult = FurnitureGapCalculator.evaluateWalkway(state.availableWidthMm, state.requiredWidthMm)

    val margin = walkwayResult.remainingWidthMm
    val status = if (walkwayResult.fits) ClearanceStatus.ADEQUATE else ClearanceStatus.BELOW_TARGET

    LargeMetricCard(
        title = "WALKWAY CLEARANCE",
        value = unit.format(state.availableWidthMm, decimals),
        subtitle = if (walkwayResult.fits) "✓ WALKWAY FITS" else "⚠ WALKWAY TOO NARROW",
        status = status,
        marginText = if (walkwayResult.fits) "Extra: +${unit.format(margin, decimals)}" else "Short by: ${unit.format(abs(margin), decimals)}"
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "WALKWAY REQUIREMENTS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))
            DimensionInputField(
                label = "Available Width (Gap between Objects/Walls)",
                valueMm = state.availableWidthMm,
                unit = unit,
                onValueChangeMm = { viewModel.updateWalkwayCalculator(availableWidthMm = it, requiredWidthMm = state.requiredWidthMm) },
                decimals = decimals
            )
            Spacer(modifier = Modifier.height(8.dp))
            DimensionInputField(
                label = "Required Walkway Width (Standard ~90 cm)",
                valueMm = state.requiredWidthMm,
                unit = unit,
                onValueChangeMm = { viewModel.updateWalkwayCalculator(availableWidthMm = state.availableWidthMm, requiredWidthMm = it) },
                decimals = decimals
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onSaveHistory(
                        "Walkway Check",
                        "Width: ${unit.format(state.availableWidthMm, decimals)}",
                        state.availableWidthMm,
                        state.requiredWidthMm,
                        status,
                        if (walkwayResult.fits) "Passage fits with +${unit.format(margin, decimals)} extra" else "Passage short by ${unit.format(abs(margin), decimals)}"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Walkway Calculation")
            }
        }
    }
}

// -------------------------------------------------------------
// 5. ROOM LAYOUT SUMMARY MODE VIEW
// -------------------------------------------------------------
@Composable
private fun RoomLayoutSummaryModeView(
    viewModel: FurnitureGapViewModel,
    unit: LengthUnit,
    decimals: Int,
    onNavigateToFloorPlan: () -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    val plan by viewModel.currentPlan.collectAsState()
    val (occupancyPercent, freeArea) = FurnitureGapCalculator.calculateRoomOccupancy(
        roomWidthMm = plan.roomWidthMm,
        roomDepthMm = plan.roomDepthMm,
        objects = plan.objects
    )
    val pairs = FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
    val minGap = pairs.minByOrNull { it.distanceMm }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = plan.name.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Room: ${unit.format(plan.roomWidthMm, decimals)} × ${unit.format(plan.roomDepthMm, decimals)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "OBJECTS", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${plan.objects.size} pieces", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column {
                    Text(text = "FLOOR OCCUPANCY", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "%.1f%%".format(occupancyPercent), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column {
                    Text(text = "FREE FLOOR AREA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // In square meters (1 m² = 1,000,000 mm²)
                    val freeM2 = freeArea / 1_000_000.0
                    Text(text = "%.1f m²".format(freeM2), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (minGap != null) {
                Text(
                    text = "MINIMUM GAP: ${unit.format(minGap.distanceMm, decimals)} (${minGap.objectAName} ↔ ${minGap.objectBName})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (minGap.status == ClearanceStatus.BELOW_TARGET) StatusBelowTarget else StatusAdequate
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToFloorPlan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Floor Plan")
                }
                OutlinedButton(
                    onClick = onNavigateToAnalysis,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clearance Analysis")
                }
            }
        }
    }
}
