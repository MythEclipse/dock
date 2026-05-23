package com.mytheclipse.orchestrator.data.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkModuleTest {
    @Test
    fun authHttpClientDoesNotFollowRedirectsToCallbackUrl() {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setResponseCode(302)
                    .setHeader("Location", "http://localhost:3000/login?error=CredentialsSignin&code=credentials")
            )
            server.start()

            val client = NetworkModule.createAuthHttpClient(OkHttpClient())
            val response = client.newCall(Request.Builder().url(server.url("/api/auth/callback/credentials")).build()).execute()

            assertEquals(302, response.code)
            assertEquals("http://localhost:3000/login?error=CredentialsSignin&code=credentials", response.header("Location"))
            assertNull(response.priorResponse)
        }
    }
}
