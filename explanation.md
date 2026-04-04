# Micro Task Marketplace - Simple Project Explanation

## 1. Project Overview (Simple)

Micro Task Marketplace is a small freelancing platform.

- A BUYER posts a task with budget and details.
- A SELLER sees open tasks and applies.
- The BUYER chooses one application.
- When accepted, that task is assigned and moves to IN_PROGRESS.

ADMIN can monitor and manage more actions across the system.

This project has:

- Backend API (Spring Boot)
- Database (PostgreSQL)
- Frontend pages (HTML/CSS/JS)
- Automated testing
- Docker setup
- CI/CD pipeline
- Render deployment setup

## 2. How the System Works (Step by Step)

### Step 1: User creates account

- User goes to signup page.
- User enters name, email, password, role.
- Backend saves user with encrypted password.
- Backend returns JWT token.

### Step 2: User logs in

- User logs in with email and password.
- Backend verifies credentials.
- Backend returns JWT token and roles.
- Frontend stores token in browser localStorage.

### Step 3: Frontend sends secured API requests

- Frontend includes token in Authorization header:
  - Bearer <token>
- JWT filter validates token.
- If token is valid, request goes to API logic.
- If token is missing/invalid, request fails.

### Step 4: Buyer creates a task

- Buyer opens Create Task page.
- Sends title, description, budget.
- Backend creates task with status OPEN.

### Step 5: Seller applies to task

- Seller opens task board and chooses a task.
- Sends taskId, proposedAmount, coverLetter.
- Backend saves application as PENDING.
- Same seller cannot apply twice to same task.

### Step 6: Buyer/Admin accepts application

- Buyer loads applications by task id.
- Clicks accept on one application.
- Backend does these actions:
  - Selected application = ACCEPTED
  - Other applications for same task = REJECTED
  - Task status = IN_PROGRESS
  - TaskAssignment record is created

## 3. Role-Based Access (ADMIN, BUYER, SELLER)

### ADMIN

- Can do buyer-level task management actions.
- Can review applications.
- Can accept applications even if not owner in normal business flow.
- Can access broader dashboard views.

### BUYER

- Can create/update/delete tasks.
- Can view tasks.
- Can review applications for tasks.
- Can accept applications for own tasks.

### SELLER

- Can view tasks.
- Can apply to tasks.
- Can view application listing endpoints.
- Cannot create/update/delete buyer tasks.

## 4. Backend Architecture (Controller -> Service -> Repository)

The backend uses a layered architecture.

### Controller layer

- Receives HTTP requests.
- Validates request body/path with annotations.
- Extracts authentication details.
- Calls service methods.
- Returns HTTP response with status code.

Example controllers:

- AuthController
- TaskController
- ApplicationController

### Service layer

- Contains main business rules.
- Checks ownership and permissions.
- Handles status transitions.
- Converts entities to response DTOs.

Example services:

- AuthServiceImpl
- TaskServiceImpl
- ApplicationServiceImpl

### Repository layer

- Talks directly to the database using Spring Data JPA.
- Provides CRUD and custom finder methods.

Example repositories:

- UserRepository
- TaskRepository
- ApplicationRepository
- TaskAssignmentRepository
- RoleRepository

## 5. Database Design (Simple Relationship Explanation)

Main tables/entities:

- users
- roles
- user_roles (join table)
- tasks
- applications
- task_assignments

How they connect:

- One user can have multiple roles.
- One buyer can create many tasks.
- One task can have many applications.
- One seller can submit many applications.
- One task can be assigned to one seller (one task_assignment record).

Common status fields:

- TaskStatus: OPEN, IN_PROGRESS, COMPLETED
- ApplicationStatus: PENDING, ACCEPTED, REJECTED

Audit fields:

- createdAt and updatedAt are automatically maintained for each entity through BaseEntity + JPA auditing.

## 6. Testing Explanation (Very Detailed)

### What is a unit test?

A unit test checks one small piece of code in isolation.

In this project, unit tests mostly target service classes.

- Dependencies (repository, jwt service, etc.) are mocked.
- Only service logic is tested.
- This helps verify business rules without starting full app or real DB.

### What is an integration test?

An integration test checks multiple layers together.

In this project, integration tests use:

- Spring Boot test context
- MockMvc for HTTP request simulation
- Real controller + service + repository interaction
- H2 in-memory database (test profile)

### Which parts are tested?

#### Unit test coverage

1. AuthServiceImplTest
- Register success
- Default role assignment
- Duplicate email rejection
- Missing role auto-creation behavior
- Login success
- Login failure behavior

2. TaskServiceImplTest
- Create task success/failure
- Task retrieval
- Owner update rules
- Admin override update/delete rules
- Forbidden update for non-owner non-admin

3. ApplicationServiceImplTest
- Apply success
- Apply blocked for non-OPEN task
- Duplicate application blocked
- Accept success flow with status updates
- Non-owner accept blocked
- Already-assigned task blocked

#### Integration test coverage

MarketplaceIntegrationTest validates full API behavior:

- Unauthenticated task listing is rejected
- Register + login flow works
- Full happy path:
  - buyer creates task
  - seller applies
  - buyer accepts
- Duplicate application from same seller is rejected

### How tests are written (JUnit, Mockito, MockMvc)

#### JUnit 5

- Main testing framework.
- Defines test methods using @Test.
- Handles assertions and test lifecycle.

#### Mockito

- Used in unit tests.
- Creates mock objects for repositories/services.
- Verifies interactions and behavior.
- Keeps tests fast and focused.

#### MockMvc

- Used in integration tests.
- Sends fake HTTP requests to endpoints.
- Checks HTTP status and response body.
- Good for testing API contracts end-to-end.

### Why testing is important

- Finds bugs before deployment.
- Protects existing features when new code is added.
- Gives confidence for refactoring.
- Documents expected behavior in executable form.
- Prevents security/business rule regressions.

Without tests, small changes can silently break login, permissions, or status transitions.

## 7. CI/CD Pipeline Explanation (Very Detailed)

CI configuration file:

- .github/workflows/ci.yml

### What happens when code is pushed?

When code is pushed to main or develop, or when a PR is opened:

1. GitHub Actions starts automatically.
2. Runner machine (ubuntu-latest) is created.
3. Code is checked out.
4. Java 17 is installed.
5. Maven dependency cache is prepared.
6. Maven command clean verify is executed.
7. All unit and integration tests run.
8. Docker image build is executed.

If any step fails, workflow is marked failed.

### How GitHub Actions works here

- Workflow trigger rules are written in YAML.
- Each run has one main job: build-and-test.
- Steps run in sequence.
- Concurrency rule cancels old running jobs for same branch update.

### Build process details

Maven clean verify does:

- clean: removes old build files
- compile: compiles source code
- test: runs test suite
- verify: validates full build lifecycle success

### Test execution details

- Unit tests run quickly with mocks.
- Integration tests spin Spring context and use MockMvc + H2.
- Failures stop pipeline immediately.

### Why CI is important

- Stops broken code from entering important branches.
- Gives fast feedback to developers.
- Makes team collaboration safer.
- Standardizes quality checks for everyone.

### How CI ensures code quality

- Every change is tested the same way.
- Security/permission regressions are caught early.
- Docker build check ensures deployment artifact can be created.
- PRs can be reviewed with confidence when CI is green.

## 8. Docker Explanation

### Why Docker is used

Docker makes environment consistent.

- Works same on every machine.
- Removes local setup mismatch issues.
- Packages app and dependencies in a container.

### What Dockerfile does

This project uses multi-stage build:

1. Maven image builds jar file.
2. Lightweight JRE image runs jar.

This keeps runtime image smaller and cleaner.

### What docker-compose does

docker-compose starts multiple containers together:

- postgres service (database)
- app service (Spring Boot backend)

It also:

- sets environment variables
- maps ports
- adds DB health check
- ensures app waits for DB readiness

## 9. Deployment (Render) - Simple Explanation

Render deployment is described in render.yaml.

How it works:

1. Render creates a managed PostgreSQL database.
2. Render builds and runs the app from Dockerfile.
3. Render injects environment variables (DB URL/user/password, JWT settings).
4. App starts and connects to the managed database.
5. New push to main can auto-deploy updated code.

In short:

- GitHub provides source code.
- Render builds and hosts the app.
- Managed database is attached automatically.

## Extra Note on UI Technology

The project includes Thymeleaf dependency in backend dependencies.

Current UI pages are served from static resources (HTML/CSS/JS) in src/main/resources/static. This means the current UI is client-side rendered pages, not server-side Thymeleaf templates.
