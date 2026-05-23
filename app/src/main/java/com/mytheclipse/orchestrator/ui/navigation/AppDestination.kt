package com.mytheclipse.orchestrator.ui.navigation

sealed class AppDestination(val route: String) {
    object Login : AppDestination("login")
    object Dashboard : AppDestination("dashboard")
    object Nodes : AppDestination("nodes")
    object Containers : AppDestination("containers")
    object Users : AppDestination("users")
    object AuditLogs : AppDestination("audit_logs")
    object ContainerLogs : AppDestination("container_logs/{containerId}") {
        fun createRoute(containerId: String) = "container_logs/$containerId"
    }
}
