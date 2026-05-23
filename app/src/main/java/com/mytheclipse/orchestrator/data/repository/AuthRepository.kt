package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.NetworkModule
import com.mytheclipse.orchestrator.session.SessionCookieStore
import okhttp3.FormBody
import okhttp3.Request

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

            val callbackUrl = "${com.mytheclipse.orchestrator.data.api.ApiConfig.BaseUrl}api/auth/callback/credentials"
            val request = Request.Builder()
                .url(callbackUrl)
                .post(
                    FormBody.Builder()
                        .add("csrfToken", csrfToken)
                        .add("email", email)
                        .add("password", password)
                        .add("json", "true")
                        .build()
                )
                .build()

            networkModule.authHttpClient.newCall(request).execute().use { loginResponse ->
                if (loginResponse.isSuccessful) {
                    ApiResult.Success(Unit)
                } else if (loginResponse.code == 302) {
                    val location = loginResponse.header("Location").orEmpty()
                    if (location.contains("error=", ignoreCase = true)) {
                        ApiResult.Error(401, "Invalid email or password")
                    } else {
                        ApiResult.Success(Unit)
                    }
                } else {
                    val errorMessage = when (loginResponse.code) {
                        401 -> "Invalid email or password"
                        else -> "Login failed"
                    }
                    ApiResult.Error(loginResponse.code, errorMessage)
                }
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

