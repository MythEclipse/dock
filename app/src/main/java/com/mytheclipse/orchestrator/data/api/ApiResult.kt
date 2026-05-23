package com.mytheclipse.orchestrator.data.api

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val statusCode: Int?, val message: String) : ApiResult<Nothing>
}
