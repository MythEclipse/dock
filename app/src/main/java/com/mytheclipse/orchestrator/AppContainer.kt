package com.mytheclipse.orchestrator

import android.content.Context
import com.mytheclipse.orchestrator.data.api.NetworkModule
import com.mytheclipse.orchestrator.data.repository.AuditLogRepository
import com.mytheclipse.orchestrator.data.repository.AuthRepository
import com.mytheclipse.orchestrator.data.repository.ContainerRepository
import com.mytheclipse.orchestrator.data.repository.DashboardRepository
import com.mytheclipse.orchestrator.data.repository.NodeRepository
import com.mytheclipse.orchestrator.data.repository.UserRepository
import com.mytheclipse.orchestrator.session.SessionCookieStore

class AppContainer(context: Context) {
    private val sessionCookieStore = SessionCookieStore(context)
    private val networkModule = NetworkModule(sessionCookieStore)

    val authRepository = AuthRepository(sessionCookieStore)
    val nodeRepository = NodeRepository(networkModule)
    val containerRepository = ContainerRepository(networkModule)
    val userRepository = UserRepository(networkModule)
    val auditLogRepository = AuditLogRepository(networkModule)
    val dashboardRepository = DashboardRepository
}
