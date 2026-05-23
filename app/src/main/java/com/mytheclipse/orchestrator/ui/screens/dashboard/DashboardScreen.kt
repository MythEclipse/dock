package com.mytheclipse.orchestrator.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mytheclipse.orchestrator.AppContainer
import com.mytheclipse.orchestrator.ui.components.ErrorState
import com.mytheclipse.orchestrator.ui.components.MetricCard
import com.mytheclipse.orchestrator.ui.theme.Cyan
import com.mytheclipse.orchestrator.ui.theme.OnlineGreen
import com.mytheclipse.orchestrator.ui.theme.TextMuted
import com.mytheclipse.orchestrator.ui.theme.TextPrimary
import com.mytheclipse.orchestrator.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier,
    onUnauthorized: () -> Unit = {}
) {
    val viewModel = remember {
        appContainer?.let {
            DashboardViewModel(it.nodeRepository, it.containerRepository)
        }
    }

    if (viewModel == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Overview of your infrastructure",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        return
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onUnauthorized()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Overview of your infrastructure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = { viewModel.load() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading dashboard...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: "Unknown error",
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            uiState.summary != null -> {
                val summary = uiState.summary!!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Nodes",
                            value = summary.nodeCount.toString(),
                            subtitle = "${summary.onlineNodeCount} online",
                            accentColor = Cyan,
                            modifier = Modifier
                                .weight(1f)
                                .padding(0.dp)
                        )
                        MetricCard(
                            title = "Containers",
                            value = summary.containerCount.toString(),
                            subtitle = "${summary.runningContainerCount} running",
                            accentColor = OnlineGreen,
                            modifier = Modifier
                                .weight(1f)
                                .padding(0.dp)
                        )
                    }
                }
            }
        }
    }
}
