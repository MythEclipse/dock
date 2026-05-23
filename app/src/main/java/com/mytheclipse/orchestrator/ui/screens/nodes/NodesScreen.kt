package com.mytheclipse.orchestrator.ui.screens.nodes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mytheclipse.orchestrator.AppContainer
import com.mytheclipse.orchestrator.data.api.NodeDto
import com.mytheclipse.orchestrator.ui.components.ConfirmDialog
import com.mytheclipse.orchestrator.ui.components.ErrorState
import com.mytheclipse.orchestrator.ui.components.StatusChip
import com.mytheclipse.orchestrator.ui.theme.PanelHigh
import com.mytheclipse.orchestrator.ui.theme.TextMuted
import com.mytheclipse.orchestrator.ui.theme.TextPrimary

@Composable
fun NodesScreen(
    appContainer: AppContainer? = null,
    modifier: Modifier = Modifier,
    onUnauthorized: () -> Unit = {}
) {
    val viewModel = remember {
        appContainer?.let { NodesViewModel(it.nodeRepository) }
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
                text = "Nodes",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Manage your Docker nodes",
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateForm() }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Node")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        text = "Nodes",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manage your Docker nodes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(onClick = { viewModel.load() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }

            if (uiState.actionError != null) {
                ErrorState(
                    message = uiState.actionError ?: "Unknown error",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            when {
                uiState.isLoading && uiState.nodes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading nodes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                uiState.error != null && uiState.nodes.isEmpty() -> {
                    ErrorState(
                        message = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                uiState.nodes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No nodes yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "Create your first node to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.nodes) { node ->
                            NodeCard(
                                node = node,
                                onEdit = { viewModel.openEditForm(node) },
                                onDelete = { viewModel.deleteNode(node) },
                                onSync = { viewModel.syncNode(node) },
                                onSyncContainers = { viewModel.syncContainers(node) },
                                isActionInProgress = uiState.actionInProgress
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showForm) {
            NodeFormDialog(
                formState = uiState.formState,
                isEditing = uiState.isFormEditing,
                isLoading = uiState.actionInProgress,
                onNameChange = { viewModel.updateFormName(it) },
                onPortainerUrlChange = { viewModel.updateFormPortainerUrl(it) },
                onPortainerUsernameChange = { viewModel.updateFormPortainerUsername(it) },
                onPortainerPasswordChange = { viewModel.updateFormPortainerPassword(it) },
                onStatusChange = { viewModel.updateFormStatus(it) },
                onSubmit = { viewModel.submitForm() },
                onDismiss = { viewModel.closeForm() }
            )
        }
    }
}

@Composable
private fun NodeCard(
    node: NodeDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSync: () -> Unit,
    onSyncContainers: () -> Unit,
    isActionInProgress: Boolean,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PanelHigh, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = node.ipAddress ?: node.portainerUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (node.cpuCapacity != null || node.ramCapacityMb != null) {
                    Text(
                        text = buildString {
                            if (node.cpuCapacity != null) append("CPU: ${node.cpuCapacity}")
                            if (node.cpuCapacity != null && node.ramCapacityMb != null) append(" | ")
                            if (node.ramCapacityMb != null) append("RAM: ${node.ramCapacityMb}MB")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (node.lastSyncedAt != null) {
                    Text(
                        text = "Last synced: ${node.lastSyncedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            StatusChip(text = node.status, status = node.status)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onEdit,
                enabled = !isActionInProgress,
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(
                onClick = onSync,
                enabled = !isActionInProgress,
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp)
            ) {
                Icon(Icons.Filled.Sync, contentDescription = "Sync")
            }
            IconButton(
                onClick = { showDeleteConfirm = true },
                enabled = !isActionInProgress,
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Node",
            message = "Are you sure you want to delete '${node.name}'? This action cannot be undone.",
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            },
            onDismiss = { showDeleteConfirm = false },
            confirmLabel = "Delete",
            destructive = true
        )
    }
}

@Composable
private fun NodeFormDialog(
    formState: NodeFormState,
    isEditing: Boolean,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onPortainerUrlChange: (String) -> Unit,
    onPortainerUsernameChange: (String) -> Unit,
    onPortainerPasswordChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Node" else "Create Node")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    isError = formState.nameError != null,
                    supportingText = formState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = formState.portainerUrl,
                    onValueChange = onPortainerUrlChange,
                    label = { Text("Portainer URL") },
                    isError = formState.portainerUrlError != null,
                    supportingText = formState.portainerUrlError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    placeholder = { Text("https://portainer.example.com") }
                )
                OutlinedTextField(
                    value = formState.portainerUsername,
                    onValueChange = onPortainerUsernameChange,
                    label = { Text("Portainer Username") },
                    isError = formState.portainerUsernameError != null,
                    supportingText = formState.portainerUsernameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = formState.portainerPassword,
                    onValueChange = onPortainerPasswordChange,
                    label = { Text("Portainer Password") },
                    isError = formState.portainerPasswordError != null,
                    supportingText = formState.portainerPasswordError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                if (isEditing) {
                    OutlinedTextField(
                        value = formState.status,
                        onValueChange = onStatusChange,
                        label = { Text("Status (optional)") },
                        isError = formState.statusError != null,
                        supportingText = formState.statusError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        placeholder = { Text("online/offline") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp).padding(end = 8.dp))
                }
                Text(if (isEditing) "Update" else "Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
