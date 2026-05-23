package com.mytheclipse.orchestrator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.orchestrator.ui.theme.Graphite
import com.mytheclipse.orchestrator.ui.theme.TextMuted
import com.mytheclipse.orchestrator.ui.theme.TextPrimary

@Composable
fun TerminalLogViewer(
    logs: String,
    onRefresh: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Graphite, shape = MaterialTheme.shapes.medium)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terminal",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.weight(1f)
            )
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.then(Modifier)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh logs",
                        tint = TextMuted
                    )
                }
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.then(Modifier)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy logs",
                        tint = TextMuted
                    )
                }
            }
        }
        Text(
            text = logs.ifEmpty { "No logs available" },
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = if (logs.isEmpty()) TextMuted else TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}
