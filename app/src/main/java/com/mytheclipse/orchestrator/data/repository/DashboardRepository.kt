package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.NodeDto

data class DashboardSummary(
    val nodeCount: Int,
    val onlineNodeCount: Int,
    val containerCount: Int,
    val runningContainerCount: Int
)

object DashboardRepository {
    fun summaryFrom(nodes: List<NodeDto>, containers: List<ContainerDto>): DashboardSummary {
        val nodeCount = nodes.size
        val onlineNodeCount = nodes.count { it.status.equals("online", ignoreCase = true) }
        val containerCount = containers.size
        val runningContainerCount = containers.count { it.status.equals("running", ignoreCase = true) }

        return DashboardSummary(
            nodeCount = nodeCount,
            onlineNodeCount = onlineNodeCount,
            containerCount = containerCount,
            runningContainerCount = runningContainerCount
        )
    }
}
