package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.NodeDto
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardRepositoryTest {
    @Test
    fun summaryCountsNodesAndContainersByStatus() {
        val summary = DashboardRepository.summaryFrom(
            nodes = listOf(
                NodeDto(id = "n1", name = "Node 1", portainerUrl = "http://localhost:9000", status = "online", createdAt = "", updatedAt = ""),
                NodeDto(id = "n2", name = "Node 2", portainerUrl = "http://localhost:9001", status = "offline", createdAt = "", updatedAt = ""),
            ),
            containers = listOf(
                ContainerDto(id = "c1", nodeId = "n1", name = "A", image = "nginx", status = "running", createdAt = "", updatedAt = ""),
                ContainerDto(id = "c2", nodeId = "n1", name = "B", image = "redis", status = "stopped", createdAt = "", updatedAt = ""),
            ),
        )

        assertEquals(2, summary.nodeCount)
        assertEquals(1, summary.onlineNodeCount)
        assertEquals(2, summary.containerCount)
        assertEquals(1, summary.runningContainerCount)
    }
}
