package com.mytheclipse.orchestrator.ui.screens.auditlogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.AuditLogDto
import com.mytheclipse.orchestrator.data.repository.AuditLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuditLogsUiState(
    val auditLogs: List<AuditLogDto> = emptyList(),
    val filteredLogs: List<AuditLogDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterAction: String? = null,
    val filterResourceType: String? = null,
    val filterResourceId: String? = null,
    val filterUserEmail: String? = null,
    val isUnauthorized: Boolean = false
)

class AuditLogsViewModel(
    private val auditLogRepository: AuditLogRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuditLogsUiState(isLoading = true))
    val uiState: StateFlow<AuditLogsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = auditLogRepository.list()
                when (result) {
                    is ApiResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            auditLogs = result.data,
                            isLoading = false
                        )
                        applyFilters()
                    }
                    is ApiResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message ?: "Failed to load audit logs",
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

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun setFilterAction(action: String?) {
        _uiState.value = _uiState.value.copy(filterAction = action)
        applyFilters()
    }

    fun setFilterResourceType(resourceType: String?) {
        _uiState.value = _uiState.value.copy(filterResourceType = resourceType)
        applyFilters()
    }

    fun setFilterResourceId(resourceId: String?) {
        _uiState.value = _uiState.value.copy(filterResourceId = resourceId)
        applyFilters()
    }

    fun setFilterUserEmail(userEmail: String?) {
        _uiState.value = _uiState.value.copy(filterUserEmail = userEmail)
        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filterAction = null,
            filterResourceType = null,
            filterResourceId = null,
            filterUserEmail = null
        )
        applyFilters()
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.auditLogs

        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { log ->
                log.action.lowercase().contains(query) ||
                log.resourceType.lowercase().contains(query) ||
                log.resourceId?.lowercase()?.contains(query) == true ||
                log.user?.email?.lowercase()?.contains(query) == true ||
                log.metadata?.lowercase()?.contains(query) == true
            }
        }

        if (!state.filterAction.isNullOrBlank()) {
            filtered = filtered.filter { it.action.equals(state.filterAction, ignoreCase = true) }
        }

        if (!state.filterResourceType.isNullOrBlank()) {
            filtered = filtered.filter { it.resourceType.equals(state.filterResourceType, ignoreCase = true) }
        }

        if (!state.filterResourceId.isNullOrBlank()) {
            filtered = filtered.filter { it.resourceId?.equals(state.filterResourceId, ignoreCase = true) == true }
        }

        if (!state.filterUserEmail.isNullOrBlank()) {
            filtered = filtered.filter { it.user?.email?.equals(state.filterUserEmail, ignoreCase = true) == true }
        }

        _uiState.value = _uiState.value.copy(filteredLogs = filtered)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retry() {
        load()
    }
}
