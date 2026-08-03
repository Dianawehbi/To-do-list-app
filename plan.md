# Task Manager — Build Plan

A step-by-step guide for building all three pieces: `auth-service`, `task-service`, and `web-app`.

**Overall order of attack:** Auth service → Task service → Frontend.
Reason: the frontend needs both backends to test against, and task-service needs auth-service's introspection endpoint to work at all. Building bottom-up means you're never blocked waiting on a piece that isn't finished yet.

---

## Phase 0 — Setup (do this first, all three pieces)

- [✔️ ] Install Java 21, Maven, MySQL 8, Node.js, Angular CLI
- [✔️] Create a MySQL server locally with two schemas: `auth_db` and `task_db`
- [✔️ ] Create 3 project folders: `auth-service/`, `task-service/`, `web-app/`
- [ ✔️ ] Set up 2 separate Spring Boot projects via [start.spring.io](https://start.spring.io) with dependencies: Web, Data JPA, Validation, Security, MySQL Driver
- [✔️] Set up the Angular project: `ng new web-app --standalone --routing`

---

## Phase 1 — `auth-service` (build this first)

### 1.1 Database layer
- [ ✔️] Write `schema.sql` for `auth_db`: `users` table + `refresh_tokens` table (see column specs in the project doc — get types/constraints exactly right, especially `unique` on `username`/`email`/`token`)
- [✔️ ] Write `data.sql` with one seeded admin user (password must be BCrypt-hashed — you can generate this hash with a small throwaway script or an online BCrypt generator for now)
- [ ✔️] Set `spring.jpa.hibernate.ddl-auto=none` and `spring.sql.init.mode=always` in `application.properties`
- [ ✔️ ] Run the app once, confirm tables are created and the seed admin row exists

### 1.2 Entities & repositories
- [ ✔️] `User` entity (map to `users` table)
- [ ✔️] `RefreshToken` entity (map to `refresh_tokens` table)
- [ ✔️ ] `UserRepository`, `RefreshTokenRepository` (Spring Data JPA interfaces)

### 1.3 Security basics
- [✔️] Add a `PasswordEncoder` bean (BCrypt)
- [✔️] Decide on JWT vs opaque tokens for the access token (JWT is the common choice — lets you encode userId/role directly, though introspection means task-service never decodes it itself)
- [✔️] Build a `JwtService` (or equivalent) to generate/validate access tokens with a 15-minute expiry
- [✔️] Build refresh token generation/storage logic (7-day expiry, stored in `refresh_tokens`, revocable)

### 1.4 Endpoints — build and test each with Postman/curl before moving to the next
- [✔️] `POST /api/auth/login` — verify username/password, return access + refresh token. Disabled account → same error as wrong password (no distinguishing info)
- [✔️] `POST /api/auth/refresh` — exchange valid refresh token for new access token
- [✔️] `POST /api/auth/logout` — revoke the refresh token
- [✔️] `POST /api/auth/introspect` — **this is the most important endpoint**, since task-service depends on it entirely. Takes a token, returns `{active, userId, username, role, expiresAt}` or `{active: false}`. Always HTTP 200, never 401, even when invalid.
- [✔️] `GET /api/auth/me` — return the logged-in user's own profile
- [✔️] `POST /api/auth/register` (admin only) — create a new account
- [✔️] `GET /api/users`, `GET /api/users/{id}`, `PUT /api/users/{id}`, `DELETE /api/users/{id}` (soft-delete → `enabled = false`, admin only)

### 1.5 Lock down introspection
- [✔️] Add a shared internal credential check (`X-Internal-Key` header or basic auth) so `/api/auth/introspect` can't be called from outside the network
- [✔️] Confirm the raw token and password hash never appear in logs

**✅ Checkpoint:** you should be able to log in via Postman, get a token, call `/introspect` with it, and get back a valid user. That's the whole service working end to end.

---

## Phase 2 — `task-service`

### 2.1 Database layer
- [ ✔️ ] Write `schema.sql` for `task_db`: `categories` + `tasks` tables — remember `owner_user_id` is just a plain BIGINT column, **not** a real foreign key (it references a different service's data)
- [✔️ ] Add indexes on `owner_user_id` and `status` (mentioned explicitly since most queries filter by owner)
- [ ✔️] Same `ddl-auto=none` / `sql.init.mode=always` setup as auth-service

### 2.2 Entities & repositories
- [✔️] `Category` entity, `Task` entity
- [✔️] `CategoryRepository`, `TaskRepository` — use Spring Data JPA's `Pageable` support from the start, since every list endpoint needs server-side pagination

### 2.3 The introspection filter (the trickiest new concept here)
- [ ] Build a custom `OncePerRequestFilter` that:
  - Extracts the token from the `Authorization` header
  - Calls `auth-service`'s `/api/auth/introspect` (use `RestTemplate` or `WebClient` with a 2-second connect/read timeout)
  - If `active: true` → populate Spring Security's context with `userId` + `role` so the rest of the app treats the request as authenticated
  - If `active: false` → respond 401
  - If the call times out/fails to connect → respond 503 (not 401 — this distinction matters, see project doc section 7)
  - Cache the result briefly (60s, or less if the token expires sooner) so you're not calling auth-service on every single request

### 2.4 Business rules — enforce these in service-layer code, not just the DB
- [ ] Only the task's owner can read/update/delete it (admins can *view* any task, but that's it)
- [ ] Can't assign a task to a deactivated category
- [ ] Can't create a task with a past due date (but existing ones can keep one)
- [ ] Can't delete a category that's referenced by any task — only deactivate it
- [ ] Marking a task `DONE` updates its timestamp; `DONE` → `TODO` isn't allowed directly, must pass through `IN_PROGRESS`

### 2.5 Endpoints
- [ ] `GET /api/tasks?page=&size=&status=&priority=&categoryId=&search=` — always scoped to the caller's own tasks, from the token, never from the URL
- [ ] `POST /api/tasks`, `GET /api/tasks/{id}`, `PUT /api/tasks/{id}`, `PATCH /api/tasks/{id}/status`, `DELETE /api/tasks/{id}`
- [ ] `GET /api/categories?active=true`, `POST/PUT/DELETE /api/categories` (admin only)

### 2.6 Error handling
- [ ] One global `@ControllerAdvice` producing the standard error shape (`timestamp`, `status`, `error`, `message`, `path`, optional `fieldErrors`) — shared format across both services

**✅ Checkpoint:** with both services running, you should be able to log in via auth-service, take the token, and successfully create/list/update tasks via task-service — including getting a 401 with a bad token and a 503 if you stop auth-service.

---

## Phase 3 — `web-app` (Angular)

### 3.1 Project structure
- [ ] Standalone components, no NgModules
- [ ] Set up lazy-loaded feature routes: `login`, `tasks`, `categories`, `users`
- [ ] Build empty placeholder pages for each route first, just to confirm routing works

### 3.2 Auth foundation (build this before anything else in the frontend)
- [ ] `AuthService` — signal-based, holds current user + access token + refresh token (in memory, or `localStorage` for persistence across refresh)
- [ ] `/login` page — reactive form, calls `POST /api/auth/login`, stores tokens on success
- [ ] `authGuard` — redirects to `/login` if not logged in
- [ ] `adminGuard` — blocks non-admins from `/users`
- [ ] `authInterceptor` (functional, `HttpInterceptorFn`) — attaches `Authorization: Bearer <token>` to every request; on a 401, attempts one silent refresh via `/api/auth/refresh`; if that also fails, clears session and redirects to `/login`

**✅ Checkpoint:** you can log in, get redirected to `/tasks`, and refreshing the page keeps you logged in.

### 3.3 Tasks feature (the biggest page)
- [ ] `TaskService` (signals) — wraps HTTP calls to task-service
- [ ] `/tasks` — list with search, status/priority/category filters, server-side pagination, visible loading state, visible error state
- [ ] `/tasks/new` — typed reactive form, client-side validation mirroring backend rules (no past due dates, etc.)
- [ ] `/tasks/:id` — edit form, same validation, handles status transitions (blocking `DONE → TODO` directly in the UI too, as a nice-to-have)

### 3.4 Categories feature
- [ ] `/categories` — list, with create/edit controls only visible (`@if`) when the current user is admin

### 3.5 Users feature (admin only)
- [ ] `/users` — list, paginated, with enable/disable actions

### 3.6 Polish pass
- [ ] Confirm every data-fetching page has both a loading state and an error state
- [ ] Confirm pagination is genuinely server-side everywhere (not fetching everything and slicing client-side)
- [ ] Confirm all feature routes are lazy-loaded (check the network tab — separate chunks per route)

---



