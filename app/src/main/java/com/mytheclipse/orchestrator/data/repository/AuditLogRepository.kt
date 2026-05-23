package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.AuditLogDto
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.NetworkModule

class AuditLogRepository(
    private val network: NetworkModule,
    private val api: DockerManagerApi = network.dockerManagerApi
) {
    suspend fun list(page: Int = 1, pageSize: Int = 10): ApiResult<List<AuditLogDto>> {
        return runCatching {
            network.parseResponse(api.getAuditLogs(page, pageSize))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.logs)
                is ApiResult.Error -> result
            }
        }
    }
}
