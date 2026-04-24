# BookShare

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.5.5-brightgreen.svg)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?logo=docker)
![Build](https://github.com/smzoha/bookshare/actions/workflows/gradle.yml/badge.svg)
![License: GPL v2](https://img.shields.io/badge/License-GPL_v2-blue.svg)
![Status](https://img.shields.io/badge/Status-Development-yellow)

BookShare is a social reading platform where users can track books, log reading progress, write reviews, organize personal shelves, and follow friends' reading activity — all in one place.

---

## Table of Contents

- [Features](#features)
- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Setup](#local-setup)
  - [Running with Docker](#running-with-docker)
  - [Seed Data](#seed-data)
  - [Gmail API & Google OAuth2](#gmail-api--google-oauth2-setup)
- [Application Roles](#application-roles)
- [Key Workflows](#key-workflows)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## Features

**Books & Reading**
- Browse, search, and filter the book catalog by genre, tag, and average rating
- Track reading progress per book (pages read and completion percentage)
- Mark books as complete — progress is auto-finalized
- Write and like book reviews (star ratings 0–5, rich-text content via TinyMCE)
- Organize books into built-in shelves (Currently Reading, Want to Read, Read) and custom shelves
- View your full collection organized by shelf

**Social**
- Send, accept, decline, and revoke friend requests
- Remove existing connections
- Browse any user's public profile: shelves, reading progress, and bio
- Real-time activity feed showing friends' reviews, progress updates, and new connections (30-day window, paginated)

**Authors**
- Any registered user can apply to become an author
- Authors can submit book addition requests for admin review
- Author profiles optionally link to a registered user account

**Authentication & Account Management**
- Standard email/password registration and login
- Google OAuth2 / OIDC login (auto-provisions new accounts)
- Password reset via tokenized email link (Gmail API, 10-minute expiry)

**Admin Panel**
- Full CRUD for users, books, authors, genres, and tags
- Review and approve/reject author applications
- Review and approve author-submitted book requests
- System metrics dashboard: live JVM heap, CPU, thread and disk metrics, Caffeine cache statistics, health component status

**REST API**
- Full JWT-authenticated REST API under `/api/v1` for mobile and external clients
- Covers books, shelves, feed, profiles, author applications, and account management
- OpenAPI 3.0 specification served at `/api-docs/openapi.yaml`

**General**
- Server-side rendering with Thymeleaf; AJAX-powered fragments for feeds, shelves, and search
- Internationalization (i18n): English, French, German, Spanish, Bengali
- Dark / light mode toggle
- Async activity event system with transactional outbox pattern
- Request logging to rolling file

---

## Architecture Overview

BookShare is a monolithic Spring Boot application with a server-rendered Thymeleaf frontend and a parallel stateless REST API. AJAX is used selectively in the MVC layer to update page fragments without full reloads (book grid, activity feed, shelf tabs, profile connection state).

```
Browser (Thymeleaf + Bootstrap)     Mobile / API clients
        │                                   │
        ▼                                   ▼
Spring MVC Controllers          REST API Controllers (/api/v1/**)
        │                        JWT filter + stateless auth
        └───────────┬────────────────────────┘
                    ▼
        Service Layer (business logic, caching, event publishing)
                    │
           ┌────────┴────────────┐
           │                     │
     JPA/Hibernate     Activity Outbox (transactional writes)
           │                     │
      PostgreSQL        Scheduled Processor (every 15 s)
                                 │
                      Activity + FeedEntry tables
```

**Activity & Feed pipeline:**
1. User actions (review, progress update, friend connection) write to `activity_outbox` within the same transaction.
2. `ActivityOutboxProcessor` (runs every 15 seconds) picks up PENDING outbox rows, persists `Activity` records, and fans out `FeedEntry` rows to the actor's connections.
3. Feed reads are served from the `feed` Caffeine cache (60-second TTL).

**Caching (Caffeine):**

| Cache | TTL | Notes |
|---|---|---|
| `books` / `book-lists` | 12 h / 30 min | Individual books and paginated list results |
| `authors` / `author-lists` | 12 h / 30 min | |
| `genres` / `genre-lists` | 24 h / 24 h | |
| `tags` / `tag-lists` | 24 h / 24 h | |
| `shelves` / `shelf-lists` | 10 min / 5 min | Short TTL; invalidated on shelf writes |
| `logins` | 30 min | Keyed by email and handle |
| `feed` | 60 s | Per-user, refreshed frequently |

Cache statistics are visible on the admin Actuator dashboard.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 3.5.5, Spring MVC, Spring Data JPA / Hibernate |
| Security | Spring Security, Spring Security OAuth2 Client (Google OIDC), JJWT 0.13 (JWT for REST API) |
| Frontend | Thymeleaf + Layout Dialect, Bootstrap 5, jQuery 3.7.1, FontAwesome |
| Rich UI | TinyMCE (description editor), FilePond (image upload), Select2 (multi-select), DataTables |
| Database | PostgreSQL 17, Flyway (migrations V1–V20) |
| Caching | Caffeine (managed via Spring Cache abstraction) |
| Email | Spring Mail + Gmail API (Google OAuth2 UserCredentials) |
| Observability | Spring Boot Actuator, Micrometer |
| Build & QA | Gradle (version catalog), Lombok, SpotBugs |
| Infrastructure | Docker (multi-stage build, eclipse-temurin:25-jre-alpine), Docker Compose |

---

## Getting Started

### Prerequisites

- Java 25+
- Gradle 8+
- PostgreSQL (or Docker — no local DB needed when using Compose)
- Git

### Local Setup

```bash
# Clone the repository
git clone https://github.com/smzoha/bookshare.git
cd bookshare
```

Database connection defaults to `localhost:5432/bookshare`. Override in `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookshare
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
```

Start the application:

```bash
./gradlew bootRun
```

Access at: **http://localhost:6001**

Flyway runs all migrations automatically on startup. No manual schema setup is required.

### Running with Docker

Docker Compose starts both the application and a PostgreSQL 17 instance. No local database required.

```bash
# 1. Create your environment file
cp .env.example .env
```

Edit `.env`:
```env
DATABASE_NAME=bookshare
DATABASE_USER=your_db_user
DATABASE_PASSWORD=your_db_password

# REST API — JWT signing key (Base64-encoded) and token expiry
APP_JWT_SECRET=your_base64_encoded_secret
APP_JWT_EXPIRY_MS=1800000
```

```bash
# 2. Start all services
./deploy.sh start

# 3. Stop all services
./deploy.sh stop
```

- App port: **6001** (host)
- PostgreSQL port: **5433** (host, mapped to 5432 inside the container)
- Logs are written to `./logs/` on the host

### Seed Data

The project ships with sample data covering 10 authors, 10 books, 8 tags, and 5 genres. This script is not run automatically by Flyway — load it manually into your database after first startup:

```bash
psql -U your_db_user -d bookshare -f src/main/resources/seed/seed_data.sql
```

### Gmail API & Google OAuth2 Setup

Two Google Cloud credentials are needed:
- **Gmail API** — for sending password-reset emails
- **Google OAuth2 Login** — for Sign in with Google

**Step 1 — Create OAuth2 Credentials in Google Cloud Console:**

1. Go to [Google Cloud Console](https://console.cloud.google.com/) and create or select a project.
2. Enable the **Gmail API**: APIs & Services → Library → Gmail API → Enable.
3. Configure the **OAuth Consent Screen**: External, fill in app name and support email, add scopes:
   - `https://www.googleapis.com/auth/gmail.send`
   - `openid`
   - `https://www.googleapis.com/auth/userinfo.email`
   - `https://www.googleapis.com/auth/userinfo.profile`
4. Create an **OAuth Client ID** (Web application) with these Authorized Redirect URIs:
   - `http://localhost:6001`
   - `http://localhost:6001/login/oauth2/code/google`
   - `http://localhost:6001/oauth2/authorization/google`
5. Note the `client_id` and `client_secret`.

**Step 2 — Generate a Gmail Refresh Token:**

Open in your browser (replace `YOUR_CLIENT_ID`):
```
https://accounts.google.com/o/oauth2/v2/auth?client_id=YOUR_CLIENT_ID&redirect_uri=http://localhost:6001&response_type=code&scope=https://www.googleapis.com/auth/gmail.send&access_type=offline&prompt=consent
```

After granting access, copy the `code` from the redirect URL. Exchange it for tokens:

```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

code=AUTH_CODE&client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&redirect_uri=http://localhost:6001&grant_type=authorization_code
```

Copy `refresh_token` from the response.

**Step 3 — Add credentials to your config:**

For local Spring Boot (`secret-dev.properties`, see `secret-dev.properties.example`):
```properties
app.gmail.client.id=your_client_id
app.gmail.client.secret=your_client_secret
app.gmail.refresh.token=your_refresh_token

spring.security.oauth2.client.registration.google.client-id=${app.gmail.client.id}
spring.security.oauth2.client.registration.google.client-secret=${app.gmail.client.secret}
```

For Docker deployment (`.env`):
```env
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_REFRESH_TOKEN=your_refresh_token
```

> The Google Login flow is handled automatically by Spring Security OAuth2. The manual token exchange above is only needed for the Gmail sending credential.

### REST API

The API base URL is `/api/v1`. It uses stateless JWT authentication — no cookies or sessions.

**Getting a token:**
```http
POST /api/v1/auth/token
Content-Type: application/json

{"email": "user@example.com", "password": "yourpassword"}
```

The response body contains `{"token": "<JWT>"}`. Include it in subsequent requests:
```
Authorization: Bearer <token>
```

Tokens expire after 30 minutes by default (`APP_JWT_EXPIRY_MS`).

The full OpenAPI 3.0 specification is available at `/api-docs/openapi.yaml` once the application is running. When running locally with the dev profile, Swagger UI is also available at `/swagger-ui/index.html`. It is disabled in production.

---

## Application Roles

| Role | Description |
|---|---|
| `USER` | Default role. Can browse, review, shelve books, track progress, and manage social connections. Can apply to become an author. |
| `AUTHOR` | Elevated user. Can submit new book requests for admin review. Cannot re-apply for authorship. |
| `MODERATOR` | Access to `/manage/**` routes (subset of admin). |
| `ADMIN` | Full access including admin panel, user management, and Actuator dashboard. |

Role assignment: new registrations and Google logins default to `USER`. Admins upgrade roles via the user management panel. Author applications go through an admin-approval flow.

---

## Key Workflows

**Registering and logging in:**
`/login` → register tab or Google button → redirected to `/` (authenticated home with feed).

**Browsing and shelving a book:**
`/book/list` → filter/search → `/book/{id}` → add to shelf via dropdown → reading progress form.

**Social connections:**
`/profile/{handle}` → Add Friend → friend receives request → accepts → both appear in each other's connections → activity events fan out to their mutual friends' feeds.

**Author application:**
Home page → "Apply to be an Author" → request saved → admin reviews at `/admin/author/request` → on approval, user role upgraded to AUTHOR and an Author entity is created.

**Book submission (authors):**
`/author/bookRequest` → fill form (title, ISBN, authors, genres, tags, description, cover image) → submitted with PENDING status → admin reviews at `/admin/book`.

**Password reset:**
`/resetPasswordRequest` → enter email → tokenized link sent via Gmail → `/resetPassword?token=...` → set new password (token expires in 10 minutes).

---

## Contributing

1. Fork the repository
2. Create a branch: `feat/my-feature` for features, `bugfix/my-fix` for bugfixes
3. Commit your changes with a clear message
4. Push and open a Pull Request

#### Guidelines:
- Follow existing code style and package structure
- Flyway migration files must follow the naming convention `V{N}__{description}.sql` and be placed in the appropriate `db/migration/{month_year}/` subdirectory
- New i18n strings belong in all five locale files under `src/main/resources/locale/`
- Keep controller methods thin — business logic lives in the service layer
- Cache eviction must be added alongside any writes that affect cached data

---

## License

This project is licensed under the **GNU General Public License v2 (GPL-2.0)**.
See the [LICENSE](LICENSE) file for the full text.

---

## Contact

BookShare — Powered by ZedApps

GitHub: [smzoha](https://github.com/smzoha)

Email: shamah.zoha@gmail.com
