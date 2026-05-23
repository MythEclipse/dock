package com.mytheclipse.orchestrator.ui.screens.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.NodeDto
import com.mytheclipse.orchestrator.data.repository.NodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NodeFormState(
    val name: String = "",
    val host: String = "",
    val port: String = "",
    val nameError: String? = null,
    val hostError: String? = null,
    val portError: String? = null
)

data class NodesUiState(
    val nodes: List<NodeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showForm: Boolean = false,
    val formState: NodeFormState = NodeFormState(),
    val selectedNode: NodeDto? = null,
    val isFormEditing: Boolean = false,
    val actionInProgress: Boolean = false,
    val actionError: String? = null
)

class NodesViewModel(
    private val nodeRepository: NodeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NodesUiState(isLoading = true))
    val uiState: StateFlow<NodesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = nodeRepository.list()
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        nodes = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message ?: "Failed to load nodes",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun retry() {
        load()
    }

    fun openCreateForm() {
        _uiState.value = _uiState.value.copy(
            showForm = true,
            formState = NodeFormState(),
            selectedNode = null,
            isFormEditing = false,
            actionError = null
        )
    }

    fun openEditForm(node: NodeDto) {
        _uiState.value = _uiState.value.copy(
            showForm = true,
            formState = NodeFormState(
                name = node.name,
                host = node.host,
                port = node.port.toString()
            ),
            selectedNode = node,
            isFormEditing = true,
            actionError = null
        )
    }

    fun closeForm() {
        _uiState.value = _uiState.value.copy(
            showForm = false,
            formState = NodeFormState(),
            selectedNode = null,
            isFormEditing = false,
            actionError = null
        )
    }

    fun updateFormName(name: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                name = name,
                nameError = null
            )
        )
    }

    fun updateFormHost(host: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                host = host,
                hostError = null
            )
        )
    }

    fun updateFormPort(port: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                port = port,
                portError = null
            )
        )
    }

    fun submitForm() {
        if (_uiState.value.actionInProgress) {
            return
        }

        val formState = _uiState.value.formState
        var hasError = false
        var nameError: String? = null
        var hostError: String? = null
        var portError: String? = null

        if (formState.name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        }
        if (formState.host.isBlank()) {
            hostError = "Host is required"
            hasError = true
        }
        if (formState.port.isBlank()) {
            portError = "Port is required"
            hasError = true
        } else {
            val portInt = formState.port.toIntOrNull()
            if (portInt == null || portInt <= 0 || portInt > 65535) {
                portError = "Port must be between 1 and 65535"
                hasError = true
            }
        }

        if (hasError) {
            _uiState.value = _uiState.value.copy(
                formState = formState.copy(
                    nameError = nameError,
                    hostError = hostError,
                    portError = portError
                )
            )
            return
        }

        // Capture validated form state into immutable local value
        val validatedFormState = formState.copy(
            nameError = null,
            hostError = null,
            portError = null
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionError = null)
            val port = validatedFormState.port.toInt()
            val result = if (_uiState.value.isFormEditing) {
                val node = _uiState.value.selectedNode ?: return@launch
                nodeRepository.update(node.id, validatedFormState.name, validatedFormState.host, port)
            } else {
                nodeRepository.create(validatedFormState.name, validatedFormState.host, port)
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false)
                    closeForm()
                    load()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionError = result.message ?: "Operation failed"
                    )
                }
            }
        }
    }

    fun deleteNode(node: NodeDto) {
        if (_uiState.value.actionInProgress) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionError = null)
            val result = nodeRepository.delete(node.id)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false)
                    load()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionError = result.message ?: "Failed to delete node"
                    )
                }
            }
        }
    }

    fun syncNode(node: NodeDto) {
        if (_uiState.value.actionInProgress) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionError = null)
            val result = nodeRepository.sync(node.id)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false)
                    load()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionError = result.message ?: "Failed to sync node"
                    )
                }
            }
        }
    }

    fun syncContainers(node: NodeDto) {
        if (_uiState.value.actionInProgress) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionError = null)
            val result = nodeRepository.syncContainers(node.id)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionInProgress = false)
                    load()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionError = result.message ?: "Failed to sync containers"
                    )
                }
            }
        }
    }
}
