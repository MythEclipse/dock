package com.mytheclipse.orchestrator.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.UserDto
import com.mytheclipse.orchestrator.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsersUiState(
    val users: List<UserDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionInProgress: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val selectedUser: UserDto? = null,
    val editingUser: UserDto? = null,
    val isUnauthorized: Boolean = false
)

class UsersViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UsersUiState(isLoading = true))
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = userRepository.list()
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            users = result.data,
                            isLoading = false
                        )
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to load users",
                            isLoading = false,
                            isUnauthorized = result.statusCode == 401
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

    fun openCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true, error = null)
    }

    fun closeCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun openEditDialog(user: UserDto) {
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingUser = user,
            error = null
        )
    }

    fun closeEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editingUser = null)
    }

    fun openDeleteConfirmation(user: UserDto) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmation = true,
            selectedUser = user,
            error = null
        )
    }

    fun closeDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false, selectedUser = null)
    }

    fun createUser(email: String, password: String, role: String) {
        if (_uiState.value.actionInProgress != null) return
        if (email.isBlank() || password.isBlank() || role.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "All fields are required")
            return
        }

        _uiState.value = _uiState.value.copy(actionInProgress = "Creating user...", error = null)
        viewModelScope.launch {
            try {
                val result = userRepository.create(email, password, "", role)
                when (result) {
                    is ApiResult.Success -> {
                        val updatedUsers = _uiState.value.users + result.data
                        _uiState.value = _uiState.value.copy(
                            users = updatedUsers,
                            actionInProgress = null,
                            successMessage = "User created successfully",
                            showCreateDialog = false
                        )
                    }
                    is ApiResult.Error -> {
                        val errorMsg = when {
                            result.message?.contains("duplicate", ignoreCase = true) == true ->
                                "Email already exists"
                            else -> result.message ?: "Failed to create user"
                        }
                        _uiState.value = _uiState.value.copy(
                            error = errorMsg,
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create user",
                    actionInProgress = null
                )
            }
        }
    }

    fun updateUser(userId: String, email: String?, password: String?, role: String?) {
        if (_uiState.value.actionInProgress != null) return
        if (email.isNullOrBlank() && password.isNullOrBlank() && role.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(error = "At least one field must be updated")
            return
        }

        _uiState.value = _uiState.value.copy(actionInProgress = "Updating user...", error = null)
        viewModelScope.launch {
            try {
                val result = userRepository.update(userId, email, password, role)
                when (result) {
                    is ApiResult.Success -> {
                        val updatedUsers = _uiState.value.users.map {
                            if (it.id == userId) result.data else it
                        }
                        _uiState.value = _uiState.value.copy(
                            users = updatedUsers,
                            actionInProgress = null,
                            successMessage = "User updated successfully",
                            showEditDialog = false,
                            editingUser = null
                        )
                    }
                    is ApiResult.Error -> {
                        val errorMsg = when {
                            result.message?.contains("duplicate", ignoreCase = true) == true ->
                                "Email already exists"
                            else -> result.message ?: "Failed to update user"
                        }
                        _uiState.value = _uiState.value.copy(
                            error = errorMsg,
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update user",
                    actionInProgress = null
                )
            }
        }
    }

    fun deleteUser(userId: String) {
        if (_uiState.value.actionInProgress != null) return

        _uiState.value = _uiState.value.copy(actionInProgress = "Deleting user...", error = null)
        viewModelScope.launch {
            try {
                val result = userRepository.delete(userId)
                when (result) {
                    is ApiResult.Success -> {
                        val updatedUsers = _uiState.value.users.filter { it.id != userId }
                        _uiState.value = _uiState.value.copy(
                            users = updatedUsers,
                            actionInProgress = null,
                            successMessage = "User deleted successfully",
                            showDeleteConfirmation = false,
                            selectedUser = null
                        )
                    }
                    is ApiResult.Error -> {
                        val errorMsg = when {
                            result.message?.contains("conflict", ignoreCase = true) == true ->
                                "Cannot delete user: user has associated resources"
                            else -> result.message ?: "Failed to delete user"
                        }
                        _uiState.value = _uiState.value.copy(
                            error = errorMsg,
                            actionInProgress = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete user",
                    actionInProgress = null
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun retry() {
        load()
    }
}
