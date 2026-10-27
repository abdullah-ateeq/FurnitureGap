package com.example.furnituregap.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.furnituregap.data.export.PlanExporter
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.ui.components.ClearanceMatrixTable
import com.example.furnituregap.ui.components.ClearanceStatusBadge
import com.example.furnituregap.ui.components.LargeMetricCard
import com.example.furnituregap.ui.components.ObjectRelationshipCard
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping
import com.example.furnituregap.ui.viewmodel.FurnitureGapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearanceAnalysisScreen(
    viewModel: FurnitureGapViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val plan by viewModel.currentPlan.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val sortOption by viewModel.clearanceSort.collectAsState()

    val unit = settings.unit
    val decimals = settings.decimalPlaces

    val pairs = remember(plan.objects) {
        FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
    }

    // Calculations & Metrics
    val validPairs = pairs.filter { !it.overlaps }
    val minPair = pairs.minByOrNull { it.distanceMm }
    val maxPair = validPairs.maxByOrNull { it.distanceMm }
    val avgGap = if (validPairs.isNotEmpty()) validPairs.map { it.distanceMm }.average() else 0.0

    val overlapCount = pairs.count { it.overlaps }
    val touchingCount = pairs.count { it.status == ClearanceStatus.TOUCHING }
    val belowTargetCount = pairs.count { it.status == ClearanceStatus.BELOW_TARGET }

    val belowTargetList = pairs.filter { it.status == ClearanceStatus.BELOW_TARGET || it.overlaps }

    // Sorted pairs
    val sortedPairs = remember(pairs, sortOption) {
        when (sortOption) {
            FurnitureGapViewModel.ClearanceSort.SMALLEST_GAP -> pairs.sortedBy { it.distanceMm }
            FurnitureGapViewModel.ClearanceSort.LARGEST_GAP -> pairs.sortedByDescending { it.distanceMm }
            FurnitureGapViewModel.ClearanceSort.BELOW_TARGET -> pairs.sortedBy {
                when (it.status) {
                    ClearanceStatus.OVERLAPPING -> 0
                    ClearanceStatus.BELOW_TARGET -> 1
                    ClearanceStatus.TOUCHING -> 2
                    ClearanceStatus.ADEQUATE -> 3
                }
            }
            FurnitureGapViewModel.ClearanceSort.OBJECT_NAME -> pairs.sortedBy { it.objectAName }
        }
    }

    // Export storage access framework launchers
    var pendingExportType by remember { mutableStateOf("txt") }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            when (pendingExportType) {
                "csv" -> "text/csv"
                "json" -> "application/json"
                else -> "text/plain"
            }
        )
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val content = when (pendingExportType) {
                        "csv" -> PlanExporter.generateCsv(plan, unit, decimals)
                        "json" -> PlanExporter.generateJson(plan)
                        else -> PlanExporter.generateTxt(plan, unit, decimals)
                    }
                    os.write(content.toByteArray())
                }
                Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showExportMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clearance Analysis", fontWeight = FontWeight.Bold) },
                actions = {
                    // Sort Menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            FurnitureGapViewModel.ClearanceSort.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.label) },
                                    onClick = {
                                        viewModel.setClearanceSort(s)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Share Intent
                    IconButton(onClick = {
                        val shareText = PlanExporter.generateShareText(plan, unit, decimals)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "FurnitureGap Clearance: ${plan.name}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Clearance Analysis"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    // Export Menu
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as TXT") },
                                onClick = {
                                    pendingExportType = "txt"
                                    showExportMenu = false
                                    exportLauncher.launch("${plan.name.replace(" ", "_")}_clearance.txt")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as CSV") },
                                onClick = {
                                    pendingExportType = "csv"
                                    showExportMenu = false
                                    exportLauncher.launch("${plan.name.replace(" ", "_")}_clearance.csv")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as JSON") },
                                onClick = {
                                    pendingExportType = "json"
                                    showExportMenu = false
                                    exportLauncher.launch("${plan.name.replace(" ", "_")}_plan.json")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Large Architectural Header Result Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FURNITUREGAP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MINIMUM GAP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (minPair != null) {
                            if (minPair.overlaps) "OVERLAP" else unit.format(minPair.distanceMm, decimals)
                        } else "N/A",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = when (minPair?.status) {
                            ClearanceStatus.BELOW_TARGET -> StatusBelowTarget
                            ClearanceStatus.OVERLAPPING -> StatusOverlapping
                            ClearanceStatus.ADEQUATE -> StatusAdequate
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (minPair != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${minPair.objectAName} ↔ ${minPair.objectBName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Required: ${unit.format(minPair.requiredClearanceMm, decimals)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ClearanceStatusBadge(status = minPair.status)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clearance Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLEARANCE SUMMARY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryStatItem("Minimum Gap", minPair?.let { unit.format(it.distanceMm, decimals) } ?: "—")
                        SummaryStatItem("Maximum Gap", maxPair?.let { unit.format(it.distanceMm, decimals) } ?: "—")
                        SummaryStatItem("Average Gap", if (validPairs.isNotEmpty()) unit.format(avgGap, decimals) else "—")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryStatItem("Overlaps", "$overlapCount", isAlert = overlapCount > 0)
                        SummaryStatItem("Touching Objects", "$touchingCount")
                        SummaryStatItem("Below Target", "$belowTargetCount", isAlert = belowTargetCount > 0)
                    }
                }
            }

            // Attention Needed Card (Dedicated list of below-target and overlapping pieces)
            if (belowTargetList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusBelowTarget.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusBelowTarget)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATTENTION NEEDED (${belowTargetList.size})",
                                fontWeight = FontWeight.Bold,
                                color = StatusBelowTarget
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        belowTargetList.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${p.objectAName} ↔ ${p.objectBName}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (p.overlaps) "OVERLAP" else "${unit.format(p.distanceMm, decimals)} / ${unit.format(p.requiredClearanceMm, decimals)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (p.overlaps) StatusOverlapping else StatusBelowTarget
                                )
                            }
                        }
                    }
                }
            }

            // Clearance Matrix Table
            if (settings.showClearanceMatrix && plan.objects.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))
                ClearanceMatrixTable(
                    objects = plan.objects,
                    pairs = pairs,
                    unit = unit,
                    decimals = decimals,
                    onSelectPair = { pair ->
                        viewModel.selectPair(pair)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clearance Object Pairs List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OBJECT PAIRS (${pairs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sorted by: ${sortOption.label}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (sortedPairs.isEmpty()) {
                Text(
                    text = "Add at least two pieces of furniture to analyze pairwise clearances.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                sortedPairs.forEach { pair ->
                    ObjectRelationshipCard(
                        pair = pair,
                        unit = unit,
                        decimals = decimals,
                        onSelect = {
                            viewModel.selectPair(Pair(pair.objectAId, pair.objectBId))
                            viewModel.selectObject(pair.objectAId)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryStatItem(
    title: String,
    value: String,
    isAlert: Boolean = false
) {
    Column {
        Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isAlert) StatusOverlapping else MaterialTheme.colorScheme.onSurface
        )
    }
}
