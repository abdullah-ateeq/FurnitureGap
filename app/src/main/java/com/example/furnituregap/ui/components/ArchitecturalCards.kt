package com.example.furnituregap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.WallClearances
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping

@Composable
fun LargeMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    status: ClearanceStatus? = null,
    marginText: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                if (status != null) {
                    ClearanceStatusBadge(status = status)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = when (status) {
                    ClearanceStatus.BELOW_TARGET -> StatusBelowTarget
                    ClearanceStatus.OVERLAPPING -> StatusOverlapping
                    ClearanceStatus.ADEQUATE -> StatusAdequate
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (subtitle != null || marginText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (marginText != null) {
                        Text(
                            text = marginText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (marginText.startsWith("-")) StatusBelowTarget else StatusAdequate
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WallClearanceDisplayCard(
    clearances: WallClearances,
    unit: LengthUnit,
    decimals: Int,
    requiredTargetMm: Double,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "WALL CLEARANCES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WallSideItem(label = "LEFT", valueMm = clearances.leftMm, unit = unit, decimals = decimals, reqMm = requiredTargetMm)
                WallSideItem(label = "RIGHT", valueMm = clearances.rightMm, unit = unit, decimals = decimals, reqMm = requiredTargetMm)
                WallSideItem(label = "FRONT / TOP", valueMm = clearances.topMm, unit = unit, decimals = decimals, reqMm = requiredTargetMm)
                WallSideItem(label = "BACK / BOTTOM", valueMm = clearances.bottomMm, unit = unit, decimals = decimals, reqMm = requiredTargetMm)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = DividerDefaults.color.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MINIMUM CLEARANCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = unit.format(clearances.minimumClearanceMm, decimals),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tightest: ${clearances.tightestSide()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val minStatus = if (clearances.minimumClearanceMm >= requiredTargetMm) {
                    ClearanceStatus.ADEQUATE
                } else if (clearances.minimumClearanceMm == 0.0) {
                    ClearanceStatus.TOUCHING
                } else {
                    ClearanceStatus.BELOW_TARGET
                }
                ClearanceStatusBadge(status = minStatus)
            }
        }
    }
}

@Composable
private fun WallSideItem(
    label: String,
    valueMm: Double,
    unit: LengthUnit,
    decimals: Int,
    reqMm: Double
) {
    val isBelow = valueMm < reqMm
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = unit.format(valueMm, decimals),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isBelow) StatusBelowTarget else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ObjectRelationshipCard(
    pair: ClearanceResult,
    unit: LengthUnit,
    decimals: Int,
    onSelect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pair.objectAName} ↔ ${pair.objectBName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                ClearanceStatusBadge(status = pair.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (pair.overlaps) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusOverlapping,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Overlap width: ${unit.format(pair.overlapWidthMm, decimals)}, depth: ${unit.format(pair.overlapDepthMm, decimals)}",
                        color = StatusOverlapping,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "CLEARANCE",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = unit.format(pair.distanceMm, decimals),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Text(
                            text = "DIRECTION",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pair.direction.label.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "REQUIRED",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = unit.format(pair.requiredClearanceMm, decimals),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text(
                            text = "MARGIN",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val sign = if (pair.marginMm >= 0) "+" else ""
                        Text(
                            text = "$sign${unit.format(pair.marginMm, decimals)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pair.marginMm >= 0) StatusAdequate else StatusBelowTarget
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClearanceMatrixTable(
    objects: List<FurnitureObject>,
    pairs: List<ClearanceResult>,
    unit: LengthUnit,
    decimals: Int,
    onSelectPair: (Pair<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (objects.isEmpty()) return

    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CLEARANCE MATRIX",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.horizontalScroll(scrollState)) {
                Column {
                    // Header row
                    Row {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = "Object", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        objects.forEach { obj ->
                            Box(
                                modifier = Modifier
                                    .width(85.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = obj.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Matrix data rows
                    objects.forEach { rowObj ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = rowObj.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            objects.forEach { colObj ->
                                Box(
                                    modifier = Modifier
                                        .width(85.dp)
                                        .padding(2.dp)
                                        .background(
                                            if (rowObj.id == colObj.id) Color.Transparent
                                            else MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable(enabled = rowObj.id != colObj.id) {
                                            onSelectPair(Pair(rowObj.id, colObj.id))
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rowObj.id == colObj.id) {
                                        Text(text = "—", color = Color.Gray, fontSize = 12.sp)
                                    } else {
                                        val pairResult = pairs.find {
                                            (it.objectAId == rowObj.id && it.objectBId == colObj.id) ||
                                                    (it.objectAId == colObj.id && it.objectBId == rowObj.id)
                                        }
                                        if (pairResult != null) {
                                            if (pairResult.overlaps) {
                                                Text(
                                                    text = "OVERLAP",
                                                    color = StatusOverlapping,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                val isBelow = pairResult.status == ClearanceStatus.BELOW_TARGET
                                                Text(
                                                    text = unit.format(pairResult.distanceMm, decimals),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isBelow) StatusBelowTarget else StatusAdequate
                                                )
                                            }
                                        } else {
                                            Text(text = "—", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
