package com.mytheclipse.orchestrator.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mytheclipse.orchestrator.ui.theme.DangerRed
import com.mytheclipse.orchestrator.ui.theme.OnlineGreen
import com.mytheclipse.orchestrator.ui.theme.TextPrimary
import com.mytheclipse.orchestrator.ui.theme.WarningAmber

@Composable
fun StatusChip(
    text: String,
    status: String,
    onClick: (() -> Unit)? = null
) {
    val (containerColor, labelColor) = when (status.lowercase()) {
        "online", "running", "success" -> OnlineGreen to Color.Black
        "warning", "sync" -> WarningAmber to Color.Black
        "offline", "error", "destructive" -> DangerRed to TextPrimary
        else -> Color.Gray to TextPrimary
    }

    AssistChip(
        onClick = onClick ?: {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor
        ),
        enabled = onClick != null
    )
}
