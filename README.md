# 📖 BookShare

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.x-brightgreen.svg)
![Gradle](https://img.shields.io/badge/Build-Gradle-02303A.svg?logo=gradle)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-blue.svg?logo=postgresql)
![Build](https://github.com/smzoha/bookshare/actions/workflows/gradle.yml/badge.svg)
![License: GPL v2](https://img.shields.io/badge/License-GPL_v2-blue.svg)
![Status](https://img.shields.io/badge/Status-Development-yellow)

BookShare is a **modern web application** to track your books, reading progress, and connect with other readers. Keep your library organized and stay updated with friends’ reading activity!

---

## 📌 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Running](#running)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## ✨ Features

- User registration, login, and authentication
- Add books and track reading progress
- Connect with other users and view their activity
- Responsive interface built with **Thymeleaf** and **Bootstrap**
- Secure backend with **Spring Boot** and **Spring Security**
- Easily extensible for reviews, ratings, and more

---

## 🛠 Technology Stack

| Layer      | Technology                                 |
|------------|--------------------------------------------|
| Backend    | Java, Spring Boot, Spring Security         |
| Frontend   | Thymeleaf, Bootstrap, HTML/CSS, JavaScript |
| Database   | PostgreSQL, Flyway                         |
| Build Tool | Gradle                                     |
| Versioning | Git & GitHub                               |

---

## 🚀 Getting Started

### Prerequisites

- Java 25+
- Gradle 8+
- Database setup (PostgreSQL)
- Git

### Installation

```bash
# Clone the repository
git clone https://github.com/smzoha/bookshare.git
cd bookshare

# Configure database in src/main/resources/application-dev.properties:
spring.datasource.url=jdbc:mysql://localhost:3306/bookshare
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
```

### Running the Application
```bash
# Run with Gradle
./gradlew bootRun
```

### Access the app at:
```
http://localhost:6001
```

## 💻 Usage

1. Register a new account or log in with an existing account.
2. Add books to your library.
3. Track reading progress (pages read / percentage).
4. Connect with other users to view their activity.
5. Manage your book collection across shelves.

## 🤝 Contributing

We welcome contributions! Follow these steps:

1. Fork the repository
2. Create a branch: `git checkout -b feature/my-feature` for features, `git checkout -b bugfix/my-fix` for bugfixes
3. Commit your changes: `git commit -m "Add feature"`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

### Guidelines:
- Follow existing code style
- Include tests for new features when applicable
- Update documentation as needed

---

## 📄 License

This project is licensed under the **GNU General Public License v2 (GPL-2.0)**.  
See the full license text in the [LICENSE](LICENSE) file for details.

You are free to:

- Use, copy, and modify the software
- Distribute copies and derivatives under the same license

> This ensures that BookShare and any derivative works remain free software under GPL v2.

---

## 📫 Contact

**BookShare** — Powered by ZedApps

GitHub Profile: [smzoha](https://github.com/smzoha)  
Email: shamah.zoha@gmail.com
