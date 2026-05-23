package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.NetworkModule
import com.mytheclipse.orchestrator.session.SessionCookieStore

class AuthRepository(
    private val sessionCookieStore: SessionCookieStore,
    private val networkModule: NetworkModule
) {
    private val api: DockerManagerApi = networkModule.dockerManagerApi

    suspend fun login(email: String, password: String): ApiResult<Unit> {
        return try {
            // Step 1: Get CSRF token
            val csrfResponse = api.getCsrfToken()
            if (!csrfResponse.isSuccessful || csrfResponse.body() == null) {
                return ApiResult.Error(csrfResponse.code(), "Failed to get CSRF token")
            }

            val csrfToken = csrfResponse.body()!!.csrfToken

            // Step 2: Attempt login with credentials
            val loginResponse = api.login(
                csrfToken = csrfToken,
                email = email,
                password = password,
                json = "true"
            )

            // Step 3: Check if login was successful
            // NextAuth credentials flow returns 200 on success, 401 on failure
            if (loginResponse.isSuccessful) {
                // Session cookies are automatically saved by SessionCookieStore
                ApiResult.Success(Unit)
            } else {
                val errorMessage = when (loginResponse.code()) {
                    401 -> "Invalid email or password"
                    else -> "Login failed"
                }
                ApiResult.Error(loginResponse.code(), errorMessage)
            }
        } catch (e: Exception) {
            ApiResult.Error(null, e.message ?: "Login failed")
        }
    }

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

