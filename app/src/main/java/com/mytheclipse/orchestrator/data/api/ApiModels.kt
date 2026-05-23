package com.mytheclipse.orchestrator.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class ApiErrorBody(
    val error: String,
    val message: String
)

@JsonClass(generateAdapter = false)
data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = false)
data class UsersResponse(
    val users: List<UserDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@JsonClass(generateAdapter = false)
data class UserResponse(
    val user: UserDto
)

@JsonClass(generateAdapter = false)
data class CreateUserRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String
)

@JsonClass(generateAdapter = false)
data class UpdateUserRequest(
    val email: String? = null,
    val name: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = false)
data class NodeDto(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = false)
data class NodesResponse(
    val nodes: List<NodeDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@JsonClass(generateAdapter = false)
data class NodeResponse(
    val node: NodeDto
)

@JsonClass(generateAdapter = false)
data class CreateNodeRequest(
    val name: String,
    val host: String,
    val port: Int
)

@JsonClass(generateAdapter = false)
data class UpdateNodeRequest(
    val name: String? = null,
    val host: String? = null,
    val port: Int? = null
)

@JsonClass(generateAdapter = false)
data class ContainerDto(
    val id: String,
    val nodeId: String,
    val name: String,
    val image: String,
    val status: String,
    val ports: List<String>,
    val createdAt: String,
    val updatedAt: String
)

@JsonClass(generateAdapter = false)
data class ContainersResponse(
    val containers: List<ContainerDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@JsonClass(generateAdapter = false)
data class ContainerResponse(
    val container: ContainerDto
)

@JsonClass(generateAdapter = false)
data class CreateContainerRequest(
    val nodeId: String,
    val name: String,
    val image: String,
    val ports: List<String>? = null,
    val env: Map<String, String>? = null
)

@JsonClass(generateAdapter = false)
data class LogsResponse(
    val logs: String,
    val containerId: String
)

@JsonClass(generateAdapter = false)
data class DockerHubSearchResponse(
    val results: List<DockerHubImageDto>,
    val count: Int
)

@JsonClass(generateAdapter = false)
data class DockerHubImageDto(
    val name: String,
    val description: String,
    val starCount: Int,
    val pullCount: Int,
    val official: Boolean
)

@JsonClass(generateAdapter = false)
data class AuditLogsResponse(
    val logs: List<AuditLogDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

@JsonClass(generateAdapter = false)
data class AuditLogDto(
    val id: String,
    val userId: String,
    val action: String,
    val resource: String,
    val resourceId: String,
    val timestamp: String,
    val details: String? = null
)

@JsonClass(generateAdapter = false)
data class OkResponse(
    val message: String
)

@JsonClass(generateAdapter = false)
data class SyncContainersResponse(
    val synced: Int,
    val failed: Int,
    val message: String
)
