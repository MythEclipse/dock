package com.mytheclipse.orchestrator.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.repository.ContainerRepository
import com.mytheclipse.orchestrator.data.repository.DashboardRepository
import com.mytheclipse.orchestrator.data.repository.DashboardSummary
import com.mytheclipse.orchestrator.data.repository.NodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val summary: DashboardSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val nodeRepository: NodeRepository,
    private val containerRepository: ContainerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState(isLoading = true)
            try {
                val nodesResult = nodeRepository.list()
                val containersResult = containerRepository.list()

                when {
                    nodesResult is ApiResult.Error -> {
                        _uiState.value = DashboardUiState(
                            error = nodesResult.message ?: "Failed to load nodes",
                            isLoading = false
                        )
                    }
                    containersResult is ApiResult.Error -> {
                        _uiState.value = DashboardUiState(
                            error = containersResult.message ?: "Failed to load containers",
                            isLoading = false
                        )
                    }
                    nodesResult is ApiResult.Success && containersResult is ApiResult.Success -> {
                        val summary = DashboardRepository.summaryFrom(
                            nodesResult.data,
                            containersResult.data
                        )
                        _uiState.value = DashboardUiState(summary = summary, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState(
                    error = e.message ?: "An unexpected error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun retry() {
        load()
    }
}
