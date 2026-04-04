# Micro Task Marketplace

A full-stack role-based marketplace where buyers post micro tasks, sellers apply, and buyers or admins accept the best application.

This project is built with Spring Boot, JWT security, PostgreSQL, and a role-aware web UI.

## Features

- JWT authentication with secure login and registration
- Role-based authorization for ADMIN, BUYER, and SELLER
- Task lifecycle management: OPEN, IN_PROGRESS, COMPLETED
- Application lifecycle management: PENDING, ACCEPTED, REJECTED
- Ownership and admin checks for secure task/application actions
- Global API exception handling with consistent error response shape
- Modern static UI pages for each role flow
- Unit tests and integration tests with CI automation
- Docker support for local full-stack startup
- Render deployment config for cloud hosting

## Tech Stack

- Backend: Java 17, Spring Boot 3.2.5
- Security: Spring Security, JWT (jjwt)
- Database: PostgreSQL (prod/dev), H2 (tests)
- Persistence: Spring Data JPA, Hibernate
- Validation: Jakarta Validation
- Frontend: HTML, CSS, JavaScript (served from Spring Boot static resources)
- Build Tool: Maven Wrapper
- Testing: JUnit 5, Mockito, Spring Boot Test, MockMvc
- DevOps: Docker, Docker Compose, GitHub Actions, Render

## System Architecture

📌 [Insert Architecture Diagram Here]

High-level flow:

1. User accesses frontend pages.
2. Frontend calls backend REST APIs.
3. Spring Security validates JWT token and roles.
4. Controller calls Service layer for business rules.
5. Service layer uses Repository layer for database access.
6. Database stores users, tasks, applications, and assignments.

## ER Diagram

📌 [Insert ER Diagram Here]

Core relationships:

- User ↔ Role: many-to-many
- User (buyer) → Task: one-to-many
- Task → Application: one-to-many
- User (seller) → Application: one-to-many
- Task → TaskAssignment: one-to-one
- User (seller) → TaskAssignment: one-to-many

## API Endpoints

| Method | Endpoint | Auth | Allowed Roles | Description |
|---|---|---|---|---|
| POST | /api/v1/auth/register | No | Public | Register user and return token |
| POST | /api/v1/auth/login | No | Public | Login and return token |
| GET | /api/v1/tasks | Yes | Any authenticated user | List all tasks |
| GET | /api/v1/tasks/{taskId} | Yes | Any authenticated user | Get task by id |
| POST | /api/v1/tasks | Yes | BUYER, ADMIN | Create task |
| PUT | /api/v1/tasks/{taskId} | Yes | BUYER, ADMIN (owner or admin check) | Update task |
| DELETE | /api/v1/tasks/{taskId} | Yes | BUYER, ADMIN (owner or admin check) | Delete task |
| POST | /api/v1/applications | Yes | SELLER, BUYER, ADMIN | Apply to a task |
| GET | /api/v1/applications/task/{taskId} | Yes | BUYER, SELLER, ADMIN | List applications for a task |
| POST | /api/v1/applications/{applicationId}/accept | Yes | BUYER, SELLER, ADMIN (service enforces buyer/admin ownership rule) | Accept one application |

## Project Structure

```text
Micro-Task-Marketplace-SWE/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
├── postman/
│   ├── MicroTaskMarketplace.local.postman_environment.json
│   └── MicroTaskMarketplace.postman_collection.json
├── src/
│   ├── main/
│   │   ├── java/com/logarithm/microtask/
│   │   │   ├── config/
│   │   │   │   ├── JpaAuditConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ApplicationController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   └── TaskController.java
│   │   │   ├── dto/
│   │   │   │   ├── application/
│   │   │   │   ├── auth/
│   │   │   │   ├── common/
│   │   │   │   ├── task/
│   │   │   │   └── taskassignment/
│   │   │   ├── entity/
│   │   │   │   ├── enums/
│   │   │   │   ├── Application.java
│   │   │   │   ├── BaseEntity.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── Task.java
│   │   │   │   ├── TaskAssignment.java
│   │   │   │   └── User.java
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   └── MicrotaskMarketplaceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── css/styles.css
│   │           ├── js/app.js
│   │           ├── admin-panel.html
│   │           ├── application-management.html
│   │           ├── apply-task.html
│   │           ├── available-tasks.html
│   │           ├── buyer-dashboard.html
│   │           ├── create-task.html
│   │           ├── index.html
│   │           ├── seller-dashboard.html
│   │           ├── signup.html
│   │           └── task-status.html
│   └── test/
│       ├── java/com/logarithm/microtask/
│       │   ├── integration/MarketplaceIntegrationTest.java
│       │   ├── service/impl/ApplicationServiceImplTest.java
│       │   ├── service/impl/AuthServiceImplTest.java
│       │   ├── service/impl/TaskServiceImplTest.java
│       │   └── MicrotaskMarketplaceApplicationTests.java
│       └── resources/application-test.properties
├── .dockerignore
├── .gitattributes
├── .gitignore
├── application.yml.example
├── docker-compose.yml
├── Dockerfile
├── HELP.md
├── LICENSE
├── mvnw
├── mvnw.cmd
├── pom.xml
├── render.yaml
└── TEAM_PUSH_AND_RELEASE_GUIDE.md
```

## Setup Instructions

### Local Setup

1. Clone repository.
2. Configure database and JWT values in environment variables or .env.
3. Run application:

```bash
./mvnw spring-boot:run
```

For Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

4. Open UI at:

- http://localhost:8081/index.html

### Docker Setup

Run everything (app + PostgreSQL):

```bash
docker compose up --build -d
```

Access app at:

- http://localhost:8081/index.html

Stop containers:

```bash
docker compose down
```

## CI/CD Pipeline

The pipeline is defined in .github/workflows/ci.yml.

On push or pull request to main/develop:

1. Checkout source
2. Set up Java 17
3. Run Maven clean verify (build + tests)
4. Build Docker image to validate containerization

This prevents broken code from being merged and keeps release quality stable.

## Deployment (Render)

Deployment configuration is in render.yaml.

- Provisions PostgreSQL database
- Deploys web service using Docker
- Injects database and app environment variables
- Supports automatic deploy from main branch

Required environment values:

- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- APP_SECURITY_JWT_SECRET
- APP_SECURITY_JWT_EXPIRATION_MS
- SPRING_JPA_HIBERNATE_DDL_AUTO

## Team Members

- Zisan: Backend Core Owner
- Nafiz: UI, Testing, and Integration Owner

## Future Improvements

- Add pagination/filtering/sorting for task lists
- Add refresh token flow and token revocation
- Add endpoint-level OpenAPI documentation
- Add migration tooling (Flyway or Liquibase)
- Add staging deployment before production
- Expand test coverage to controller security edge cases
- Add notifications for application acceptance/rejection
