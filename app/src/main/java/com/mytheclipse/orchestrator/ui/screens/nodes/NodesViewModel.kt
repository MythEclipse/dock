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
    val portainerUrl: String = "",
    val portainerUsername: String = "",
    val portainerPassword: String = "",
    val status: String = "",
    val nameError: String? = null,
    val portainerUrlError: String? = null,
    val portainerUsernameError: String? = null,
    val portainerPasswordError: String? = null,
    val statusError: String? = null
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
    val actionError: String? = null,
    val isUnauthorized: Boolean = false
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
                        isLoading = false,
                        isUnauthorized = result.statusCode == 401
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
                portainerUrl = node.portainerUrl,
                portainerUsername = node.portainerUsername ?: "",
                status = node.status
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

    fun updateFormPortainerUrl(portainerUrl: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                portainerUrl = portainerUrl,
                portainerUrlError = null
            )
        )
    }

    fun updateFormPortainerUsername(portainerUsername: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                portainerUsername = portainerUsername,
                portainerUsernameError = null
            )
        )
    }

    fun updateFormPortainerPassword(portainerPassword: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                portainerPassword = portainerPassword,
                portainerPasswordError = null
            )
        )
    }

    fun updateFormStatus(status: String) {
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                status = status,
                statusError = null
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
        var portainerUrlError: String? = null
        var portainerUsernameError: String? = null
        var portainerPasswordError: String? = null

        if (formState.name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        }

        val isEditing = _uiState.value.isFormEditing

        // For create, all portainer fields are required
        // For edit, they are optional (only send if provided)
        if (!isEditing) {
            if (formState.portainerUrl.isBlank()) {
                portainerUrlError = "Portainer URL is required"
                hasError = true
            }
            if (formState.portainerUsername.isBlank()) {
                portainerUsernameError = "Portainer username is required"
                hasError = true
            }
            if (formState.portainerPassword.isBlank()) {
                portainerPasswordError = "Portainer password is required"
                hasError = true
            }
        } else {
            // For edit, validate only if provided
            if (formState.portainerUrl.isNotBlank() && formState.portainerUrl.length < 5) {
                portainerUrlError = "Invalid Portainer URL"
                hasError = true
            }
        }

        if (hasError) {
            _uiState.value = _uiState.value.copy(
                formState = formState.copy(
                    nameError = nameError,
                    portainerUrlError = portainerUrlError,
                    portainerUsernameError = portainerUsernameError,
                    portainerPasswordError = portainerPasswordError
                )
            )
            return
        }

        // Capture validated form state into immutable local value
        val validatedFormState = formState.copy(
            nameError = null,
            portainerUrlError = null,
            portainerUsernameError = null,
            portainerPasswordError = null
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionError = null)
            val result = if (_uiState.value.isFormEditing) {
                val node = _uiState.value.selectedNode ?: return@launch
                // For edit, only send non-blank values
                nodeRepository.update(
                    node.id,
                    name = validatedFormState.name.takeIf { it.isNotBlank() },
                    portainerUrl = validatedFormState.portainerUrl.takeIf { it.isNotBlank() },
                    portainerUsername = validatedFormState.portainerUsername.takeIf { it.isNotBlank() },
                    portainerPassword = validatedFormState.portainerPassword.takeIf { it.isNotBlank() },
                    status = validatedFormState.status.takeIf { it.isNotBlank() }
                )
            } else {
                nodeRepository.create(
                    validatedFormState.name,
                    validatedFormState.portainerUrl,
                    validatedFormState.portainerUsername,
                    validatedFormState.portainerPassword
                )
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
