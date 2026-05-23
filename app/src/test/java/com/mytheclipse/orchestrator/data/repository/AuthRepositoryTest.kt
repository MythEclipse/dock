package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @Test
    fun authCallbackRequestRunsOnProvidedDispatcher() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(200))
            server.start()
            newSingleThreadContext("auth-io-test").use { dispatcher ->
                val requestThread = arrayOf<String?>(null)
                val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        requestThread[0] = Thread.currentThread().name
                        chain.proceed(chain.request())
                    }
                    .build()
                val request = Request.Builder().url(server.url("/api/auth/callback/credentials")).build()

                val result = AuthRepository.executeAuthCallback(client, request, dispatcher)

                assertTrue(result is ApiResult.Success<*>)
                assertTrue(requestThread[0]?.startsWith("auth-io-test") == true)
            }
        }
    }
}
