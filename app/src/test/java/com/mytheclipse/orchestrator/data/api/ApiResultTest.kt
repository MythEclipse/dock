package com.mytheclipse.orchestrator.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultTest {
    @Test
    fun unauthorizedErrorIsRecognized() {
        val result: ApiResult<Unit> = ApiResult.Error(statusCode = 401, message = "Unauthorized")

        assertTrue(result is ApiResult.Error)
        assertEquals(401, (result as ApiResult.Error).statusCode)
        assertEquals("Unauthorized", result.message)
    }
}
