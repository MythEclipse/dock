package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.CreateContainerRequest
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.DockerHubImageDto
import com.mytheclipse.orchestrator.data.api.NetworkModule

class ContainerRepository(
    private val network: NetworkModule,
    private val api: DockerManagerApi = network.dockerManagerApi
) {
    suspend fun list(page: Int = 1, pageSize: Int = 10): ApiResult<List<ContainerDto>> {
        return runCatching {
            network.parseResponse(api.getContainers(page, pageSize))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.containers)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun get(id: String): ApiResult<ContainerDto> {
        return runCatching {
            network.parseResponse(api.getContainer(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.container)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun create(
        nodeId: String,
        name: String,
        image: String,
        cpu: Int,
        ramMb: Int,
        ownerId: String? = null
    ): ApiResult<ContainerDto> {
        return runCatching {
            network.parseResponse(api.createContainer(CreateContainerRequest(nodeId, name, image, cpu, ramMb, ownerId)))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.container)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun delete(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.deleteContainer(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun start(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.startContainer(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun stop(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.stopContainer(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun restart(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.restartContainer(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun logs(id: String): ApiResult<String> {
        return runCatching {
            network.parseResponse(api.getContainerLogs(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.logs)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun searchImages(query: String): ApiResult<List<DockerHubImageDto>> {
        return runCatching {
            network.parseResponse(api.searchDockerHub(query))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.results)
                is ApiResult.Error -> result
            }
        }
    }
}
