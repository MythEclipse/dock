package com.mytheclipse.orchestrator.session

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionCookieStoreTest {
    @Test
    fun detectsOnlyAuthSessionCookiesAsSession() {
        assertFalse(SessionCookieStore.hasSessionCookie(listOf(csrfCookie(), callbackCookie())))
        assertTrue(SessionCookieStore.hasSessionCookie(listOf(csrfCookie(), authSessionCookie())))
    }

    private fun csrfCookie(): Cookie = Cookie.Builder()
        .name("authjs.csrf-token")
        .value("token")
        .domain("docker.asepharyana.tech")
        .path("/")
        .build()

    private fun callbackCookie(): Cookie = Cookie.Builder()
        .name("authjs.callback-url")
        .value("http%3A%2F%2Flocalhost%3A3000")
        .domain("docker.asepharyana.tech")
        .path("/")
        .build()

    private fun authSessionCookie(): Cookie = Cookie.Builder()
        .name("authjs.session-token")
        .value("session")
        .domain("docker.asepharyana.tech")
        .path("/")
        .build()
}
