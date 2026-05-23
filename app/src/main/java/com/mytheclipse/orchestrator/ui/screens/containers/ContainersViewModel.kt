package com.mytheclipse.orchestrator.ui.screens.containers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.DockerHubImageDto
import com.mytheclipse.orchestrator.data.api.NodeDto
import com.mytheclipse.orchestrator.data.api.UserDto
import com.mytheclipse.orchestrator.data.repository.ContainerRepository
import com.mytheclipse.orchestrator.data.repository.NodeRepository
import com.mytheclipse.orchestrator.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContainersUiState(
    val containers: List<ContainerDto> = emptyList(),
    val nodes: List<NodeDto> = emptyList(),
    val users: List<UserDto> = emptyList(),
    val imageSearchResults: List<DockerHubImageDto> = emptyList(),
    val selectedContainer: ContainerDto? = null,
    val logs: String = "",
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingLogs: Boolean = false,
    val error: String? = null,
    val actionInProgress: String? = null,
    val successMessage: String? = null
)

class ContainersViewModel(
    private val containerRepository: ContainerRepository,
    private val nodeRepository: NodeRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContainersUiState(isLoading = true))
    val uiState: StateFlow<ContainersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val containersResult = containerRepository.list()
                val nodesResult = nodeRepository.list()
                val usersResult = userRepository.list()

                when {
                    containersResult is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = containersResult.message ?: "Failed to load containers",
                            isLoading = false
                        )
                    }
                    nodesResult is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = nodesResult.message ?: "Failed to load nodes",
                            isLoading = false
                        )
                    }
                    containersResult is ApiResult.Success && nodesResult is ApiResult.Success -> {
                        val users = when (usersResult) {
                            is ApiResult.Success -> usersResult.data
                            else -> emptyList()
                        }
                        _uiState.value = _uiState.value.copy(
                            containers = containersResult.data,
                            nodes = nodesResult.data,
                            users = users,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An unexpected error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun searchImages(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(imageSearchResults = emptyList())
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            try {
                val result = containerRepository.searchImages(query)
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            imageSearchResults = result.data,
                            isSearching = false
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to search images",
                            isSearching = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Search failed",
                    isSearching = false
                )
            }
        }
    }

    fun createContainer(
        nodeId: String,
        name: String,
        image: String,
        cpu: Int? = null,
        ramMb: Int? = null,
        ownerId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "Creating container...", error = null)
            try {
                val result = containerRepository.create(nodeId, name, image)
                when (result) {
                    is ApiResult.Success -> {
                        val updatedContainers = _uiState.value.containers + result.data
                        _uiState.value = _uiState.value.copy(
                            containers = updatedContainers,
                            actionInProgress = null,
                            successMessage = "Container created successfully",
                            imageSearchResults = emptyList()
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to create container",
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create container",
                    actionInProgress = null
                )
            }
        }
    }

    fun startContainer(containerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "Starting container...", error = null)
            try {
                val result = containerRepository.start(containerId)
                when (result) {
                    is ApiResult.Success -> {
                        refreshContainers()
                        _uiState.value = _uiState.value.copy(
                            actionInProgress = null,
                            successMessage = "Container started"
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to start container",
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to start container",
                    actionInProgress = null
                )
            }
        }
    }

    fun stopContainer(containerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "Stopping container...", error = null)
            try {
                val result = containerRepository.stop(containerId)
                when (result) {
                    is ApiResult.Success -> {
                        refreshContainers()
                        _uiState.value = _uiState.value.copy(
                            actionInProgress = null,
                            successMessage = "Container stopped"
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to stop container",
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to stop container",
                    actionInProgress = null
                )
            }
        }
    }

    fun restartContainer(containerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "Restarting container...", error = null)
            try {
                val result = containerRepository.restart(containerId)
                when (result) {
                    is ApiResult.Success -> {
                        refreshContainers()
                        _uiState.value = _uiState.value.copy(
                            actionInProgress = null,
                            successMessage = "Container restarted"
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to restart container",
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to restart container",
                    actionInProgress = null
                )
            }
        }
    }

    fun deleteContainer(containerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = "Deleting container...", error = null)
            try {
                val result = containerRepository.delete(containerId)
                when (result) {
                    is ApiResult.Success -> {
                        val updatedContainers = _uiState.value.containers.filter { it.id != containerId }
                        _uiState.value = _uiState.value.copy(
                            containers = updatedContainers,
                            actionInProgress = null,
                            successMessage = "Container deleted",
                            selectedContainer = null
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to delete container",
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete container",
                    actionInProgress = null
                )
            }
        }
    }

    fun loadLogs(containerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLogs = true, error = null)
            try {
                val result = containerRepository.logs(containerId)
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            logs = result.data,
                            isLoadingLogs = false
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to load logs",
                            isLoadingLogs = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load logs",
                    isLoadingLogs = false
                )
            }
        }
    }

    fun selectContainer(container: ContainerDto) {
        _uiState.value = _uiState.value.copy(selectedContainer = container)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    private fun refreshContainers() {
        viewModelScope.launch {
            try {
                val result = containerRepository.list()
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(containers = result.data)
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to refresh containers"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh containers"
                )
            }
        }
    }

    fun retry() {
        load()
    }
}
