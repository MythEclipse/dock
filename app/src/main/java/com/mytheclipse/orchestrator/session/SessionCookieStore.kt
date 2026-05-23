package com.mytheclipse.orchestrator.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieStore(context: Context) : CookieJar {
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
        for ((_, value) in prefs.all) {
            val cookie = Cookie.parse(url, value as String)
            if (cookie != null) {
                cookies.add(cookie)
            }
        }
        return cookies
    }

    fun hasSession(): Boolean = prefs.all.isNotEmpty()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
