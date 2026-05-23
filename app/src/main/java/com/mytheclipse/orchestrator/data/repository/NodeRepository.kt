package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.CreateNodeRequest
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.NetworkModule
import com.mytheclipse.orchestrator.data.api.NodeDto
import com.mytheclipse.orchestrator.data.api.UpdateNodeRequest

class NodeRepository(
    private val network: NetworkModule,
    private val api: DockerManagerApi = network.dockerManagerApi
) {
    suspend fun list(page: Int = 1, pageSize: Int = 10): ApiResult<List<NodeDto>> {
        return runCatching {
            network.parseResponse(api.getNodes(page, pageSize))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.nodes)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun create(name: String, host: String, port: Int): ApiResult<NodeDto> {
        return runCatching {
            network.parseResponse(api.createNode(CreateNodeRequest(name, host, port)))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.node)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun update(id: String, name: String? = null, host: String? = null, port: Int? = null): ApiResult<NodeDto> {
        return runCatching {
            network.parseResponse(api.updateNode(id, UpdateNodeRequest(name, host, port)))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.node)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun delete(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.deleteNode(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun sync(id: String): ApiResult<List<String>> {
        return runCatching {
            network.parseResponse(api.syncNode(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.syncedContainerIds)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun syncContainers(id: String): ApiResult<List<String>> {
        return runCatching {
            network.parseResponse(api.syncNodeContainers(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.syncedContainerIds)
                is ApiResult.Error -> result
            }
        }
    }
}
