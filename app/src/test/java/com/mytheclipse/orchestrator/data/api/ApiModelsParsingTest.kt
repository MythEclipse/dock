package com.mytheclipse.orchestrator.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiModelsParsingTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Test
    fun parsesContainerListWithOwnerWithoutId() {
        val json = """
            {
              "containers": [
                {
                  "id": "container-1",
                  "name": "web",
                  "image": "nginx:latest",
                  "status": "running",
                  "dockerContainerId": "docker-1",
                  "nodeId": "node-1",
                  "ownerId": "user-1",
                  "cpu": 1,
                  "ramMb": 256,
                  "createdAt": "2026-05-24T00:00:00.000Z",
                  "updatedAt": "2026-05-24T00:00:00.000Z",
                  "owner": { "email": "admin@example.com", "role": "ADMIN" }
                }
              ]
            }
        """.trimIndent()

        val response = moshi.adapter(ContainersResponse::class.java).fromJson(json)!!

        assertEquals("container-1", response.containers.single().id)
        assertEquals("admin@example.com", response.containers.single().owner?.email)
        assertNull(response.containers.single().owner?.id)
    }

    @Test
    fun parsesLogsResponseWithoutContainerId() {
        val response = moshi.adapter(LogsResponse::class.java).fromJson("""{"logs":"hello"}""")!!

        assertEquals("hello", response.logs)
    }

    @Test
    fun parsesAuditLogsResponseUsingAuditLogsKey() {
        val json = """
            {
              "auditLogs": [
                {
                  "id": "audit-1",
                  "userId": "user-1",
                  "action": "container.start",
                  "resourceType": "Container",
                  "resourceId": "container-1",
                  "metadata": { "source": "android" },
                  "createdAt": "2026-05-24T00:00:00.000Z",
                  "user": { "email": "admin@example.com", "role": "ADMIN" }
                }
              ]
            }
        """.trimIndent()

        val response = moshi.adapter(AuditLogsResponse::class.java).fromJson(json)!!

        assertEquals("audit-1", response.auditLogs.single().id)
        assertEquals("admin@example.com", response.auditLogs.single().user?.email)
    }
}
