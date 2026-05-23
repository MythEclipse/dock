package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.session.SessionCookieStore

class AuthRepository(
    private val sessionCookieStore: SessionCookieStore
) {
    fun hasSession(): Boolean {
        return sessionCookieStore.hasSession()
    }

    fun logout(): ApiResult<Unit> {
        return try {
            sessionCookieStore.clear()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(null, e.message ?: "Failed to logout")
        }
    }

    fun clear(): ApiResult<Unit> {
        return try {
            sessionCookieStore.clear()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(null, e.message ?: "Failed to clear session")
        }
    }
}
