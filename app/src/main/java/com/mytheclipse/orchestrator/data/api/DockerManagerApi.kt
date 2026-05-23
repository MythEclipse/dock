package com.mytheclipse.orchestrator.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DockerManagerApi {
    // Nodes endpoints
    @GET("api/nodes")
    suspend fun getNodes(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<NodesResponse>

    @GET("api/nodes/{id}")
    suspend fun getNode(@Path("id") id: String): Response<NodeResponse>

    @POST("api/nodes")
    suspend fun createNode(@Body request: CreateNodeRequest): Response<NodeResponse>

    @PUT("api/nodes/{id}")
    suspend fun updateNode(
        @Path("id") id: String,
        @Body request: UpdateNodeRequest
    ): Response<NodeResponse>

    @DELETE("api/nodes/{id}")
    suspend fun deleteNode(@Path("id") id: String): Response<OkResponse>

    @POST("api/nodes/{id}/sync")
    suspend fun syncNode(@Path("id") id: String): Response<SyncContainersResponse>

    // Containers endpoints
    @GET("api/containers")
    suspend fun getContainers(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<ContainersResponse>

    @GET("api/containers/{id}")
    suspend fun getContainer(@Path("id") id: String): Response<ContainerResponse>

    @POST("api/containers")
    suspend fun createContainer(@Body request: CreateContainerRequest): Response<ContainerResponse>

    @PUT("api/containers/{id}")
    suspend fun updateContainer(
        @Path("id") id: String,
        @Body request: CreateContainerRequest
    ): Response<ContainerResponse>

    @DELETE("api/containers/{id}")
    suspend fun deleteContainer(@Path("id") id: String): Response<OkResponse>

    @POST("api/containers/{id}/start")
    suspend fun startContainer(@Path("id") id: String): Response<OkResponse>

    @POST("api/containers/{id}/stop")
    suspend fun stopContainer(@Path("id") id: String): Response<OkResponse>

    @POST("api/containers/{id}/restart")
    suspend fun restartContainer(@Path("id") id: String): Response<OkResponse>

    @GET("api/containers/{id}/logs")
    suspend fun getContainerLogs(@Path("id") id: String): Response<LogsResponse>

    // Docker Hub search endpoint
    @GET("api/docker-hub/search")
    suspend fun searchDockerHub(@Query("query") query: String): Response<DockerHubSearchResponse>

    // Users endpoints
    @GET("api/users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<UsersResponse>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserResponse>

    @POST("api/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserResponse>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<OkResponse>

    // Audit logs endpoint
    @GET("api/audit-logs")
    suspend fun getAuditLogs(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): Response<AuditLogsResponse>
}
