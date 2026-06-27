# AGENTS.md — BookShare Agentic Development Guide

This file provides the context an AI coding agent needs to work effectively in the BookShare codebase. Read this before making any changes.

---

## Project Identity

- **Name:** BookShare
- **Group:** `com.zedapps`
- **Main class:** `com.zedapps.bookshare.AppRunner`
- **Port:** 6001
- **Java:** 25 (toolchain enforced in `build.gradle`)
- **Spring Boot:** 3.5.5
- **Build:** Gradle with version catalog (`gradle/libs.versions.toml`)

---

## Repository Layout

```
src/main/java/com/zedapps/bookshare/
├── AppRunner.java               # @SpringBootApplication, @EnableScheduling, BCryptPasswordEncoder bean
├── async/                       # Async listeners and scheduled jobs
│   ├── ActivityOutboxProcessor  # @Scheduled: processes outbox every 15s, cleanup at midnight
│   ├── ActivityEventListener    # @EventListener(@Async): handles ActivityEvent, persists Activity records
│   ├── AuthEventListener        # @EventListener: handles Spring Security login/logout events
│   └── FeedEntryListener        # @EventListener: fans out FeedEntry rows to connections
├── config/
│   ├── AsyncConfig              # activityPublishExecutor: virtual threads, concurrencyLimit=10
│   ├── CacheConfig              # Caffeine cache definitions (see cache table below)
│   ├── GmailConfig              # Gmail API client bean (OAuth2 credentials + service instance)
│   ├── LocaleConfig             # CookieLocaleResolver (cookie name: lang, 30-day TTL)
│   └── SecurityConfig           # HTTP security rules, form login, OAuth2 OIDC
├── controller/
│   ├── HomeController                      # /, /feed, /admin (MVC)
│   ├── dashboard/ReadingStatsController    # /readingStats — per-year reading statistics dashboard (MVC)
│   ├── admin/ActuatorDashboardController   # /admin/actuator/dashboard
│   ├── api/                                # REST API — all under /api/v1
│   │   ├── ApiExceptionHandler             # @RestControllerAdvice for api package
│   │   ├── HomeApiController               # /api/v1/home/featured, /api/v1/home/feed
│   │   ├── auth/ApiAuthController          # /api/v1/auth/token (JWT issuance)
│   │   ├── book/BookApiController          # /api/v1/book/**
│   │   ├── dashboard/ReadingStatsApiController # /api/v1/readingStats/{year}
│   │   ├── login/AuthorApiController       # /api/v1/author/apply
│   │   ├── login/LoginApiController        # /api/v1/login/register, /resetPassword/**
│   │   ├── login/ProfileApiController      # /api/v1/profile/** (incl. reading challenge)
│   │   └── shelf/ShelfApiController        # /api/v1/shelf/**
│   ├── book/
│   │   ├── admin/BookAdminController       # /admin/book
│   │   ├── admin/AuthorAdminController     # /admin/author
│   │   ├── admin/GenreAdminController      # /admin/genre
│   │   ├── admin/TagAdminController        # /admin/tag
│   │   └── app/
│   │       ├── BookController              # /book (public + auth endpoints)
│   │       ├── BookRequestController       # /author/bookRequest (AUTHOR role)
│   │       └── AuthorController            # /author/apply (USER role)
│   ├── image/ImageController               # /image (serve + upload)
│   └── login/
│       ├── admin/LoginAdminController      # /admin/user
│       └── app/
│           ├── LoginController             # /login, /register
│           ├── PasswordResetController     # /resetPasswordRequest, /resetPassword
│           ├── ProfileController           # /profile/**
│           ├── ShelfController             # /shelf/add
│           └── CollectionController        # /collection
├── dto/                         # Data Transfer Objects (read these before touching forms)
├── editor/                      # PropertyEditors for form binding (Author, Genre, Tag, Image)
├── helper/
│   ├── BookHelper               # Populates ModelMap for book detail page (calls BookService + ShelfService)
│   ├── ProfileHelper            # Populates ModelMap for profile page and connection fragment (calls ProfileService)
│   └── ReadingStatsHelper       # Populates ModelMap for reading stats dashboard (challenge, progress, reviews)
├── entity/
│   ├── activity/
│   │   ├── Activity             # table: activity
│   │   └── ActivityOutbox       # table: activity_outbox
│   ├── book/
│   │   ├── Author               # table: author
│   │   ├── AuthorRequest        # table: author_request
│   │   ├── Book                 # table: book
│   │   ├── Genre                # table: genre
│   │   └── Tag                  # table: tag
│   ├── feed/FeedEntry           # table: feed_entry
│   ├── image/Image              # table: image (LOB content)
│   └── login/
│       ├── Connection           # table: connection (bidirectional pair)
│       ├── FriendRequest        # table: friend_request
│       ├── Login                # table: logins (the user entity)
│       ├── PasswordResetToken   # table: password_reset_token
│       ├── ReadingChallenge     # table: reading_challenge (composite key via ReadingChallengeId)
│       ├── ReadingProgress      # table: reading_progress
│       ├── Review               # table: review
│       ├── Shelf                # table: shelf
│       └── ShelvedBook          # table: shelved_book
├── enums/                       # ActivityStatus, ActivityType, AuthProvider, Role, Status
├── exception/                   # Custom exceptions
├── filter/
│   ├── JwtAuthFilter            # Validates Bearer token for /api/v1/** (OncePerRequestFilter)
│   └── RequestLogFilter         # Logs all HTTP requests to request.log
├── repository/                  # Spring Data JPA repositories (one per entity)
├── service/
│   ├── activity/ActivityService
│   ├── auth/
│   │   ├── JwtService           # JWT generation and validation (JJWT)
│   │   ├── LoginDetails         # Unified security principal (UserDetails + OidcUser + OAuth2User)
│   │   ├── LoginDetailService   # UserDetailsService implementation
│   │   └── LoginDetailOidcService # OAuth2UserService for Google OIDC
│   ├── book/
│   │   ├── BookAdminService     # CRUD + caching for books, authors, genres, tags
│   │   ├── BookApiService       # API-specific book reads, review/shelf/progress writes
│   │   └── BookService          # User-facing reads (paginated lists, related books)
│   ├── dashboard/ReadingStatsApiService # Assembles ReadingStatsDto for the REST API (reuses ReadingStatsHelper)
│   ├── image/ImageService
│   ├── login/
│   │   ├── AuthorRequestService # Author application validation + save (shared MVC/API)
│   │   ├── FeedApiService       # Feed reads shaped for API responses
│   │   ├── FeedService          # Feed reads for MVC
│   │   ├── LoginApiService      # Registration and password-reset logic for API
│   │   ├── LoginService         # Core user load/save; canonical getLogin(email)
│   │   ├── PasswordResetService
│   │   ├── ProfileApiService    # Profile read + connection actions for API
│   │   ├── ProfileService       # Profile read + connection actions for MVC
│   │   └── ReadingChallengeApiService # Reading challenge get/save for API
│   ├── mail/MailService         # Gmail API email sending (@Async)
│   └── shelf/
│       ├── ShelfApiService      # Shelf reads/writes for API
│       └── ShelfService         # Shelf reads/writes for MVC
├── util/                        # Utility helpers
└── validator/                   # Custom Spring validators

src/main/resources/
├── application.properties       # Core config (port 6001, JPA, Flyway, caching, actuator)
├── application-dev.properties   # Dev DB connection, devtools, mail, imports secret-dev.properties
├── secret-dev.properties.example
├── db/migration/                # Flyway SQL migrations (V1–V21)
│   ├── 09_2025/                 # V1–V8_2
│   ├── 10_2025/                 # V9
│   ├── 01_2026/                 # V10–V12_1
│   ├── 02_2026/                 # V13
│   ├── 03_2026/                 # V14–V20
│   └── 06_2026/                 # V21 (reading_challenge)
├── locale/messages*.properties  # i18n: en, fr, de, es, bn
├── seed/seed_data.sql           # Manual seed (10 books, 10 authors, genres, tags)
├── static/                      # CSS, JS, vendor libs (Bootstrap, jQuery, TinyMCE, etc.)
└── templates/                   # Thymeleaf templates (see UI section)
```

---

## Domain Model (Quick Reference)

### Core Entities and Tables

| Entity | Table | Key Fields |
|---|---|---|
| `Login` | `logins` | `id`, `handle` (unique), `email` (unique), `role` (ADMIN/MODERATOR/AUTHOR/USER), `authProvider` (LOCAL/GOOGLE), `active` |
| `Book` | `book` | `id`, `title`, `isbn`, `status` (ACTIVE/PENDING/INACTIVE), `pages`, `publicationDate` |
| `Author` | `author` | `id`, `firstName`, `lastName`, `login` (OneToOne, optional), `profilePictureUrl` |
| `Genre` | `genre` | `id`, `name` |
| `Tag` | `tag` | `id`, `name` |
| `Review` | `review` | `id`, `content`, `rating` (0–5), `reviewDate`, FK: `user`, `book` |
| `Shelf` | `shelf` | `id`, `name`, `defaultShelf` (non-updatable), FK: `user` |
| `ShelvedBook` | `shelved_book` | `id`, `shelvedAt`, FK: `login`, `book`, `shelf` |
| `ReadingProgress` | `reading_progress` | `id`, `pagesRead`, `startDate`, `endDate`, `completed`, FK: `user`, `book` |
| `Connection` | `connection` | `id`, FK: `person1`, `person2` — stored as bidirectional pair |
| `FriendRequest` | `friend_request` | `id`, FK: `person1` (sender), `person2` (recipient) |
| `Activity` | `activity` | `id`, `eventType`, `referenceEntity`, `referenceId`, `metadata` (JSONB), `internal`, nullable `login` |
| `ActivityOutbox` | `activity_outbox` | `id`, `status` (PENDING/COMPLETED/FAILED), `retryCount` (max 3), `payload` (JSONB) |
| `FeedEntry` | `feed_entry` | `id`, FK: `audienceLogin`, `activity` |
| `Image` | `image` | `id`, `fileName`, `contentType`, `content` (LOB byte[]) |
| `PasswordResetToken` | `password_reset_token` | `id`, `email`, `hashedSignature` (SHA-256), `expiryTimestamp` (10 min), `inactive` |
| `AuthorRequest` | `author_request` | `id`, FK: `login` |
| `ReadingChallenge` | `reading_challenge` | composite PK `(login, year)` via `@IdClass(ReadingChallengeId)`, `bookCount` (1–1000 annual goal) |

### JPA Entity Graphs (important for N+1 avoidance)

| Entity Graph | Fetches |
|---|---|
| `login.withCollections` | shelves (with books), readingProgresses (with book/authors/reviews/image) |
| `book.withAssociations` | authors, tags, genres, image |
| `book.withAll` | + reviews |
| `author.withLogin` | linked Login |

Always use the appropriate named graph when loading entities that need associations. Avoid relying on lazy loading in controllers.

### Default Shelves
Every new user gets three shelves created automatically in `LoginService.createLogin()` and `saveLogin(LoginManageDto)`:
- `"Currently Reading"` — `defaultShelf = true`
- `"Want to Read"` — `defaultShelf = true`
- `"Read"` — `defaultShelf = true`

A book can only be in one default shelf at a time (enforced in `BookService.addToShelf()`).

---

## Security Rules

`SecurityConfig` defines two filter chains. Match these when adding new routes.

### MVC filter chain (`/**`, Order 2) — session-based

| URL Pattern | Access |
|---|---|
| `/admin/**` | ADMIN only |
| `/manage/**` | ADMIN or MODERATOR |
| `/manage/book` | AUTHOR |
| `/profile/**` | Any authenticated role |
| `/readingStats` | Any authenticated role |
| `/book/add*`, `/book/remove*`, `/book/update*`, `/book/like`, `/shelf/add`, `/collection/**` | Any authenticated user |
| `/resetPasswordRequest`, `/resetPassword` | Anonymous only |
| `/author/apply` | USER role only |
| `/author/bookRequest` | AUTHOR role only |
| `/actuator/**` | ADMIN only |
| Everything else | Public |

### API filter chain (`/api/v1/**`, Order 1) — stateless JWT

| URL Pattern | Access |
|---|---|
| `/api/v1/auth/token` | Public |
| `/api/v1/login/**` | Public |
| `/api/v1/home/featured` | Public |
| `/api/v1/author/apply` | USER role only |
| Everything else | Authenticated (valid JWT required) |

CSRF is disabled on both chains. The MVC chain uses standard Spring Security sessions; logout at `/logout` clears `JSESSIONID`. The API chain is stateless — no session is created.

The API chain configures no `authenticationEntryPoint`, so an unauthenticated request to a protected `/api/v1/**` route is rejected with **403** (the default `Http403ForbiddenEntryPoint`), not 401 — assert `status().isForbidden()` in API controller tests for the anonymous case.

The authenticated user principal is always a `LoginDetails` object (implements `UserDetails`, `OidcUser`, `OAuth2User`). Retrieve it in controllers with `@AuthenticationPrincipal LoginDetails loginDetails`.

---

## REST API

The REST API lives under `/api/v1` and is intended for mobile or external clients. It is completely stateless — no sessions, no cookies.

### Authentication

1. Client posts `{email, password}` to `POST /api/v1/auth/token`.
2. Server returns `{token: "<JWT>"}`. The token embeds the user's email (subject) and role as claims.
3. Client includes the token in every subsequent request: `Authorization: Bearer <token>`.
4. `JwtAuthFilter` intercepts `/api/v1/**` requests, validates the token via `JwtService`, and sets the `SecurityContext`.

Token expiry is configured by `app.jwt.expiry.ms` (default 1 800 000 ms = 30 minutes).

### Exception handling

`ApiExceptionHandler` (`@RestControllerAdvice` scoped to `controller.api`) converts exceptions to `ErrorResponseDto` JSON:

| Exception | Status |
|---|---|
| `NoResultException` | 404 |
| `HttpMessageNotReadableException` | 400 |
| `AccessDeniedException` | 403 |
| `AuthenticationException` | 401 |
| Any other `Exception` | 500 |

### OpenAPI spec

The hand-maintained spec lives at `src/main/resources/static/api-docs/openapi.yaml` and is served as a static file at `/api-docs/openapi.yaml`.

- **Dev profile:** Swagger UI is enabled (`springdoc.swagger-ui.enabled=true` in `application-dev.properties`) and configured to load the custom spec (`springdoc.swagger-ui.url=/api-docs/openapi.yaml`). Browse it at `/swagger-ui/index.html` while the app is running locally.
- **Production:** Swagger UI is disabled (`springdoc.swagger-ui.enabled=false` in `application.properties`); only the raw YAML is served.

Update the spec whenever you add or change an API endpoint.

### API endpoint summary

| Controller | Base path | Key endpoints |
|---|---|---|
| `ApiAuthController` | `/api/v1/auth` | `POST /token` |
| `BookApiController` | `/api/v1/book` | `GET /list`, `GET /{id}`, `GET /search`, `POST /{id}/review`, `POST /{id}/progress`, `POST|DELETE /{id}/shelf`, `POST /review/{id}/like` |
| `ShelfApiController` | `/api/v1/shelf` | `GET /`, `GET /{id}`, `POST /` |
| `HomeApiController` | `/api/v1/home` | `GET /featured` (public), `GET /feed` |
| `LoginApiController` | `/api/v1/login` | `POST /register`, `POST /resetPassword/request`, `POST /resetPassword` |
| `ProfileApiController` | `/api/v1/profile` | `GET /{handle}`, `POST /connect`, `GET|POST /readingChallenge` |
| `ReadingStatsApiController` | `/api/v1/readingStats` | `GET /{year}` |
| `AuthorApiController` | `/api/v1/author` | `POST /apply` |

---

## Activity System

Significant user actions must record an activity. There are two paths depending on context:

**Path A — Outbox (MVC controllers):** call `activityService.saveActivityOutbox(type, referenceId, payload)` within the same DB transaction as the main write.
1. `ActivityOutboxProcessor` runs every 15 seconds, picks up PENDING rows (top 100), persists `Activity` records, and marks rows COMPLETED or FAILED.
2. For non-internal activity types (listed in `FEED_ACTIVITIES`), `ActivityService.saveActivity()` publishes an `ActivityFeedDto` Spring event.
3. `FeedEntryListener` handles that event and inserts one `FeedEntry` per connection.

**Path B — Direct Spring event (API services):** publish an `ActivityEvent` bean via `ApplicationEventPublisher`. `ActivityEventListener` handles it `@Async` and calls `activityService.saveActivity()` directly, bypassing the outbox. Use this path in API services where the outbox transaction pattern is impractical.

> `AuthEventListener` separately handles Spring Security `AuthenticationSuccessEvent` and `LogoutSuccessEvent` (MVC logins only — JWT auth does not fire these events).

### Activity Types (enum `ActivityType`)
Key types and when to use them:

| Type | Fired by |
|---|---|
| `REGISTER` | New user registration |
| `USER_ADD` / `USER_UPDATE` | Admin user save |
| `BOOK_VIEW` / `BOOK_LIST_VIEW` | Controller — anonymous tracking |
| `BOOK_VIEW_ADMIN` / `BOOK_LIST_VIEW_ADMIN` | Admin views |
| `BOOK_ADD_TO_SHELF` / `BOOK_REMOVE_FROM_SHELF` | Shelf actions |
| `BOOK_ADD_REVIEW` | Review submission |
| `BOOK_LIKE_REVIEW` / `BOOK_REMOVE_LIKE_REVIEW` | Review like toggle |
| `BOOK_UPDATE_READING_PROGRESS` | Progress update |
| `BOOK_REQUEST_SAVE` | Author book submission |
| `SHELF_ADD` | New custom shelf |
| `FRIEND_REQ_SENT` / `REVOKE_FRIEND_REQ` | Friend request sent / revoked |
| `DECLINE_FRIEND_REQ` | Friend request declined |
| `REMOVE_FRIEND` | Connection removal |
| `ADD_FRIEND` | Friend request accepted — creates the connection (triggers feed) |
| `RESET_PASSWORD_REQUEST` / `RESET_PASSWORD` | Password reset flow |

`FEED_ACTIVITIES` (non-internal, fan out to connections): `BOOK_ADD_REVIEW`, `BOOK_LIKE_REVIEW`, `BOOK_UPDATE_READING_PROGRESS`, `ADD_FRIEND`.

---

## Caching Rules

Caches are managed via Spring's `@Cacheable`, `@CacheEvict`, and `@CachePut` annotations in the service layer. Follow these invariants when modifying services:

| Cache | Populated by | Evicted by |
|---|---|---|
| `books` | `BookAdminService.getBook()` | Any book save/update |
| `book-lists` | `BookService.getPaginatedBooks()` | Any book save/update |
| `authors` | `BookAdminService.getAuthor()` | Any author save/update |
| `author-lists` | `BookAdminService.getAuthorList()` | Any author save/update |
| `genres` | `BookAdminService.getGenre()` | Any genre save/update |
| `genre-lists` | `BookAdminService.getGenreList()` | Any genre save/update |
| `tags` | `BookAdminService.getTag()` | Any tag save/update |
| `tag-lists` | `BookAdminService.getTagList()` | Any tag save/update |
| `shelves` | `ShelfService.getShelfById()` | Any shelf or shelved-book write |
| `shelf-lists` | `ShelfService.getShelvesForCollection()` | Any shelf or shelved-book write |
| `logins` | `LoginService.getLogin()`, `getLoginByHandle()` | Any login save |
| `feed` | `FeedService.getFeedDtoList()` | Not explicitly evicted (60-second TTL) |

Never write to the database in a method annotated `@Cacheable` — the result may be returned from cache on repeat calls.

---

## Database Migrations

Flyway is the only mechanism that touches the schema. `spring.jpa.hibernate.ddl-auto=none`.

**Rules for new migrations:**
1. File name: `V{N}__{description}.sql` where `N` continues from the current highest version (V21).
2. Place in `src/main/resources/db/migration/{mon_yyyy}/` matching the current month.
3. Never modify an existing migration file — Flyway will reject checksum mismatches.
4. For complex changes that require multiple steps, use sub-versions: `V21__main_change.sql`, `V21_1__followup.sql`.
5. The baseline version is V1 (`baseline-on-migrate=true`).

---

## UI / Thymeleaf Templates

All templates are in `src/main/resources/templates/`. The master layout is `base.html` (Thymeleaf Layout Dialect).

### Template Map

```
templates/
├── base.html                              # Master layout (navbar, footer, theme toggle, global CSS/JS)
├── home.html                              # Dispatcher: pubHome (anon) vs userHome (auth)
├── error.html                             # Custom error page
├── adminHome.html                         # Admin dashboard (5 cards)
├── common/
│   ├── navbar.html                        # Search bar, nav links, language selector, profile dropdown
│   ├── footer.html
│   └── themeToggle.html                   # Dark/light mode toggle button
├── app/
│   ├── pubHome.html                       # Public hero, featured books, genre grid
│   ├── userHome.html                      # Feed (AJAX), recently read, featured books, author CTA
│   ├── userFeedFragment.html              # Feed cards fragment (AJAX swap target)
│   ├── common/
│   │   ├── bookCardFragment.html          # Reusable book card
│   │   ├── bookProgressFragment.html      # Book + reading progress row widget
│   │   └── shelvedBookFragment.html       # Shelf book list row
│   ├── book/
│   │   ├── bookList.html                  # Paginated grid + filter sidebar
│   │   ├── bookGridFragment.html          # Grid cards only (AJAX swap)
│   │   ├── book.html                      # Book detail page
│   │   ├── reviewList.html                # Review list fragment (5/page, AJAX)
│   │   ├── readingProgressForm.html       # Progress update modal
│   │   └── shelfSelectionFragment.html    # Add/remove shelf controls (AJAX)
│   ├── login/
│   │   ├── login.html                     # Login + Register tabbed page
│   │   ├── resetPasswordRequest.html      # Forgot password form
│   │   ├── resetPassword.html             # New password form (token-gated)
│   │   ├── collection.html                # User's shelves and books
│   │   ├── readingStats.html              # Reading statistics dashboard (Chart.js, year selector)
│   │   └── fragments/
│   │       ├── loginComponent.html        # Login form
│   │       ├── registrationComponent.html # Registration form
│   │       └── createShelfFragment.html   # New shelf modal
│   └── profile/
│       ├── profile.html                   # Full user profile page
│       ├── profileInfoFragment.html       # Header info + connection action buttons (AJAX)
│       └── profileActiveShelfFragment.html # Active shelf content (AJAX swap)
├── admin/
│   ├── actuator/
│   │   ├── actuatorDashboard.html         # System metrics full page
│   │   └── actuatorDashboardContent.html  # Metrics fragment (auto-refreshes every 10s)
│   ├── author/
│   │   ├── authorForm.html                # Create/edit author
│   │   ├── authorList.html                # Author list (DataTables)
│   │   └── requestList.html               # Author applications (approve action)
│   ├── book/bookList.html                 # Admin book list (DataTables)
│   ├── genre/
│   │   ├── genreForm.html
│   │   └── genreList.html
│   ├── tag/
│   │   ├── tagForm.html
│   │   └── tagList.html
│   └── user/
│       ├── userForm.html                  # Create/edit user
│       └── userList.html                  # User list (DataTables)
└── bookForm.html                          # Shared book form (admin + author request)
```

### AJAX Patterns

Several pages use AJAX fragment replacement. The controller checks `request.getHeader("X-Requested-With")` or uses `@ResponseBody`:

| Endpoint | Fragment returned | Used by |
|---|---|---|
| `/feed` | `userFeedFragment :: userFeed` | User home page |
| `/book/list` (XHR) | `bookGridFragment :: bookGrid` | Book list filter/sort |
| `/book/reviews/{id}` | `reviewList :: reviewList` | Book detail |
| `/profile/shelf`, `/profile/{handle}/shelf` | Profile shelf fragment | Profile page |
| `/profile/friendRequest` | `profileInfoFragment :: profileInfo` | Profile connection actions |
| `/collection` (XHR) | `shelvedBooks` fragment | Collection page |
| `/admin/actuator/dashboard` (XHR) | `actuatorDashboardContent` | Actuator dashboard (auto-refresh) |

### Thymeleaf Conventions

- Layout: `layout:decorate="~{base}"` at the `<html>` tag; content in `layout:fragment="content"`.
- Security: use `sec:authorize="hasRole('ADMIN')"` from `thymeleaf-extras-springsecurity6`.
- i18n: `th:text="#{key.name}"` for all user-facing strings. Add new keys to all five locale files.
- CSRF is disabled — no `_csrf` token needed in forms.
- Images are served via `/image/{id}`. Use `th:src="@{/image/{id}(id=${book.image.id})}"`.
- Form binding uses custom `PropertyEditor` classes in `editor/` for `Author`, `Genre`, `Tag`, and `Image` — register them in the controller via `@InitBinder` if adding a new form.

---

## Controller Conventions

- **Thin controllers.** Controllers resolve the principal, call one or two service methods, populate the model, and return a view name or redirect. No business logic in controllers.
- **Principal access:** `@AuthenticationPrincipal LoginDetails loginDetails` parameter.
- **AJAX detection:** Check `"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))` to decide whether to return a full page or just a fragment.
- **JSON endpoints:** Annotate with `@ResponseBody` and return a DTO. No `ResponseEntity` wrappers unless needed for status codes.
- **Redirects after POST:** Always `return "redirect:/..."` to follow PRG (Post-Redirect-Get) pattern.
- **Model population:** Helper classes (`BookHelper`, `ProfileHelper`) handle bulk `ModelMap` population for complex pages — use them rather than calling multiple services in the controller. Helpers live in `helper/` and are the only layer that accepts `ModelMap` as a parameter.

---

## Service Conventions

- **Cache annotations go on service methods, never on repository methods.**
- **Outbox before return.** Fire the outbox event as the last step before returning, so the main write is complete.
- **`LoginService.getLogin(email)`** is the canonical way to load the current user's full entity from the principal's email. It's cached.
- **Never call `repository.save()` from a controller.** Always go through the service.
- **`BookAdminService` vs `BookService`:** `BookAdminService` owns all CRUD and caching for books, authors, genres, and tags. `BookService` owns user-facing read logic (paginated lists, related books, review operations, shelf operations).

---

## Adding a New Feature — Checklist

When adding a new entity or feature, work through this list in order:

1. **Migration:** Write a new Flyway SQL file (`V{N+1}__...sql`) in the current month's subdirectory.
2. **Entity:** Add JPA entity in the appropriate `entity/` package. Use sequences for IDs, `createdAt`/`updatedAt` with `@PrePersist`/`@PreUpdate`.
3. **Repository:** Add a Spring Data repository interface in `repository/`.
4. **DTO:** Add request/response DTOs in `dto/` if the form doesn't map directly to the entity.
5. **Service:** Add business logic in `service/`. Add `@Cacheable`/`@CacheEvict` as appropriate. Fire outbox event for user-facing writes.
6. **Controller:** Add a controller in the correct subpackage (`app/` for user-facing, `admin/` for admin). Register any `PropertyEditor` via `@InitBinder`.
7. **Templates:** Add Thymeleaf templates under the correct `templates/` subdirectory. Use `base.html` layout. Add all UI strings to all five locale files.
8. **Security:** Update `SecurityConfig` if the new URL pattern needs non-default access rules.
9. **Activity type:** If the action should appear in the feed or audit log, add a new `ActivityType` enum value and wire it in `ActivityService`.
10. **Cache definition:** If a new cache is needed, add it to `CacheConfig`.

---

## Running & Building

```bash
# Run locally (dev profile active by default)
./gradlew bootRun

# Build JAR
./gradlew bootJar

# Run tests
./gradlew test

# Static analysis
./gradlew spotbugsMain

# Docker (starts app + PostgreSQL)
./deploy.sh start
./deploy.sh stop
```

Application runs on **http://localhost:6001**.

PostgreSQL in Docker is on host port **5433** (to avoid conflicts with a local instance on 5432).

---

## Testing

### Stack

| Dependency | Role |
|---|---|
| `spring-boot-starter-test` | JUnit 5, AssertJ, Mockito, Spring Test |
| `spring-boot-testcontainers` | `@ServiceConnection` auto-wiring |
| `testcontainers:postgresql` 1.21.4 | Real PostgreSQL instance per test class |
| `spring-security-test` | Security test utilities |

### Layer Overview

| Component | Tool | Key assertion |
|---|---|---|
| Repository | `@DataJpaTest` + Testcontainers | Query correctness against real DB |
| Service | Mockito (`@ExtendWith(MockitoExtension.class)`) | Business logic, delegation, outbox firing |
| MVC controller | `@WebMvcTest` + MockMvc | View name, model attributes, redirects |
| REST controller | `@WebMvcTest` + MockMvc | Status codes, JSON response body |
| Helper | Mockito + real `ModelMap` | Model key presence and values |
| Validator | Mockito + `BeanPropertyBindingResult` | Error codes on specific fields |
| Filter | Mockito + direct `doFilterInternal` call | SecurityContext state, chain invocation, log output |
| Static util | Plain JUnit 5 | Return values; manual security context setup |

Naming convention: `methodName_condition_expectedResult()`.

---

### Repository Tests (`@DataJpaTest`)

Repository tests verify actual JPA queries, JPQL, pagination, sorting, and filtering against a real PostgreSQL engine. Mocking the database here defeats the purpose — the goal is to prove the SQL is correct.

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");
}
```

- Use `@DataJpaTest` (not `@SpringBootTest`) to keep the slice focused — only the JPA layer loads.
- `@AutoConfigureTestDatabase(replace = NONE)` — keeps the Testcontainers datasource instead of substituting an in-memory DB.
- Use `@BeforeAll` with `@TestInstance(PER_CLASS)` when a single fixture setup is sufficient for all tests in the class; the method can be non-static under `PER_CLASS`.
- `@BeforeAll` runs outside any test transaction. `saveAndFlush` / `saveAllAndFlush` commits the fixture data; it persists for the lifetime of the test class.
- Each `@Test` is wrapped in a `@Transactional` rollback — in-test writes are reverted after each test; `@BeforeAll` data is unaffected.
- Extra data needed by a single test (that must not bleed into siblings) should be saved inside the test method itself — it rolls back with the test's own transaction.

---

### Service Tests — Mockito

Services hold business logic. Repositories are proven by the layer below, so mock them here.

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;
}
```

Key cases to cover per service:

- **Happy path** — the expected return value with valid input.
- **Not-found branches** — `NoResultException`, `NoSuchElementException`, etc.
- **Activity outbox firing** — verify `activityService.saveActivityOutbox(...)` is called with the correct `ActivityType` and payload fields.
- **Guard conditions** — test both branches of any early-return or access-check logic.

---

### Controller Tests — `@WebMvcTest` + MockMvc

`@WebMvcTest(XController.class)` loads only the web layer. All service and helper dependencies become `@MockBean`.

#### MVC Controllers

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;
}
```

What to assert per endpoint:

- **View name**: `.andExpect(view().name("app/book/bookList"))`
- **Model attributes**: `.andExpect(model().attributeExists("bookPage"))`
- **Redirects (PRG)**: `.andExpect(redirectedUrlPattern("/book/*"))`
- **Validation failures**: POST with a bad body should return the same view (not redirect) and carry model errors: `.andExpect(model().hasErrors())`
- **AJAX fragment routing**: send `X-Requested-With: XMLHttpRequest` header and assert `.andExpect(view().name("app/book/bookGridFragment :: bookGrid"))`
- **`@ResponseBody` endpoints on MVC controllers** (e.g. `/book/addShelf`): treat like REST — assert status and JSON body.

#### REST Controllers

```java
@WebMvcTest(BookApiController.class)
class BookApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookApiService bookApiService;
}
```

Focus on:

- **Status codes**: `.andExpect(status().isOk())`, `.andExpect(status().isBadRequest())`
- **JSON response body**: `.andExpect(jsonPath("$.id").value(1L))`
- **Validation error format**: POST with a bad `@RequestBody` and assert the `ErrorResponseDto` structure.
- **Guard conditions**: test both the valid and invalid branch where the controller returns early.

#### Handling `@AuthenticationPrincipal LoginDetails`

`@WithMockUser` only provides a basic `UserDetails`, not `LoginDetails`. Use the custom test annotation `@WithMockLoginDetails` backed by `WithMockLoginDetailsSecurityContextFactory`, which wraps `TestUtils`:

```java
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockLoginDetailsSecurityContextFactory.class)
public @interface WithMockLoginDetails {
    String email() default "test@test.com";
}
```

```java
public class WithMockLoginDetailsSecurityContextFactory
        implements WithSecurityContextFactory<WithMockLoginDetails> {

    @Override
    public SecurityContext createSecurityContext(WithMockLoginDetails annotation) {
        LoginDetails loginDetails = TestUtils.getLoginDetails(annotation.email(), "test", true);
        TestUtils.setupSecurityContext(loginDetails);
        return SecurityContextHolder.getContext();
    }
}
```

Both classes live in `src/test/java/.../util/`. Annotate test methods or the class with `@WithMockLoginDetails`.

---

### Helper Tests — Mockito + real `ModelMap`

Helpers are Spring `@Component` beans with service dependencies. Mock the services; pass a real `ModelMap` instance and assert on its contents after the call.

```java
@ExtendWith(MockitoExtension.class)
class BookHelperTest {

    @InjectMocks
    BookHelper bookHelper;

    @Mock
    BookService bookService;

    @Test
    void setupReferenceData_unauthenticatedUser_skipsUserSpecificData() {
        ModelMap model = new ModelMap();
        bookHelper.setupReferenceData(null, 1L, model, true, true);

        assertFalse(model.containsKey("readingProgresses"));
    }
}
```

- Use `lenient().when(...)` in `@BeforeEach` for stubs that are only consumed by a subset of tests (avoids `UnnecessaryStubbingException` — see **Mockito Footguns** below).
- When the helper puts a `Map<K, V>` in the model, use AssertJ `asInstanceOf(map(K.class, V.class))` for type-safe key assertions without unchecked casts:

```java
assertThat(model.get("defaultShelves")).asInstanceOf(map(Long.class, String.class))
        .containsKey(defaultShelf.getId())
        .doesNotContainKey(customShelf.getId());
```

---

### Validator Tests — Mockito + `BeanPropertyBindingResult`

Spring validators implement `Validator`. The `Errors` object is passed by the caller, not injected — use `BeanPropertyBindingResult` directly; no mocking needed for it.

```java
@ExtendWith(MockitoExtension.class)
class LoginDtoValidatorTest {

    @InjectMocks
    LoginDtoValidator validator;

    @Mock
    LoginRepository loginRepository;

    @Test
    void validate_newUserEmailAlreadyTaken_rejectsEmailField() {
        LoginManageDto dto = new LoginManageDto();
        dto.setEmail("existing@test.com");

        when(loginRepository.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(new Login()));

        Errors errors = new BeanPropertyBindingResult(dto, "loginManageDto");
        validator.validate(dto, errors);

        assertTrue(errors.hasFieldErrors("email"));
        assertEquals("error.email.exists", errors.getFieldError("email").getCode());
    }
}
```

---

### Filter Tests — Mockito

`OncePerRequestFilter.doFilterInternal` is `protected` but callable directly from a same-package test class. No `MockMvc` or `@SpringBootTest` needed.

```java
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtService jwtService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }
}
```

- Always clear `SecurityContextHolder` in `@AfterEach` if the filter writes to it; otherwise authentication state leaks across tests.
- Stub `doThrow` **before** calling `doFilterInternal` — a stub registered after the call has no effect on the already-completed invocation. Wrap the call in `assertThrows` when the filter re-throws (e.g. when only a `finally` block runs with no `catch`).
- To capture log output, attach a Logback `ListAppender` to the named logger in `@BeforeEach` and detach it in `@AfterEach`:

```java
Logger logger = (Logger) LoggerFactory.getLogger("REQUEST-LOG");
ListAppender<ILoggingEvent> appender = new ListAppender<>();
appender.start();
logger.addAppender(appender);
```

Then assert on `appender.list` using `logEvent.getLevel()` and `logEvent.getFormattedMessage()`.

- For generic mocks of parameterised types (e.g. `Page<FeedEntry>`), declare them as class-level `@Mock` fields to avoid raw-type unchecked cast warnings:

```java
@Mock
Page<FeedEntry> feedPage;
```

---

### Utility Tests — Plain JUnit 5

`Utils` is a static utility class. No Spring context is needed.

- **`cleanHtml`**, **`getDefaultShelves`**, **`getErrorResponseDto`**: straightforward input/output. Build `Errors` instances with `BeanPropertyBindingResult` for the last one.
- **`isAuthenticated`**: call `TestUtils.setupSecurityContext(loginDetails)` before asserting `true`; clear with `SecurityContextHolder.clearContext()` in `@AfterEach`.
- **`getImageUrl`**: calls `ServletUriComponentsBuilder.fromCurrentContextPath()`, which requires a request context. Set one up in `@BeforeEach` and tear it down in `@AfterEach`:

```java
@BeforeEach
void setupRequestContext() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
}

@AfterEach
void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
}
```

---

### TestUtils

`com.zedapps.bookshare.util.TestUtils` builds unsaved entity instances. Prefer these over inline construction; add new factory methods here when a new test class needs them.

| Method | Returns | Notes |
|---|---|---|
| `getLogin(email, handle, active)` | `Login` | `Role.USER`, `AuthProvider.LOCAL`, firstName="Test", lastName="User" |
| `getAuthor(firstName, lastName)` | `Author` | |
| `getBook(title, isbn, author, status)` | `Book` | pages=100; single author |
| `getBooks(author, genres, tags)` | `List<Book>` | One book per `TEST_ISBN_DATA` entry, ids 1..n |
| `getGenre(name)` | `Genre` | |
| `getTag(name)` | `Tag` | |
| `getReview(book, login, rating)` | `Review` | content="Review Content" |
| `getReadingProgress(book, user, pagesRead, startDate, endDate, completed)` | `ReadingProgress` | id/updatedAt unset (set explicitly when the test needs them) |
| `getReadingChallenge(login, year, bookCount)` | `ReadingChallenge` | |
| `getShelf(login, name, defaultShelf)` | `Shelf` | |
| `getShelvedBook(book, login, shelf)` | `ShelvedBook` | |
| `getActivityOutboxItem(status)` | `ActivityOutbox` | LOGIN event, referenceId=1 |
| `getActivity(activityType)` | `Activity` | referenceId=1, no login |
| `getLoginDetails(email, handle, active)` | `LoginDetails` | security principal built from `getLogin(...)` |
| `setupSecurityContext(loginDetails)` | `void` | sets `SecurityContextHolder` with a mock `Authentication` |
| `getRegistrationRequestDto(login)` | `RegistrationRequestDto` | password="plain-password" |
| `getLoginManageDto(login)` | `LoginManageDto` | password="plain-password" |

`TestUtils.TEST_ISBN_DATA` is a shared list of valid ISBN strings used to build distinct books.

---

### Mockito Footguns

- **Never combine `@TestInstance(PER_CLASS)` with `@InjectMocks`** — Mockito only resets an `@InjectMocks` field when it is `null`. With `PER_CLASS`, the same instance is reused across tests, silently carrying stale mock references. Use the default `PER_METHOD` lifecycle.
- **Mutable list mocks** — if the service under test calls `List.remove()` or any mutating method on a list returned by a stub, return `new ArrayList<>(items)` from the stub rather than the original live list; otherwise sibling tests see the mutated state.
- **`UnnecessaryStubbingException` from `@BeforeEach`** — Mockito STRICT_STUBS throws if a stub registered in `@BeforeEach` is not consumed by a particular test. Wrap shared stubs that are only exercised by a subset of tests in `lenient().when(...)`. This is the normal pattern for helper tests where `@BeforeEach` sets up all service stubs but individual tests only trigger certain code paths.
- **`PotentialStubbingProblem` with argument-order mismatch** — when a non-lenient stub is registered for `foo(a, b)` but the production code first calls `foo(b, a)` (same method, different argument order), Mockito detects the registered stub was unmatched for that invocation and throws before the test body runs. Fix by adding an explicit stub for the first-call argument variant, or use `lenient()` if the first call's return value does not matter to the test.

---

## Environment Variables Reference

| Variable | Where used | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | docker-compose → app | Full JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | docker-compose → app | DB user |
| `SPRING_DATASOURCE_PASSWORD` | docker-compose → app | DB password |
| `SPRING_PROFILES_ACTIVE` | docker-compose → app | Active Spring profile (`dev`) |
| `DATABASE_NAME` | docker-compose → db | PostgreSQL DB name |
| `DATABASE_USER` | docker-compose → db | PostgreSQL user |
| `DATABASE_PASSWORD` | docker-compose → db | PostgreSQL password |
| `GOOGLE_CLIENT_ID` | .env / secret-dev.properties | OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | .env / secret-dev.properties | OAuth2 client secret |
| `GOOGLE_REFRESH_TOKEN` | .env / secret-dev.properties | Gmail API refresh token |
| `APP_JWT_SECRET` | .env / secret-dev.properties | Base64-encoded HMAC-SHA key for JWT signing |
| `APP_JWT_EXPIRY_MS` | .env / secret-dev.properties | JWT validity window in milliseconds (default 1 800 000 = 30 min) |

Local Spring Boot reads DB config from `application-dev.properties` and secrets from `secret-dev.properties` (not committed — see `secret-dev.properties.example`).

---

## Known Patterns & Gotchas

- **Password field:** `Login.password` is nullable (Google OAuth2 users have no local password). Always check `authProvider` before operating on the password field.
- **Connection bidirectionality:** `Connection` rows are stored in pairs (`person1→person2` and `person2→person1`). When querying connections for a user, query where `person1 = user` OR use the bidirectional query in the repository.
- **Default shelf enforcement:** When adding a book to a default shelf, `BookService.addToShelf()` first removes any existing default-shelf assignment for that book. Do not bypass this logic.
- **Image storage:** Images are stored as byte arrays (LOB) in the `image` table, not on the filesystem. Served via `ImageController`. Max upload: 10 MB.
- **Version string:** The app version is injected at build time via Gradle's `processResources` filter into `application.properties`. Do not hard-code it.
- **Locale:** Language preference is stored in a cookie named `lang`. The `LocaleConfig` bean provides the `LocaleResolver` and `LocaleChangeInterceptor` (`?lang=` query param triggers a switch).
- **Request logging:** All HTTP requests are logged to `logs/request.log` by `RequestLogFilter`. This is separate from the main application log.
- **Structured logging:** Both `logs/request.log` and `logs/server.log` are written as ECS-format JSON via Spring Boot's `StructuredLogEncoder` (configured in `logback-spring.xml`). The `CONSOLE` appender stays plain text for local development. Note: the filter-test `ListAppender` pattern is unaffected — it captures `ILoggingEvent`s before encoding.
- **Virtual threads:** `spring.threads.virtual.enabled=true` (Java 25) routes Tomcat request handling and scheduling onto virtual threads. The `activityPublishExecutor` is a virtual-thread `SimpleAsyncTaskExecutor` with `concurrencyLimit=10` for backpressure against the Hikari pool.
- **Outbox retry:** Failed outbox items are retried up to 3 times. After 3 failures, `status = FAILED` and the item is excluded from future processing. Daily cleanup removes stale entries.
- **Feed window:** The feed only shows activities from the last 30 days. Feed reads are cached per-user for 60 seconds.
- **SpotBugs:** The build runs SpotBugs static analysis. The exclusion filter is at `config/spotbugs/exclude.xml`. Fix SpotBugs warnings rather than excluding them unless there is a good documented reason.
