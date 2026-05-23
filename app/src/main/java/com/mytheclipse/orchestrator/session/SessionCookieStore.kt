package com.mytheclipse.orchestrator.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class SessionCookieStore(context: Context) : CookieJar {
    companion object {
        fun hasSessionCookie(cookies: List<Cookie>): Boolean {
            return cookies.any { cookie ->
                cookie.name == "authjs.session-token" ||
                    cookie.name == "__Secure-authjs.session-token" ||
                    cookie.name == "next-auth.session-token" ||
                    cookie.name == "__Secure-next-auth.session-token"
            }
        }
    }

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "docker_manager_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            prefs.edit().putString(cookie.name, cookie.toString()).apply()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        val expiredKeys = mutableListOf<String>()
        val currentTime = System.currentTimeMillis()

        for ((key, value) in prefs.all) {
            val stringValue = value as? String ?: continue
            val cookie = Cookie.parse(url, stringValue)
            if (cookie != null) {
                if (cookie.expiresAt > currentTime) {
                    cookies.add(cookie)
                } else {
                    expiredKeys.add(key)
                }
            }
        }

        // Remove expired cookies from prefs
        if (expiredKeys.isNotEmpty()) {
            prefs.edit().apply {
                for (key in expiredKeys) {
                    remove(key)
                }
            }.apply()
        }

        return cookies
    }

    fun hasSession(): Boolean {
        return hasSessionCookie(loadForRequest("https://docker.asepharyana.tech/".toHttpUrl()))
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
