package com.mytheclipse.orchestrator.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.mytheclipse.orchestrator.session.SessionCookieStore

class NetworkModule(private val sessionCookieStore: SessionCookieStore) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(sessionCookieStore)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BaseUrl)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val dockerManagerApi: DockerManagerApi = retrofit.create(DockerManagerApi::class.java)

    fun <T> parseResponse(response: Response<T>): ApiResult<T> {
        return if (response.isSuccessful && response.body() != null) {
            ApiResult.Success(response.body()!!)
        } else {
            val errorMessage = try {
                val raw = response.errorBody()?.string() ?: "Request failed"
                if (raw == "Request failed" || raw.isBlank()) {
                    raw
                } else {
                    try {
                        val errorBodyAdapter = moshi.adapter(ApiErrorBody::class.java)
                        val apiErrorBody = errorBodyAdapter.fromJson(raw)
                        apiErrorBody?.error ?: apiErrorBody?.message ?: raw
                    } catch (e: Exception) {
                        raw
                    }
                }
            } catch (e: Exception) {
                "Request failed"
            }
            ApiResult.Error(response.code(), errorMessage)
        }
    }
}
