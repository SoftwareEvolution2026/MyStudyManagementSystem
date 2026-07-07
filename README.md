# MyStudyManagementSystem

A study management system built for a Software Evolution project. It helps students plan tasks, run focused study sessions (Pomodoro), track progress on a calendar, and view analytics — via a Spring Boot REST API backend and a native Android (Jetpack Compose) frontend.

## Project structure

```
.
├── backend/   Spring Boot REST API (Java 17, MySQL, JWT auth)
└── frontend/  Android app (Kotlin, Jetpack Compose, Retrofit)
```

## Features

- User registration & login (JWT-based authentication)
- Task management
- Study sessions & calendar view
- Pomodoro timer with logging
- Analytics dashboard
- Reminder notifications (Android)

## Backend

**Stack:** Spring Boot, Spring Data JPA, Spring Security, MySQL, JWT (jjwt), Lombok

### Prerequisites

- Java 17+
- MySQL running locally (or update the connection settings)

### Configuration

Database and JWT settings are in `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sms_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your_jwt_secret
```

Update the username/password to match your local MySQL setup before running. The database schema is created automatically on startup (`spring.jpa.hibernate.ddl-auto=update`).

### Run

```bash
cd backend
./mvnw spring-boot:run       # macOS/Linux
mvnw.cmd spring-boot:run     # Windows
```

The API starts on `http://localhost:8080`.

### Test

```bash
cd backend
./mvnw test
```

## Frontend

**Stack:** Kotlin, Jetpack Compose, Retrofit, OkHttp, DataStore, Coroutines

### Prerequisites

- Android Studio (or the Gradle CLI + Android SDK)
- A running instance of the backend (the app points to `http://10.0.2.2:8080/`, which is the Android emulator's alias for `localhost` on the host machine)

### Run

Open the `frontend/` directory in Android Studio and run the `app` module on an emulator or device, or from the command line:

```bash
cd frontend
./gradlew installDebug        # macOS/Linux
gradlew.bat installDebug      # Windows
```

## Contributing

1. Create a feature branch from `dev`.
2. Make your changes.
3. Open a pull request describing what changed and why.
