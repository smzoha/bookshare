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
│   └── FeedEntryListener        # @EventListener: fans out FeedEntry rows to connections
├── config/
│   ├── AsyncConfig              # activityPublishExecutor: core=2, max=5, queue=1000
│   ├── CacheConfig              # Caffeine cache definitions (see cache table below)
│   ├── LocaleConfig             # CookieLocaleResolver (cookie name: lang, 30-day TTL)
│   └── SecurityConfig           # HTTP security rules, form login, OAuth2 OIDC
├── controller/
│   ├── admin/ActuatorDashboardController   # /admin/actuator/dashboard
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
│           ├── HomeController              # /, /feed, /admin
│           ├── LoginController             # /login, /register
│           ├── PasswordResetController     # /resetPasswordRequest, /resetPassword
│           ├── ProfileController           # /profile/**
│           ├── ShelfController             # /shelf/add
│           └── CollectionController        # /collection
├── dto/                         # Data Transfer Objects (read these before touching forms)
├── editor/                      # PropertyEditors for form binding (Author, Genre, Tag, Image)
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
│       ├── ReadingProgress      # table: reading_progress
│       ├── Review               # table: review
│       ├── Shelf                # table: shelf
│       └── ShelvedBook          # table: shelved_book
├── enums/                       # ActivityStatus, ActivityType, AuthProvider, Role, Status
├── exception/                   # Custom exceptions
├── filter/RequestLogFilter      # Logs all HTTP requests to request.log
├── repository/                  # Spring Data JPA repositories (one per entity)
├── service/
│   ├── activity/ActivityService
│   ├── book/BookService
│   ├── book/BookAdminService
│   ├── login/FeedService
│   ├── login/LoginService
│   ├── login/PasswordResetService
│   ├── login/ProfileService
│   ├── login/ShelfService
│   ├── LoginDetailService       # UserDetailsService implementation
│   ├── LoginDetailOidcService   # OAuth2UserService for Google OIDC
│   └── MailService              # Gmail API email sending (@Async)
├── util/                        # Utility helpers
└── validator/                   # Custom Spring validators

src/main/resources/
├── application.properties       # Core config (port 6001, JPA, Flyway, caching, actuator)
├── application-dev.properties   # Dev DB connection, devtools, mail, imports secret-dev.properties
├── secret-dev.properties.example
├── db/migration/                # Flyway SQL migrations (V1–V20)
│   ├── 09_2025/                 # V1–V8_2
│   ├── 10_2025/                 # V9
│   ├── 01_2026/                 # V10–V12_1
│   ├── 02_2026/                 # V13
│   └── 03_2026/                 # V14–V20
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

Defined in `SecurityConfig` — match these when adding new routes.

| URL Pattern | Access |
|---|---|
| `/admin/**` | ADMIN only |
| `/manage/**` | ADMIN or MODERATOR |
| `/manage/book` | AUTHOR |
| `/profile/**` | Any authenticated role |
| `/book/add*`, `/book/remove*`, `/book/update*`, `/book/like`, `/shelf/add`, `/collection/**` | Any authenticated user |
| `/resetPasswordRequest`, `/resetPassword` | Anonymous only |
| `/author/apply` | USER role only |
| `/author/bookRequest` | AUTHOR role only |
| `/actuator/**` | ADMIN only |
| Everything else | Public |

CSRF is disabled. Sessions use standard Spring Security session management. Logout at `/logout` clears `JSESSIONID`.

The authenticated user principal is always a `LoginDetails` object (implements `UserDetails`, `OidcUser`, `OAuth2User`). Retrieve it in controllers with `@AuthenticationPrincipal LoginDetails loginDetails`.

---

## Activity System

**All significant user actions must fire an outbox event.** Never write directly to `activity` — always go through `ActivityService.saveActivityOutbox()`.

### How it works

1. Service calls `activityService.saveActivityOutbox(type, referenceId, payload)` within the same DB transaction as the main write.
2. `ActivityOutboxProcessor.processOutbox()` runs every 15 seconds, picks up PENDING rows (top 100), creates `Activity` records, and marks outbox rows COMPLETED or FAILED.
3. For non-internal activity types (listed in `FEED_ACTIVITIES`), `ActivityService.saveActivity()` publishes an `ActivityFeedDto` Spring event.
4. `FeedEntryListener` handles the event, loads the actor's connections, and inserts one `FeedEntry` per connection member.

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
| `SEND_FRIEND_REQ` / `REVOKE_FRIEND_REQ` | Friend request actions |
| `ACCEPT_FRIEND_REQ` / `DECLINE_FRIEND_REQ` | Friend request response |
| `REMOVE_FRIEND` | Connection removal |
| `ADD_FRIEND` | Post-acceptance (triggers feed) |
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
1. File name: `V{N}__{description}.sql` where `N` continues from the current highest version (V20).
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
- **Model population:** Service methods like `bookService.setupReferenceData(...)` and `profileService.setupReferenceData(...)` handle bulk model population — use them rather than calling multiple services in the controller.

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
- **Outbox retry:** Failed outbox items are retried up to 3 times. After 3 failures, `status = FAILED` and the item is excluded from future processing. Daily cleanup removes stale entries.
- **Feed window:** The feed only shows activities from the last 30 days. Feed reads are cached per-user for 60 seconds.
- **SpotBugs:** The build runs SpotBugs static analysis. The exclusion filter is at `config/spotbugs/exclude.xml`. Fix SpotBugs warnings rather than excluding them unless there is a good documented reason.
