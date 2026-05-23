package com.mytheclipse.orchestrator.ui.screens.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.DockerHubImageDto
import com.mytheclipse.orchestrator.data.api.NodeDto
import com.mytheclipse.orchestrator.ui.components.ConfirmDialog
import com.mytheclipse.orchestrator.ui.components.ErrorState
import com.mytheclipse.orchestrator.ui.components.StatusChip
import com.mytheclipse.orchestrator.ui.components.TerminalLogViewer
import com.mytheclipse.orchestrator.ui.theme.Graphite
import com.mytheclipse.orchestrator.ui.theme.TextMuted
import com.mytheclipse.orchestrator.ui.theme.TextPrimary

@Composable
fun ContainersScreen(
    viewModel: ContainersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var containerToDelete by remember { mutableStateOf<ContainerDto?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create container")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null && uiState.containers.isEmpty() -> {
                    ErrorState(
                        message = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.load() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.containers.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No containers",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = "Create a new container to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Containers (${uiState.containers.size})",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary
                                )
                                IconButton(onClick = { viewModel.load() }) {
                                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                                }
                            }
                        }

                        items(uiState.containers) { container ->
                            ContainerCard(
                                container = container,
                                node = uiState.nodes.find { it.id == container.nodeId },
                                onViewLogs = {
                                    viewModel.selectContainer(container)
                                    viewModel.loadLogs(container.id)
                                    showLogsDialog = true
                                },
                                onStart = { viewModel.startContainer(container.id) },
                                onStop = { viewModel.stopContainer(container.id) },
                                onRestart = { viewModel.restartContainer(container.id) },
                                onDelete = {
                                    containerToDelete = container
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.actionInProgress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.actionInProgress ?: "",
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateContainerDialog(
            nodes = uiState.nodes,
            onDismiss = { showCreateDialog = false },
            onCreate = { nodeId, name, image ->
                viewModel.createContainer(nodeId, name, image)
                showCreateDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showLogsDialog && uiState.selectedContainer != null) {
        ContainerLogsDialog(
            container = uiState.selectedContainer!!,
            logs = uiState.logs,
            isLoading = uiState.isLoadingLogs,
            onRefresh = { uiState.selectedContainer?.let { viewModel.loadLogs(it.id) } },
            onDismiss = { showLogsDialog = false }
        )
    }

    if (showDeleteConfirm && containerToDelete != null) {
        ConfirmDialog(
            title = "Delete Container",
            message = "Are you sure you want to delete '${containerToDelete!!.name}'? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteContainer(containerToDelete!!.id)
                showDeleteConfirm = false
                containerToDelete = null
            },
            onDismiss = {
                showDeleteConfirm = false
                containerToDelete = null
            }
        )
    }
}

@Composable
fun ContainerCard(
    container: ContainerDto,
    node: NodeDto?,
    onViewLogs: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Graphite
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = container.image,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Logs") },
                            onClick = {
                                onViewLogs()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    text = container.status.uppercase(),
                    status = container.status
                )
                if (node != null) {
                    Text(
                        text = "Node: ${node.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (container.status.lowercase()) {
                    "running" -> {
                        Button(
                            onClick = onStop,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop")
                        }
                        Button(
                            onClick = onRestart,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Restart")
                        }
                    }
                    else -> {
                        Button(
                            onClick = onStart,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateContainerDialog(
    nodes: List<NodeDto>,
    onDismiss: () -> Unit,
    onCreate: (nodeId: String, name: String, image: String) -> Unit,
    viewModel: ContainersViewModel
) {
    var selectedNodeId by remember { mutableStateOf(nodes.firstOrNull()?.id ?: "") }
    var containerName by remember { mutableStateOf("") }
    var imageName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Graphite
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create Container",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Node",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = nodes.find { it.id == selectedNodeId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Container Name",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = containerName,
                    onValueChange = { containerName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., my-app") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = imageName,
                    onValueChange = { imageName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., nginx:latest") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Search Docker Hub",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchImages(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search images...") }
                )

                if (uiState.isSearching) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                if (uiState.imageSearchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.imageSearchResults) { image ->
                            SearchResultItem(
                                image = image,
                                onSelect = {
                                    imageName = image.name
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (containerName.isNotBlank() && imageName.isNotBlank()) {
                                onCreate(selectedNodeId, containerName, imageName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = containerName.isNotBlank() && imageName.isNotBlank() && selectedNodeId.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    image: DockerHubImageDto,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = image.name,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary
            )
            if (image.description.isNotBlank()) {
                Text(
                    text = image.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Stars: ${image.starCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "Pulls: ${image.pullCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                if (image.official) {
                    Text(
                        text = "Official",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ContainerLogsDialog(
    container: ContainerDto,
    logs: String,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Graphite
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logs: ${container.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    TerminalLogViewer(
                        logs = logs,
                        onRefresh = onRefresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
