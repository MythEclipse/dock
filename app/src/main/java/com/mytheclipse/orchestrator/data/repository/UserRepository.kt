package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ApiResult
import com.mytheclipse.orchestrator.data.api.CreateUserRequest
import com.mytheclipse.orchestrator.data.api.DockerManagerApi
import com.mytheclipse.orchestrator.data.api.NetworkModule
import com.mytheclipse.orchestrator.data.api.UpdateUserRequest
import com.mytheclipse.orchestrator.data.api.UserDto

class UserRepository(
    private val network: NetworkModule,
    private val api: DockerManagerApi = network.dockerManagerApi
) {
    suspend fun list(page: Int = 1, pageSize: Int = 10): ApiResult<List<UserDto>> {
        return runCatching {
            network.parseResponse(api.getUsers(page, pageSize))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.users)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun create(email: String, password: String, name: String, role: String): ApiResult<UserDto> {
        return runCatching {
            network.parseResponse(api.createUser(CreateUserRequest(email, password, name, role)))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun update(id: String, email: String? = null, name: String? = null, role: String? = null): ApiResult<UserDto> {
        return runCatching {
            network.parseResponse(api.updateUser(id, UpdateUserRequest(email, name, role)))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user)
                is ApiResult.Error -> result
            }
        }
    }

    suspend fun delete(id: String): ApiResult<Unit> {
        return runCatching {
            network.parseResponse(api.deleteUser(id))
        }.getOrElse {
            ApiResult.Error(null, it.message ?: "Network request failed")
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(Unit)
                is ApiResult.Error -> result
            }
        }
    }
}
