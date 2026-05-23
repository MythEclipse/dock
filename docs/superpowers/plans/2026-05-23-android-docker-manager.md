# Android Docker Manager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a full native Android Docker Manager app that talks to `https://docker.asepharyana.tech/api` and implements login, dashboard, nodes, containers, users, and audit logs.

**Architecture:** Keep the app as a Kotlin Compose single-activity app with small layers: API client, repositories, ViewModels, navigation, and reusable UI components. The app is online-only; every screen loads from the API and shows loading, empty, error, retry, and success states.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel/StateFlow, Retrofit, OkHttp, Moshi, AndroidX Security Crypto, JUnit.

---

## File structure

### Modify
- `gradle/libs.versions.toml` — add library aliases for Retrofit, OkHttp, Moshi, Navigation Compose, Lifecycle ViewModel Compose, Security Crypto, and coroutines test.
- `app/build.gradle.kts` — enable Kotlin Android plugin if needed and add dependencies.
- `app/src/main/AndroidManifest.xml` — add `INTERNET` permission.
- `app/src/main/java/com/mytheclipse/orchestrator/MainActivity.kt` — replace starter greeting with app bootstrap.
- `app/src/main/java/com/mytheclipse/orchestrator/ui/theme/Color.kt` — replace default purple palette.
- `app/src/main/java/com/mytheclipse/orchestrator/ui/theme/Theme.kt` — make dark industrial Material 3 theme default.

### Create
- `app/src/main/java/com/mytheclipse/orchestrator/AppContainer.kt` — dependency container for API services, repositories, and session store.
- `app/src/main/java/com/mytheclipse/orchestrator/AppRoot.kt` — top-level Compose root.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiConfig.kt` — base URL constants.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiResult.kt` — success/error result model.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiModels.kt` — DTOs for auth, nodes, containers, users, audit logs, Docker Hub, and response wrappers.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/DockerManagerApi.kt` — Retrofit interface.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/NextAuthApi.kt` — auth endpoint interface after verifying endpoint shape.
- `app/src/main/java/com/mytheclipse/orchestrator/data/api/NetworkModule.kt` — Retrofit, Moshi, OkHttp, cookie jar, and error parser.
- `app/src/main/java/com/mytheclipse/orchestrator/session/SessionCookieStore.kt` — persisted session cookie jar.
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/AuthRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/NodeRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/ContainerRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/UserRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/AuditLogRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/data/repository/DashboardRepository.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/state/UiState.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/navigation/AppDestination.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/navigation/AppNavGraph.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/components/StatusChip.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/components/MetricCard.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/components/ErrorState.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/components/ConfirmDialog.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/components/TerminalLogViewer.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/login/LoginViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/login/LoginScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/dashboard/DashboardViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/nodes/NodesViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/nodes/NodesScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/containers/ContainersViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/containers/ContainersScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/containers/ContainerLogsScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/users/UsersViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/users/UsersScreen.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/audit/AuditLogsViewModel.kt`
- `app/src/main/java/com/mytheclipse/orchestrator/ui/screens/audit/AuditLogsScreen.kt`
- `app/src/test/java/com/mytheclipse/orchestrator/data/api/ApiResultTest.kt`
- `app/src/test/java/com/mytheclipse/orchestrator/data/repository/DashboardRepositoryTest.kt`

---

### Task 1: Gradle dependencies and network permission

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add dependency aliases**

In `gradle/libs.versions.toml`, add these versions under `[versions]`:

```toml
retrofit = "3.0.0"
okhttp = "5.3.2"
moshi = "1.15.2"
navigationCompose = "2.9.6"
lifecycleViewmodelCompose = "2.10.0"
securityCrypto = "1.1.0"
coroutines = "1.10.2"
```

Add these entries under `[libraries]`:

```toml
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
moshi = { group = "com.squareup.moshi", name = "moshi", version.ref = "moshi" }
moshi-kotlin = { group = "com.squareup.moshi", name = "moshi-kotlin", version.ref = "moshi" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

- [ ] **Step 2: Add app dependencies**

In `app/build.gradle.kts`, add these dependencies:

```kotlin
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.androidx.security.crypto)
implementation(libs.retrofit)
implementation(libs.retrofit.moshi)
implementation(libs.okhttp)
implementation(libs.okhttp.logging)
implementation(libs.moshi)
implementation(libs.moshi.kotlin)
testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Add internet permission**

In `app/src/main/AndroidManifest.xml`, add this directly under `<manifest ...>` and before `<application>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

- [ ] **Step 4: Verify Gradle sync/build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds or fails only because a declared dependency version is unavailable. If a dependency version is unavailable, choose the closest stable version from Maven Central and keep the alias names unchanged.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml
git commit -m "Add Android app networking dependencies"
```

---

### Task 2: API models, result type, and Retrofit interface

**Files:**
- Create: `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiConfig.kt`
- Create: `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiResult.kt`
- Create: `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiModels.kt`
- Create: `app/src/main/java/com/mytheclipse/orchestrator/data/api/DockerManagerApi.kt`
- Test: `app/src/test/java/com/mytheclipse/orchestrator/data/api/ApiResultTest.kt`

- [ ] **Step 1: Write ApiResult test**

Create `app/src/test/java/com/mytheclipse/orchestrator/data/api/ApiResultTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mytheclipse.orchestrator.data.api.ApiResultTest"
```

Expected: FAIL because `ApiResult` does not exist.

- [ ] **Step 3: Add API config and result**

Create `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiConfig.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.api

object ApiConfig {
    const val BaseUrl = "https://docker.asepharyana.tech/"
}
```

Create `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiResult.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.api

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val statusCode: Int?, val message: String) : ApiResult<Nothing>
}
```

- [ ] **Step 4: Add DTO models**

Create `app/src/main/java/com/mytheclipse/orchestrator/data/api/ApiModels.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class ApiErrorBody(val error: String? = null, val message: String? = null)

@JsonClass(generateAdapter = false)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = false)
data class UserDto(val id: String, val email: String, val role: String, val createdAt: String? = null)

@JsonClass(generateAdapter = false)
data class UsersResponse(val users: List<UserDto>)

@JsonClass(generateAdapter = false)
data class UserResponse(val user: UserDto)

@JsonClass(generateAdapter = false)
data class CreateUserRequest(val email: String, val password: String, val role: String)

@JsonClass(generateAdapter = false)
data class UpdateUserRequest(val email: String? = null, val password: String? = null, val role: String? = null)

@JsonClass(generateAdapter = false)
data class NodeDto(
    val id: String,
    val name: String,
    val ipAddress: String? = null,
    val portainerUrl: String,
    val portainerUsername: String? = null,
    val portainerEndpointId: Int? = null,
    val cpuCapacity: Int? = null,
    val ramCapacityMb: Int? = null,
    val status: String,
    val lastSyncedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@JsonClass(generateAdapter = false)
data class NodesResponse(val nodes: List<NodeDto>)

@JsonClass(generateAdapter = false)
data class NodeResponse(val node: NodeDto)

@JsonClass(generateAdapter = false)
data class CreateNodeRequest(val name: String, val portainerUrl: String, val portainerUsername: String, val portainerPassword: String)

@JsonClass(generateAdapter = false)
data class UpdateNodeRequest(
    val name: String? = null,
    val portainerUrl: String? = null,
    val portainerUsername: String? = null,
    val portainerPassword: String? = null,
    val status: String? = null,
)

@JsonClass(generateAdapter = false)
data class ContainerDto(
    val id: String,
    val name: String,
    val image: String,
    val status: String,
    val dockerContainerId: String? = null,
    val nodeId: String? = null,
    val ownerId: String? = null,
    val cpu: Int? = null,
    val ramMb: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val node: NodeDto? = null,
    val owner: UserDto? = null,
)

@JsonClass(generateAdapter = false)
data class ContainersResponse(val containers: List<ContainerDto>)

@JsonClass(generateAdapter = false)
data class ContainerResponse(val container: ContainerDto)

@JsonClass(generateAdapter = false)
data class CreateContainerRequest(val nodeId: String, val name: String, val image: String, val cpu: Int, val ramMb: Int, val ownerId: String? = null)

@JsonClass(generateAdapter = false)
data class LogsResponse(val logs: String)

@JsonClass(generateAdapter = false)
data class DockerHubSearchResponse(val results: List<DockerHubImageDto>)

@JsonClass(generateAdapter = false)
data class DockerHubImageDto(val name: String, val description: String, val starCount: Int, val pullCount: Int, val isOfficial: Boolean)

@JsonClass(generateAdapter = false)
data class AuditLogsResponse(val auditLogs: List<AuditLogDto>)

@JsonClass(generateAdapter = false)
data class AuditLogDto(
    val id: String? = null,
    val userId: String? = null,
    val action: String,
    val resourceType: String,
    val resourceId: String? = null,
    val metadata: Map<String, Any?>? = null,
    val createdAt: String? = null,
    val user: UserDto? = null,
)

@JsonClass(generateAdapter = false)
data class OkResponse(val ok: Boolean)

@JsonClass(generateAdapter = false)
data class SyncContainersResponse(val message: String, val syncedContainerIds: List<String>)
```

- [ ] **Step 5: Add Retrofit interface**

Create `app/src/main/java/com/mytheclipse/orchestrator/data/api/DockerManagerApi.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DockerManagerApi {
    @GET("api/nodes")
    suspend fun getNodes(): Response<NodesResponse>

    @POST("api/nodes")
    suspend fun createNode(@Body request: CreateNodeRequest): Response<NodeResponse>

    @PATCH("api/nodes/{id}")
    suspend fun updateNode(@Path("id") id: String, @Body request: UpdateNodeRequest): Response<NodeResponse>

    @DELETE("api/nodes/{id}")
    suspend fun deleteNode(@Path("id") id: String): Response<OkResponse>

    @POST("api/nodes/{id}/sync")
    suspend fun syncNode(@Path("id") id: String): Response<NodeResponse>

    @POST("api/nodes/{id}/containers/sync")
    suspend fun syncNodeContainers(@Path("id") id: String): Response<SyncContainersResponse>

    @GET("api/containers")
    suspend fun getContainers(): Response<ContainersResponse>

    @POST("api/containers")
    suspend fun createContainer(@Body request: CreateContainerRequest): Response<ContainerResponse>

    @GET("api/containers/{id}")
    suspend fun getContainer(@Path("id") id: String): Response<ContainerResponse>

    @DELETE("api/containers/{id}")
    suspend fun deleteContainer(@Path("id") id: String): Response<OkResponse>

    @POST("api/containers/{id}/start")
    suspend fun startContainer(@Path("id") id: String): Response<ContainerResponse>

    @POST("api/containers/{id}/stop")
    suspend fun stopContainer(@Path("id") id: String): Response<ContainerResponse>

    @POST("api/containers/{id}/restart")
    suspend fun restartContainer(@Path("id") id: String): Response<ContainerResponse>

    @GET("api/containers/{id}/logs")
    suspend fun getContainerLogs(@Path("id") id: String): Response<LogsResponse>

    @GET("api/docker-hub/search")
    suspend fun searchDockerHub(@Query("q") query: String): Response<DockerHubSearchResponse>

    @GET("api/users")
    suspend fun getUsers(): Response<UsersResponse>

    @POST("api/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserResponse>

    @PATCH("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): Response<UserResponse>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<UserResponse>

    @GET("api/audit-logs")
    suspend fun getAuditLogs(): Response<AuditLogsResponse>
}
```

- [ ] **Step 6: Run test and compile**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/data/api app/src/test/java/com/mytheclipse/orchestrator/data/api
git commit -m "Add Docker Manager API contracts"
```

---

### Task 3: Network module and session cookie persistence

**Files:**
- Create: `app/src/main/java/com/mytheclipse/orchestrator/session/SessionCookieStore.kt`
- Create: `app/src/main/java/com/mytheclipse/orchestrator/data/api/NetworkModule.kt`

- [ ] **Step 1: Implement secure cookie store**

Create `app/src/main/java/com/mytheclipse/orchestrator/session/SessionCookieStore.kt`:

```kotlin
package com.mytheclipse.orchestrator.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class SessionCookieStore(context: Context) : CookieJar {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "docker_manager_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookie ->
            prefs.edit().putString(cookie.name, cookie.toString()).apply()
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return prefs.all.values.mapNotNull { value ->
            Cookie.parse(url, value.toString())
        }
    }

    fun hasSession(): Boolean = prefs.all.isNotEmpty()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
```

- [ ] **Step 2: Implement network module**

Create `app/src/main/java/com/mytheclipse/orchestrator/data/api/NetworkModule.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.api

import com.mytheclipse.orchestrator.session.SessionCookieStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkModule(private val sessionCookieStore: SessionCookieStore) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val okHttp = OkHttpClient.Builder()
        .cookieJar(sessionCookieStore)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BaseUrl)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val dockerManagerApi: DockerManagerApi = retrofit.create(DockerManagerApi::class.java)

    fun <T> parseResponse(response: Response<T>): ApiResult<T> {
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            ApiResult.Success(body)
        } else {
            ApiResult.Error(response.code(), parseError(response.errorBody()))
        }
    }

    private fun parseError(errorBody: ResponseBody?): String {
        val raw = errorBody?.string().orEmpty()
        if (raw.isBlank()) return "Request failed"
        return runCatching {
            val adapter = moshi.adapter(ApiErrorBody::class.java)
            val parsed = adapter.fromJson(raw)
            parsed?.error ?: parsed?.message ?: raw
        }.getOrDefault(raw)
    }
}
```

- [ ] **Step 3: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/session app/src/main/java/com/mytheclipse/orchestrator/data/api/NetworkModule.kt
git commit -m "Add network module and session cookies"
```

---

### Task 4: Repositories and dashboard aggregation

**Files:**
- Create repository files listed above
- Test: `app/src/test/java/com/mytheclipse/orchestrator/data/repository/DashboardRepositoryTest.kt`

- [ ] **Step 1: Write dashboard repository test**

Create `app/src/test/java/com/mytheclipse/orchestrator/data/repository/DashboardRepositoryTest.kt`:

```kotlin
package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.NodeDto
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardRepositoryTest {
    @Test
    fun summaryCountsNodesAndContainersByStatus() {
        val summary = DashboardRepository.summaryFrom(
            nodes = listOf(
                NodeDto(id = "n1", name = "Node 1", portainerUrl = "https://p1", status = "online"),
                NodeDto(id = "n2", name = "Node 2", portainerUrl = "https://p2", status = "offline"),
            ),
            containers = listOf(
                ContainerDto(id = "c1", name = "A", image = "nginx", status = "running"),
                ContainerDto(id = "c2", name = "B", image = "redis", status = "stopped"),
            ),
        )

        assertEquals(2, summary.nodeCount)
        assertEquals(1, summary.onlineNodeCount)
        assertEquals(2, summary.containerCount)
        assertEquals(1, summary.runningContainerCount)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :app:testDebugUnitTest --tests "com.mytheclipse.orchestrator.data.repository.DashboardRepositoryTest"
```

Expected: FAIL because `DashboardRepository` does not exist.

- [ ] **Step 3: Implement repositories**

Create repositories with one method per API call. Use this pattern in each repository method:

```kotlin
return runCatching { network.parseResponse(apiCall()) }
    .getOrElse { ApiResult.Error(null, it.message ?: "Network request failed") }
```

Create `DashboardRepository.kt` with:

```kotlin
package com.mytheclipse.orchestrator.data.repository

import com.mytheclipse.orchestrator.data.api.ContainerDto
import com.mytheclipse.orchestrator.data.api.NodeDto

data class DashboardSummary(
    val nodeCount: Int,
    val onlineNodeCount: Int,
    val containerCount: Int,
    val runningContainerCount: Int,
)

class DashboardRepository {
    companion object {
        fun summaryFrom(nodes: List<NodeDto>, containers: List<ContainerDto>): DashboardSummary {
            return DashboardSummary(
                nodeCount = nodes.size,
                onlineNodeCount = nodes.count { it.status.equals("online", ignoreCase = true) },
                containerCount = containers.size,
                runningContainerCount = containers.count { it.status.equals("running", ignoreCase = true) },
            )
        }
    }
}
```

- [ ] **Step 4: Implement API-backed repository methods**

Create each repository as a small wrapper around `DockerManagerApi` and `NetworkModule.parseResponse`. Required methods:

- `NodeRepository`: `list`, `create`, `update`, `delete`, `sync`, `syncContainers`
- `ContainerRepository`: `list`, `create`, `get`, `delete`, `start`, `stop`, `restart`, `logs`, `searchImages`
- `UserRepository`: `list`, `create`, `update`, `delete`
- `AuditLogRepository`: `list`

- [ ] **Step 5: Run tests and compile**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/data/repository app/src/test/java/com/mytheclipse/orchestrator/data/repository
git commit -m "Add Docker Manager repositories"
```

---

### Task 5: Theme and reusable UI components

**Files:**
- Modify: `ui/theme/Color.kt`
- Modify: `ui/theme/Theme.kt`
- Create UI components listed above

- [ ] **Step 1: Replace color palette**

Use named colors in `Color.kt`:

```kotlin
val Graphite = Color(0xFF0B1117)
val Panel = Color(0xFF111A22)
val PanelHigh = Color(0xFF182430)
val Cyan = Color(0xFF35C2FF)
val OnlineGreen = Color(0xFF34D399)
val WarningAmber = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)
val TextPrimary = Color(0xFFE5EEF7)
val TextMuted = Color(0xFF91A4B7)
```

- [ ] **Step 2: Make dark theme default**

Update `DockorchTheme` so `darkTheme` defaults to `true`, `dynamicColor` defaults to `false`, and `darkColorScheme` uses the palette above.

- [ ] **Step 3: Add components**

Create `StatusChip`, `MetricCard`, `ErrorState`, `ConfirmDialog`, and `TerminalLogViewer` as small composables. Each component must take plain values and callbacks, not repositories or ViewModels.

- [ ] **Step 4: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/ui/theme app/src/main/java/com/mytheclipse/orchestrator/ui/components
git commit -m "Add industrial Android UI foundation"
```

---

### Task 6: App container, root, login, and navigation shell

**Files:**
- Create: `AppContainer.kt`, `AppRoot.kt`, navigation files, login files
- Modify: `MainActivity.kt`

- [ ] **Step 1: Implement dependency container**

Create `AppContainer` that constructs `SessionCookieStore`, `NetworkModule`, API, and repositories once from `applicationContext`.

- [ ] **Step 2: Implement login ViewModel**

`LoginViewModel` state must include `email`, `password`, `isLoading`, `error`, and `isLoggedIn`. It should call `AuthRepository.login(email, password)` and route success/failure into state.

- [ ] **Step 3: Implement LoginScreen**

Build a dark premium login form with email/password inputs, loading button, and inline error.

- [ ] **Step 4: Implement navigation shell**

Create destinations for login, dashboard, nodes, containers, users, audit logs, and container logs. Add bottom navigation for main authenticated screens.

- [ ] **Step 5: Replace MainActivity starter UI**

`MainActivity` should call:

```kotlin
setContent {
    DockorchTheme {
        AppRoot(appContainer = AppContainer(applicationContext))
    }
}
```

- [ ] **Step 6: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/MainActivity.kt app/src/main/java/com/mytheclipse/orchestrator/AppContainer.kt app/src/main/java/com/mytheclipse/orchestrator/AppRoot.kt app/src/main/java/com/mytheclipse/orchestrator/ui/navigation app/src/main/java/com/mytheclipse/orchestrator/ui/screens/login
git commit -m "Add login and navigation shell"
```

---

### Task 7: Dashboard, nodes, and node forms

**Files:**
- Create/modify dashboard and nodes screen files

- [ ] **Step 1: Implement DashboardViewModel and screen**

Load nodes and containers in parallel or sequentially, then show `DashboardSummary`. Include retry and pull-to-refresh.

- [ ] **Step 2: Implement NodesViewModel**

State must include list, loading, error, form state, selected node, and action progress. Methods: `load`, `create`, `update`, `delete`, `sync`, `syncContainers`.

- [ ] **Step 3: Implement NodesScreen**

Show node cards, create/edit dialog or form sheet, sync actions, delete confirmation, and clear API errors.

- [ ] **Step 4: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/ui/screens/dashboard app/src/main/java/com/mytheclipse/orchestrator/ui/screens/nodes
git commit -m "Add dashboard and node management"
```

---

### Task 8: Containers, Docker Hub search, actions, and logs

**Files:**
- Create/modify containers screen files

- [ ] **Step 1: Implement ContainersViewModel**

State must include containers, nodes for create form, users for admin owner selection if available, image search results, selected container, logs, loading/error/action state. Methods: `load`, `create`, `searchImages`, `start`, `stop`, `restart`, `delete`, `loadLogs`.

- [ ] **Step 2: Implement ContainersScreen**

Show container cards, status chip, node/owner context, create form, image search, action menu, and delete confirmation.

- [ ] **Step 3: Implement ContainerLogsScreen**

Load logs by container id and display via `TerminalLogViewer` with refresh and copy.

- [ ] **Step 4: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/ui/screens/containers
git commit -m "Add container management and logs"
```

---

### Task 9: Users and audit logs

**Files:**
- Create/modify users and audit screen files

- [ ] **Step 1: Implement UsersViewModel and screen**

Support list, create, edit, delete, loading/error/action states, role selector, duplicate email errors, and delete conflict errors.

- [ ] **Step 2: Implement AuditLogsViewModel and screen**

Support list, loading/error/empty states, local search/filter by action, resource type, resource id, or user email.

- [ ] **Step 3: Run compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/mytheclipse/orchestrator/ui/screens/users app/src/main/java/com/mytheclipse/orchestrator/ui/screens/audit
git commit -m "Add user management and audit logs"
```

---

### Task 10: Auth endpoint verification and real API smoke test

**Files:**
- Modify auth API/repository files as needed after verifying exact auth flow

- [ ] **Step 1: Inspect OpenAPI/auth endpoint**

Fetch or inspect `https://docker.asepharyana.tech/api/openapi` and the web auth route. Confirm exact login endpoint and request fields. If the API uses NextAuth credentials callback, implement that callback flow in `NextAuthApi` and `AuthRepository`.

- [ ] **Step 2: Run app against real API**

Run:

```bash
./gradlew :app:installDebug
```

Open the app on an emulator/device and test real login.

- [ ] **Step 3: Verify golden path manually**

Confirm these work with real API credentials:

1. Login and app reopen session restore.
2. Dashboard loads.
3. Nodes list loads.
4. Containers list loads.
5. Container logs load.
6. Users screen handles admin or forbidden role correctly.
7. Audit logs screen handles allowed or forbidden role correctly.
8. Logout returns to login.

- [ ] **Step 4: Run final checks**

Run:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java app/src/test/java
git commit -m "Wire Android app to Docker Manager API"
```

---

## Self-review notes

- Spec coverage: auth, dashboard, nodes, containers, Docker Hub search, users, audit logs, online-only behavior, role behavior, dark industrial UI, error handling, and verification are covered.
- No known placeholders remain in this plan.
- The only intentionally deferred discovery is the exact NextAuth login request shape; Task 10 makes this explicit because the source API routes do not expose a simple `/api/login` route.
