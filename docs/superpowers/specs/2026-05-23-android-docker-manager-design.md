# Android Docker Manager Design

## Goal
Build a full native Android version of the Docker Manager in `/mnt/code/pab/dock`, using the existing API at `https://docker.asepharyana.tech/api/openapi` and excluding API documentation from the mobile app.

The app must be production-functional, not dummy UI. It should support the same business capabilities as the web app: authentication, dashboard, node management, container management, Docker Hub image search, user management, and audit logs.

## Source context
The source web application in `/mnt/code/pab/docker-manager` is a Next.js Docker Manager with these API capabilities:

- `GET /api/openapi` exposes the OpenAPI document.
- Auth uses the same session/auth mechanism as the web app.
- Nodes:
  - `GET /api/nodes`
  - `POST /api/nodes`
  - `PATCH /api/nodes/{id}`
  - `DELETE /api/nodes/{id}`
  - `POST /api/nodes/{id}/sync`
  - `POST /api/nodes/{id}/containers/sync`
- Containers:
  - `GET /api/containers`
  - `POST /api/containers`
  - `GET /api/containers/{id}`
  - `DELETE /api/containers/{id}`
  - `POST /api/containers/{id}/start`
  - `POST /api/containers/{id}/stop`
  - `POST /api/containers/{id}/restart`
  - `GET /api/containers/{id}/logs`
- Docker Hub search:
  - `GET /api/docker-hub/search?q=...`
- Users:
  - `GET /api/users`
  - `POST /api/users`
  - `PATCH /api/users/{id}`
  - `DELETE /api/users/{id}`
- Audit logs:
  - `GET /api/audit-logs`

## Technical approach
Use native Kotlin Compose in the existing Android project. Do not use WebView. Do not scaffold dummy screens.

Recommended stack:

- Kotlin + Jetpack Compose
- Material 3
- Retrofit for typed HTTP calls
- OkHttp for cookie/session handling and network interception
- Kotlinx Serialization or Moshi/Gson for JSON mapping, selected based on existing Gradle compatibility during implementation
- AndroidX Navigation Compose
- ViewModels and StateFlow for screen state
- Secure local session storage for cookies/session data

The app is online-only. It will not include Room or offline cache. Screens fetch live data from the API and provide loading, error, retry, empty, and pull-to-refresh states.

## Architecture
Use a small layered structure that keeps UI independent from HTTP details:

- `data/api`
  - Retrofit service interfaces
  - API response models
  - OkHttp client configuration
  - auth/session cookie handling
- `data/model`
  - DTOs and domain-facing models for nodes, containers, users, audit logs, Docker Hub search results, auth state, and errors
- `data/repository`
  - `AuthRepository`
  - `NodeRepository`
  - `ContainerRepository`
  - `UserRepository`
  - `AuditLogRepository`
  - `DockerHubRepository`
- `session`
  - secure cookie/session persistence
  - login state restoration
  - logout cleanup
- `ui/navigation`
  - app route definitions and role-aware navigation
- `ui/screens`
  - Login, Dashboard, Nodes, Node Form/Edit, Containers, Container Create, Container Detail, Container Logs, Users, User Form/Edit, Audit Logs
- `ui/components`
  - metric cards, status chips, action sheets, confirmation dialogs, terminal log viewer, form fields, error panels, empty states

Mutation flow:

1. Screen dispatches user action to its ViewModel.
2. ViewModel calls the relevant repository.
3. Repository calls the API.
4. On success, ViewModel refreshes the relevant list/detail from the API.
5. On failure, ViewModel surfaces a typed UI error.

## Authentication
Authentication follows the same API/web auth behavior. The Android app must not invent a separate mobile-only auth flow.

Expected behavior:

- Login sends credentials to the same backend auth endpoint used by the web app.
- OkHttp captures and reuses session cookies.
- Session data is persisted securely so the user can reopen the app without logging in again.
- Logout clears local session state and returns to Login.
- Any `401 Unauthorized` response clears the stored session and redirects to Login.

During implementation, inspect the OpenAPI document and web auth route to determine the exact login endpoint and request shape.

## Screens and features

### Login
- Email/password login.
- Loading state while authenticating.
- API validation and unauthorized errors shown inline.
- Successful login enters the main app.
- Session restore on app start.

### Dashboard
- Summary cards for nodes and containers.
- Status counts for running/stopped containers and online/offline/unknown nodes.
- Node capacity overview using CPU/RAM fields available from node data.
- Quick actions for creating nodes, creating containers, and sync actions when the user role allows them.

### Nodes
- List nodes with name, status, IP/Portainer URL, CPU capacity, RAM capacity, and last synced timestamp.
- Create node with name, Portainer URL, Portainer username, and Portainer password.
- Edit node fields supported by the API: name, Portainer URL, Portainer username, Portainer password, and status.
- Delete node with confirmation.
- Show API conflict errors, such as trying to delete a node that still has containers.
- Sync node capacity.
- Admin-only sync containers for a node.

### Containers
- List containers available to the current role.
- Create container with node, name, image, CPU, RAM, and owner selection for admin users.
- Search Docker Hub images via `/api/docker-hub/search` when choosing an image.
- Detail screen for one container.
- Actions: start, stop, restart, delete.
- Each destructive or operational action uses a confirmation dialog and progress state.
- After each action, refresh the container data from the API.

### Container logs
- Fetch logs from `/api/containers/{id}/logs`.
- Present logs in a terminal-style viewer with monospace text, dark surface, refresh action, and copy action.

### Users
- Admin-only user management.
- List users.
- Create user with email, password, and role.
- Edit email, password, and role.
- Delete user with confirmation.
- Show conflict errors, such as duplicate email or deleting a user with owned resources.

### Audit logs
- Read-only audit log list for roles allowed by the API.
- Display actor/user, action, resource type, resource ID, timestamp, and metadata when present.
- Provide local filtering/search after data is loaded.

## Role behavior
The app trusts the API as the source of truth for authorization. UI should still improve UX by hiding unavailable actions when the current user role is known.

- Admin: all management screens and actions.
- Developer: container-focused actions allowed by the API.
- Auditor: read-only views allowed by the API.

If role data is not available locally, show features optimistically and handle `403 Forbidden` with a clear forbidden state.

## UI direction
Use a premium industrial control panel aesthetic rather than copying the web UI.

- Primary theme: dark graphite/charcoal surfaces.
- Accents:
  - green for running/online
  - amber for sync/warning
  - red for destructive/offline
  - cyan/blue for primary actions
- Material 3 components with custom shapes, spacing, and status styling.
- Dashboard emphasizes large readable metrics, status rings, and node capacity cards.
- Bottom navigation exposes main areas: Dashboard, Nodes, Containers, Users, Audit Logs, adjusted by role and available space.
- Forms are mobile-first with clear validation, grouped sections, and sticky submit actions.
- Container logs use a terminal-like visual treatment.
- Error and empty states are intentional, not placeholders.

## Error handling
Use consistent error handling across repositories and ViewModels:

- `401 Unauthorized`: clear session and navigate to Login.
- `403 Forbidden`: show permission message and avoid retry loops.
- `400 Bad Request`: show validation message near the relevant form.
- `409 Conflict`: show conflict message from the API.
- `5xx` or network failure: show retryable error panel.
- Unknown response shape: show a safe generic error with retry.

## Testing and verification
Automated tests should focus on behavior that can break without UI screenshots:

- Repository/API mapper tests for successful responses and error envelopes.
- ViewModel state tests for loading, success, empty, validation error, forbidden, unauthorized, and retry states.
- UI tests for core rendering states where practical.

Manual verification must use the real API and cover:

1. Login and session restore.
2. Dashboard data loading.
3. Node list, create, edit, sync, delete.
4. Container list, create with Docker Hub search, start, stop, restart, logs, delete.
5. User list, create, edit, delete as admin.
6. Audit logs list.
7. Unauthorized logout behavior.
8. Forbidden behavior for restricted roles.

## Out of scope
- API docs inside the mobile app.
- WebView wrapper.
- Offline cache/read-only mode.
- Dummy screens or mocked data as the final implementation.
- New backend endpoints unless the existing API lacks a required auth or user-info capability discovered during implementation.
