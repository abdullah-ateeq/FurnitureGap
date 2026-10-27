package com.example.furnituregap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.ui.theme.StatusAdequate
import com.example.furnituregap.ui.theme.StatusBelowTarget
import com.example.furnituregap.ui.theme.StatusOverlapping
import com.example.furnituregap.ui.theme.StatusTouching

@Composable
fun ClearanceStatusBadge(
    status: ClearanceStatus,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (bgColor, textColor, icon) = when (status) {
        ClearanceStatus.ADEQUATE -> Triple(
            StatusAdequate.copy(alpha = 0.15f),
            StatusAdequate,
            Icons.Default.CheckCircle
        )
        ClearanceStatus.BELOW_TARGET -> Triple(
            StatusBelowTarget.copy(alpha = 0.15f),
            StatusBelowTarget,
            Icons.Default.Warning
        )
        ClearanceStatus.TOUCHING -> Triple(
            StatusTouching.copy(alpha = 0.15f),
            StatusTouching,
            Icons.Default.Info
        )
        ClearanceStatus.OVERLAPPING -> Triple(
            StatusOverlapping.copy(alpha = 0.15f),
            StatusOverlapping,
            Icons.Default.Close
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = customText ?: status.badge,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
