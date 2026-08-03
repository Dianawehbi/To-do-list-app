# Task Manager — Project Overview

We're building a simple to-do app. People log in, add tasks, sort them into categories, and mark them complete when they're done.

There are three separate pieces that work together:

---

## 1. The moving parts

| Piece | Built with | Runs on | What it's for |
|---|---|---|---|
| `web-app` | Angular | port 4200 | The interface people actually see and use. It doesn't make any decisions itself — it just asks the other two pieces for information. |
| `auth-service` | Spring Boot | port 8081 | Handles logins and knows who everyone is. It's the only piece that decides whether someone really is who they say they are. |
| `task-service` | Spring Boot | port 8082 | Handles all the tasks and categories. Whenever it needs to know who's making a request, it checks with `auth-service` rather than figuring that out itself. |

Each backend piece has its own database (`auth_db` and `task_db`), and neither one reads the other's tables directly — if one needs something from the other, it asks over the network.

```
                 ┌──────────────┐
                 │   web-app    │
                 │   (Angular)  │
                 └──────┬───────┘
                        │
          fep/profile │ tasks, categories
              ┌─────────┴─────────┐
              ▼                   ▼
     ┌────────────────┐   ┌────────────────┐
     │  auth-service  │◄──│  task-service  │
     │                │   │                │
     │  users         │   │  tasks         │
     │  tokens        │   │  categories    │
     └───────┬────────┘   └───────┬────────┘
             │                    │
         ┌───▼────┐           ┌───▼────┐
         │auth_db │           │task_db │
         └────────┘           └────────┘
```

---

## 2. What we're building it with

**Backend (same stack for both services)**
- Java 21 and Spring Boot 3.x, built with Maven
- The standard Spring set: Web, Data JPA, Validation, Security
- MySQL 8 as the database, one server split into two schemas (`auth_db` and `task_db`)

**How the tables get created**

Each service has its own `schema.sql` (the table definitions) and `data.sql` (a few starter rows). Spring runs these automatically on startup:

```properties
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
```

In other words, Hibernate isn't allowed to generate or alter the database structure. The SQL files are the source of truth, and they're written so the app can restart without anything breaking.

**Frontend**
- Angular 19 or later, using standalone components rather than the older NgModule approach
- Routes load lazily as people navigate the app
- Typed reactive forms
- Signals for tracking component state
- A functional interceptor (`HttpInterceptorFn`) that attaches the login token to outgoing requests
- Any UI component library is fine — visual polish isn't a requirement here

**Getting it running**
- 2 Tomcat services each running a service (auth-service on the first tomcat instance on port 8081, and task-service on the other tomat instance on port 8082)

---

## 3. Who can do what

There are two types of users.

- **Regular users** — manage their own tasks. They can't see anyone else's tasks and can't manage accounts.
- **Admins** — everything a regular user can do, plus creating, listing, updating, and disabling accounts, and managing the shared list of categories.

---

## 4. What the data looks like

### `auth_db` — belongs to `auth-service`

**`users`**

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | primary key, auto-increments |
| `username` | VARCHAR(50) | unique, required |
| `email` | VARCHAR(120) | unique, required |
| `password_hash` | VARCHAR(120) | required, BCrypt-hashed — never stored in plain text |
| `role` | VARCHAR(20) | either `USER` or `ADMIN` |
| `enabled` | TINYINT(1) | required, defaults to on |
| `created_at` | DATETIME | required |

**`refresh_tokens`**

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | primary key, auto-increments |
| `user_id` | BIGINT | points back to `users` |
| `token` | VARCHAR(255) | unique, required |
| `expires_at` | DATETIME | required |
| `revoked` | TINYINT(1) | required, defaults to off |

One admin account is included from the start, with an already-hashed password.

### `task_db` — belongs to `task-service`

**`categories`**

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | primary key, auto-increments |
| `name` | VARCHAR(60) | unique, required |
| `color` | VARCHAR(7) | a hex color code, like `#3b82f6` |
| `active` | TINYINT(1) | required, defaults to on |

**`tasks`**

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | primary key, auto-increments |
| `title` | VARCHAR(150) | required |
| `description` | VARCHAR(1000) | optional |
| `status` | VARCHAR(20) | `TODO`, `IN_PROGRESS`, or `DONE` |
| `priority` | VARCHAR(10) | `LOW`, `MEDIUM`, or `HIGH` |
| `due_date` | DATE | optional |
| `category_id` | BIGINT | points to `categories`, optional |
| `owner_user_id` | BIGINT | the ID of the owning user, from the auth side. Not a real foreign key, since it belongs to a different service's data |
| `created_at` | DATETIME | required |
| `updated_at` | DATETIME | required |

We index `owner_user_id` and `status`, since nearly every task lookup filters by owner.

### The ground rules

These are enforced in the `task-service` application code, not just left to the database:

1. Only the owner of a task can read, update, or delete it. Admins can view any task, but that doesn't make it theirs.
2. A task can't be assigned to a category that's been deactivated.
3. A task can't be created with a due date in the past — but an existing task is allowed to keep one if it's already there.
4. A category that's referenced by at least one task can't be deleted — it can only be deactivated.
5. Marking a task `DONE` updates its timestamp, and once it's `DONE` it can't move directly back to `TODO` — it has to pass through `IN_PROGRESS` first.

---

## 5. `auth-service`

This is the only piece that understands login tokens — it issues them, and it's the only one that can confirm whether one is still valid.

### Endpoints

| Method | Path | Who can call it | What it does |
|---|---|---|---|
| POST | `/api/auth/register` | admin only | Creates a new account |
| POST | `/api/auth/login` | anyone | Returns an access token and a refresh token |
| POST | `/api/auth/refresh` | anyone | Exchanges a refresh token for a new access token |
| POST | `/api/auth/logout` | logged in | Revokes the refresh token |
| POST | `/api/auth/introspect` | internal only | Checks whether a token is valid — covered in more detail below |
| GET | `/api/auth/me` | logged in | Returns the current user's own profile |
| GET | `/api/users` | admin only | List of users, paginated |
| GET | `/api/users/{id}` | admin only | A single user's details |
| PUT | `/api/users/{id}` | admin only | Updates a user |
| DELETE | `/api/users/{id}` | admin only | Sets `enabled` to false — the row itself is never deleted |

### Rules around tokens

- Access tokens last 15 minutes.
- Refresh tokens last 7 days, and are stored so they can be revoked if needed.
- Passwords are hashed with BCrypt. The hash is never returned in a response and never written to a log.
- Logging into a disabled account fails the same way a wrong password would — same message, same status code — so there's no way to tell the difference from outside.

---

## 6. `task-service`

Owns everything to do with tasks and categories. It doesn't parse tokens, has no signing key, and has no users table.

### Endpoints

```
GET    /api/tasks?page=0&size=20&status=&priority=&categoryId=&search=
POST   /api/tasks
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
PATCH  /api/tasks/{id}/status        { "status": "DONE" }
DELETE /api/tasks/{id}

GET    /api/categories?active=true
POST   /api/categories               admin only
GET    /api/categories/{id}
PUT    /api/categories/{id}          admin only
DELETE /api/categories/{id}          admin only
```

`GET /api/tasks` only ever returns the caller's own tasks. Who the caller is comes from their token, never from anything in the URL — so nobody can change an ID and see someone else's list.

Every list endpoint is paginated on the server side, using Spring's standard paging format.

### When something goes wrong

Both services return errors in the same shape, produced from a single place:

```json
{
  "timestamp": "2026-07-27T10:15:30Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/tasks",
  "fieldErrors": {
    "title": "must not be blank"
  }
}
```

`fieldErrors` only appears when it's a validation failure.

---

## 7. How the two backend services talk to each other

`task-service` treats the login token as an opaque value — it doesn't try to read it. Instead, on every request, it asks `auth-service` whether the token is valid and who it belongs to.

### The verification call

`POST http://auth-service:8081/api/auth/introspect`

Request:
```json
{ "token": "<raw token from the Authorization header>" }
```

If the token is valid, the response looks like this:
```json
{
  "active": true,
  "userId": 42,
  "username": "jdoe",
  "role": "USER",
  "expiresAt": "2026-07-27T10:30:00Z"
}
```

If it's expired, malformed, revoked, or belongs to a disabled account:
```json
{ "active": false }
```

Both responses come back as a normal HTTP 200 — an invalid token is a valid answer to the question, not a failed call.

### What `task-service` does with that answer

- A filter (`OncePerRequestFilter`) in `task-service` extracts the token, calls the verification endpoint, and if the response is `active: true`, populates Spring Security with the returned `userId` and `role`. From there, the rest of the app treats the request as authenticated normally.
- If the response is `active: false`, that's a 401.
- If the verification call times out or fails to connect, that results in a 503, not a 401 — being unable to verify someone's identity is a different situation from that person being unauthorized.
- Connect and read timeouts are set on that call; two seconds is enough.
- The result is cached briefly (60 seconds, or less if the token expires sooner), so the same request isn't verified repeatedly on every page load.
- The verification endpoint itself is locked down — it's only reachable with a shared internal credential (an `X-Internal-Key` header or basic auth), so it can't be called from outside the network.
- The raw token is never written to a log by either service.

### The tradeoff worth understanding

If `auth-service` is down, `task-service` can't serve any protected request — that's a deliberate part of the design. Identity lives in exactly one place, so disabling a user takes effect quickly, and there's no signing key shared across services. The tradeoff is that `task-service` now depends on `auth-service` being available, which is why the caching and timeouts matter.

---

## 8. The frontend

### Pages

| Route | Who can see it | What's there |
|---|---|---|
| `/login` | anyone | Username and password form |
| `/tasks` | logged in | Task list, with search, filters, and pagination |
| `/tasks/new` | logged in | Form to create a task |
| `/tasks/:id` | logged in | Form to edit a task |
| `/categories` | logged in | Category list — create/edit controls are visible to admins only |
| `/users` | admin only | User management |

### What it needs to do

- A route guard redirects anyone who isn't logged in to `/login`.
- Another guard keeps non-admins off admin-only pages.
- An interceptor attaches `: Bearer <token>` to every outgoing request. If a request comes back with a 401, it attempts one silent refresh — and if that fails too, it clears the session and redirects to `/login`.
- The forms validate on the client using the same rules the backend enforces, but that's a convenience — the backend check is what actually matters.
- Pagination happens on the server; the app isn't loading everything and paginating it in the browser.
- Every page that loads data shows a visible loading state and a visible error state.
- Feature areas load lazily rather than all at once.

### What's out of scope

Visual design, dark mode, animations, translations, mobile layouts, and offline support are not part of this project.Authorization