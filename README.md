# 📖 BookShare

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.5.5-brightgreen.svg)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?logo=docker)
![Build](https://github.com/smzoha/bookshare/actions/workflows/gradle.yml/badge.svg)
![License: GPL v2](https://img.shields.io/badge/License-GPL_v2-blue.svg)
![Status](https://img.shields.io/badge/Status-Development-yellow)

BookShare is a **modern web application** to track your books, reading progress, and connect with other readers. Keep your library organized and stay updated with friends' reading activity!

---

## 📌 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running)
  - [Running with Docker](#docker)
  - [Set up Gmail API & Google OAuth2 Login](#google)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

<h2 id="features">✨ Features</h2>

**Books & Reading**
- Browse, search, and filter books by genre, tag, and rating
- Track reading progress per book (pages read / percentage)
- Write and like book reviews
- Organize books into custom shelves and view your collection

**Social**
- Send and manage friend requests
- View a social activity feed showing friends' reviews, reading progress updates, and new connections
- Browse user profiles and their public shelves

**Authors**
- Authors can submit book addition requests for admin review

**Authentication & Account Management**
- Standard registration and login
- Google OAuth2 / OIDC login
- Password reset via email (Gmail API)

**Admin Panel**
- Manage users, books, authors, genres, and tags
- Review and approve author book requests
- Actuator dashboard: live JVM heap, CPU, thread, and disk metrics with health status

**General**
- Responsive interface built with **Thymeleaf** and **Bootstrap**
- Internationalization (i18n) support
- Dark mode
- Async activity event system with outbox pattern
- Request logging

---

<h2 id="technology-stack"> 🛠 Technology Stack</h2>

| Layer          | Technology                                                            |
|----------------|-----------------------------------------------------------------------|
| Backend        | Java 25, Spring Boot 3.5.5, Spring Security, Spring Data JPA         |
| Auth           | Spring Security OAuth2 Client (Google OIDC), Spring Mail, Gmail API  |
| Frontend       | Thymeleaf, Bootstrap, jQuery, TinyMCE, Font Awesome                  |
| Database       | PostgreSQL, Flyway                                                    |
| Observability  | Spring Boot Actuator, Micrometer                                     |
| Build & QA     | Gradle, SpotBugs, Lombok                                              |
| Infrastructure | Docker & Docker Compose                                               |
| Versioning     | Git & GitHub                                                          |

---

<h2 id="getting-started">🚀 Getting Started</h2>

<h3 id="prerequisites">Prerequisites</h3>

- Java 25+
- Gradle 8+
- PostgreSQL
- Git
- Docker & Docker Compose _(optional, for containerized setup)_

<h3 id="installation">Installation</h3>

```bash
# Clone the repository
git clone https://github.com/smzoha/bookshare.git
cd bookshare

# Configure database in src/main/resources/application-dev.properties:
spring.datasource.url=jdbc:postgresql://localhost:5432/bookshare
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
```

<h3 id="running">Running the Application</h3>

```bash
# Run with Gradle
./gradlew bootRun
```

### Access the app at:
```
http://localhost:6001
```

---

<h3 id="docker">🐳 Running with Docker</h3>

Docker will spin up both the application and a PostgreSQL database together, with no local setup required.

**1. Set up your environment file:**

```bash
cp .env.example .env
```

Edit `.env` with your preferred credentials:

```env
DATABASE_NAME=bookshare
DATABASE_USER=your_db_user
DATABASE_PASSWORD=your_db_password
```

**2. Start all services:**

```bash
./deploy.sh start
```

**3. Stop all services:**

```bash
./deploy.sh stop
```

> App logs are persisted to the `./logs/` directory on your host machine.

---

<h3 id="google">✉️ 🔐 Set up Gmail API & Google OAuth2 Login</h3>

Follow these steps to set up Google OAuth2 credentials for two features:
- **Gmail API** — sending emails via Gmail
- **Google OAuth2 Login** — Login with Google authentication

**Create OAuth2 Credentials in Google Cloud:**
- Go to [Google Cloud Console](https://console.cloud.google.com/) and select your project (the name defined for this project is "bookshare").
- **Enable Required APIs**:
  - APIs & Services → Library → **Gmail API** → Enable
- **Configure OAuth Consent Screen**:
  - App Type: External
  - Fill in App Name and Support Email (bookshare and your choice of Gmail address respectively)
  - Add the following **OAuth2 scopes**:
    - `https://www.googleapis.com/auth/gmail.send` — Send emails via Gmail
    - `openid` — Google Login: authenticate user identity
    - `https://www.googleapis.com/auth/userinfo.email` — Google Login: access user's email address
    - `https://www.googleapis.com/auth/userinfo.profile` — Google Login: access user's basic profile info
- **Create OAuth Client ID**:
  - Application Type: Web application
  - Add **Authorized redirect URIs**:
    - `http://localhost:6001` *(for refresh token generation)*
    - `http://localhost:6001/login/oauth2/code/google` *(for Google Login callback — adjust host/port as needed)*
    - `http://localhost:6001/oauth2/authorization/google` *(for Google Login callback — adjust host/port as needed)*
  - Copy the `client_id` and `client_secret`

**Generate Refresh Token (for Gmail sending):**
- Open the following URL in your browser (replace `YOUR_CLIENT_ID`):
```
  https://accounts.google.com/o/oauth2/v2/auth?
  client_id=YOUR_CLIENT_ID
  &redirect_uri=http://localhost:6001
  &response_type=code
  &scope=https://www.googleapis.com/auth/gmail.send
  &access_type=offline
  &prompt=consent
```
- Login with your Gmail account and allow access.
- Copy the `code` from the redirected URL: `http://localhost:6001/?code=AUTH_CODE`
- Exchange `AUTH_CODE` for tokens via POST request:
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

code=AUTH_CODE&
client_id=YOUR_CLIENT_ID&
client_secret=YOUR_CLIENT_SECRET&
redirect_uri=http://localhost:6001&
grant_type=authorization_code
```
- The response will include the refresh token:
```json
{
  "access_token": "...",
  "expires_in": 3599,
  "refresh_token": "YOUR_REFRESH_TOKEN",
  "scope": "https://www.googleapis.com/auth/gmail.send",
  "token_type": "Bearer"
}
```

> **Note:** The Google Login flow is handled automatically by Spring Security OAuth2 — no manual token exchange is needed for login.

**Add Secret Tokens to env and properties files:**
- Add the following key/value pairs to the `.env` file (for Docker deployment)
  - Refer to the `.env.example` for example
```
GOOGLE_CLIENT_ID=your_client_id
GOOGLE_CLIENT_SECRET=your_client_secret
GOOGLE_REFRESH_TOKEN=your_refresh_token
```
- For Spring Boot deployment, add a `secrets-dev.properties` file and include the following properties
  - Refer to `secret-dev.properties.example` for example
```
app.gmail.client.id=${GOOGLE_CLIENT_ID:client}
app.gmail.client.secret=${GOOGLE_CLIENT_SECRET:secret}
app.gmail.refresh.token=${GOOGLE_REFRESH_TOKEN:token}

spring.security.oauth2.client.registration.google.client-id=${app.gmail.client.id}
spring.security.oauth2.client.registration.google.client-secret=${app.gmail.client.secret}
```

---

<h2 id="usage">💻 Usage</h2>

1. Register a new account or sign in with Google.
2. Browse the book catalog — search and filter by genre, tag, or rating.
3. Add books to shelves and track your reading progress.
4. Write reviews and like others' reviews.
5. Send friend requests and follow your friends' activity in the feed.
6. Authors can submit new book requests via their author profile.
7. Admins manage users, books, authors, genres, and tags through the admin panel, and can monitor application health via the Actuator Dashboard.

<h2 id="contributing">🤝 Contributing</h2>

We welcome contributions! Follow these steps:

1. Fork the repository
2. Create a branch: `git checkout -b feat/my-feature` for features, `git checkout -b bugfix/my-fix` for bugfixes
3. Commit your changes: `git commit -m "Add feature"`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

### Guidelines:
- Follow existing code style
- Include tests for new features when applicable
- Update documentation as needed

---

<h2 id="license">📄 License</h2>

This project is licensed under the **GNU General Public License v2 (GPL-2.0)**.  
See the full license text in the [LICENSE](LICENSE) file for details.

You are free to:

- Use, copy, and modify the software
- Distribute copies and derivatives under the same license

> This ensures that BookShare and any derivative works remain free software under GPL v2.

---

<h2 id="contact">📫 Contact</h2>

**BookShare** — Powered by ZedApps

GitHub Profile: [smzoha](https://github.com/smzoha)  
Email: shamah.zoha@gmail.com