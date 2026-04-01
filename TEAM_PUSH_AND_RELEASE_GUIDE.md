# Micro Task Marketplace SWE - Team Push and Merge Guide

## Team Members
- Zisan: Backend Core Owner
- Nafiz: UI, Testing, and Integration Owner

## Current Branch Model
- main: production-ready only
- develop: integration branch for all completed features
- feature branches: one branch per task/module

## Ownership Split

### Zisan (Backend Core)
Responsibilities:
- Domain and persistence consistency
- Security and authorization rules
- API behavior correctness
- Production config safety

Primary folders:
- src/main/java/com/logarithm/microtask/entity
- src/main/java/com/logarithm/microtask/repository
- src/main/java/com/logarithm/microtask/service
- src/main/java/com/logarithm/microtask/security
- src/main/java/com/logarithm/microtask/exception
- src/main/java/com/logarithm/microtask/config

Exact files (current baseline):
- src/main/java/com/logarithm/microtask/entity/BaseEntity.java
- src/main/java/com/logarithm/microtask/entity/Role.java
- src/main/java/com/logarithm/microtask/entity/User.java
- src/main/java/com/logarithm/microtask/entity/Task.java
- src/main/java/com/logarithm/microtask/entity/Application.java
- src/main/java/com/logarithm/microtask/entity/TaskAssignment.java
- src/main/java/com/logarithm/microtask/entity/enums/RoleName.java
- src/main/java/com/logarithm/microtask/entity/enums/TaskStatus.java
- src/main/java/com/logarithm/microtask/entity/enums/ApplicationStatus.java
- src/main/java/com/logarithm/microtask/repository/RoleRepository.java
- src/main/java/com/logarithm/microtask/repository/UserRepository.java
- src/main/java/com/logarithm/microtask/repository/TaskRepository.java
- src/main/java/com/logarithm/microtask/repository/ApplicationRepository.java
- src/main/java/com/logarithm/microtask/repository/TaskAssignmentRepository.java
- src/main/java/com/logarithm/microtask/service/AuthService.java
- src/main/java/com/logarithm/microtask/service/TaskService.java
- src/main/java/com/logarithm/microtask/service/ApplicationService.java
- src/main/java/com/logarithm/microtask/service/impl/AuthServiceImpl.java
- src/main/java/com/logarithm/microtask/service/impl/TaskServiceImpl.java
- src/main/java/com/logarithm/microtask/service/impl/ApplicationServiceImpl.java
- src/main/java/com/logarithm/microtask/security/JwtService.java
- src/main/java/com/logarithm/microtask/security/JwtAuthenticationFilter.java
- src/main/java/com/logarithm/microtask/security/CustomUserDetailsService.java
- src/main/java/com/logarithm/microtask/exception/ResourceNotFoundException.java
- src/main/java/com/logarithm/microtask/exception/BadRequestException.java
- src/main/java/com/logarithm/microtask/exception/ForbiddenOperationException.java
- src/main/java/com/logarithm/microtask/exception/GlobalExceptionHandler.java
- src/main/java/com/logarithm/microtask/config/SecurityConfig.java
- src/main/java/com/logarithm/microtask/config/JpaAuditConfig.java
- src/main/resources/application.properties
- docker-compose.yml
- render.yaml

### Nafiz (UI + Testing + Integration)
Responsibilities:
- UI/UX pages and static resources
- API DTO contract consistency with controllers
- Unit and integration test quality
- CI stability and developer docs

Primary folders:
- src/main/java/com/logarithm/microtask/controller
- src/main/java/com/logarithm/microtask/dto
- src/main/resources/static
- src/main/resources/templates
- src/test
- .github/workflows
- postman

Exact files (current baseline):
- src/main/java/com/logarithm/microtask/controller/AuthController.java
- src/main/java/com/logarithm/microtask/controller/TaskController.java
- src/main/java/com/logarithm/microtask/controller/ApplicationController.java
- src/main/java/com/logarithm/microtask/dto/auth/RegisterRequest.java
- src/main/java/com/logarithm/microtask/dto/auth/LoginRequest.java
- src/main/java/com/logarithm/microtask/dto/auth/AuthResponse.java
- src/main/java/com/logarithm/microtask/dto/task/TaskCreateRequest.java
- src/main/java/com/logarithm/microtask/dto/task/TaskUpdateRequest.java
- src/main/java/com/logarithm/microtask/dto/task/TaskResponse.java
- src/main/java/com/logarithm/microtask/dto/application/ApplicationCreateRequest.java
- src/main/java/com/logarithm/microtask/dto/application/ApplicationResponse.java
- src/main/java/com/logarithm/microtask/dto/taskassignment/TaskAssignmentResponse.java
- src/main/java/com/logarithm/microtask/dto/common/ApiErrorResponse.java
- src/main/resources/static/index.html
- src/main/resources/static/css/styles.css
- src/main/resources/static/js/app.js
- src/test/java/com/logarithm/microtask/MicrotaskMarketplaceApplicationTests.java
- src/test/java/com/logarithm/microtask/integration/MarketplaceIntegrationTest.java
- src/test/java/com/logarithm/microtask/service/impl/AuthServiceImplTest.java
- src/test/java/com/logarithm/microtask/service/impl/TaskServiceImplTest.java
- src/test/java/com/logarithm/microtask/service/impl/ApplicationServiceImplTest.java
- src/test/resources/application-test.properties
- .github/workflows/ci.yml
- postman/MicroTaskMarketplace.postman_collection.json
- postman/MicroTaskMarketplace.local.postman_environment.json
- TEAM_PUSH_AND_RELEASE_GUIDE.md

## 3-Day Execution Plan (Remaining Work)

### Day 1: API Contract and Stability Lock
Zisan:
- Finalize service/business-rule edge cases (ownership checks, status transitions, duplicate application behavior)
- Finalize security access boundaries and forbidden responses
- Ensure exception mapping is consistent and production-safe

Nafiz:
- Freeze request/response DTO contract and validation messages
- Align controllers with DTO contracts and HTTP status consistency
- Update Postman collection to match finalized endpoints

Day 1 exit criteria:
- Both feature branches pushed
- CI green for both PRs
- No contract mismatch between controller and DTO layers

### Day 2: Test Depth and UI Reliability
Zisan:
- Add/refine service tests for newly fixed edge cases
- Verify Docker behavior for local startup and environment variables

Nafiz:
- Expand integration tests for full happy-path and one major failure-path
- Improve minimal UI behavior: clear API error display and token persistence flow
- Confirm CI workflow remains stable on develop

Day 2 exit criteria:
- Unit + integration tests stable locally and in CI
- UI can execute register/login/task/apply/accept without manual token copy errors

### Day 3: Release Readiness and Main Merge
Zisan:
- Final backend regression pass (security + service)
- Confirm Render env variable contract and deployment settings

Nafiz:
- Final docs pass (guide + API usage)
- Final Postman run-through and screenshot evidence for report/demo

Day 3 exit criteria:
- Create PR: develop -> main
- Both reviewers approve
- CI fully green on latest develop
- Merge to main and create release tag

## Branches to Use for This 3-Day Plan
- feature/zisan-service-security-hardening
- feature/zisan-release-readiness
- feature/nafiz-api-contract-ui
- feature/nafiz-testing-ci-docs

## Safe Push Process (For Remaining Work)
Use this exact flow for every feature:

1. Pull latest develop
- git checkout develop
- git pull origin develop

2. Create a focused feature branch
- git checkout -b feature/<short-feature-name>

3. Implement small, scoped changes only
- Keep each PR to one concern
- Avoid mixing refactor + feature + test infra in one PR

4. Run local quality checks before push
- .\\mvnw.cmd clean verify
- docker compose config

5. Push feature branch
- git push -u origin feature/<short-feature-name>

6. Open PR: feature -> develop
- Require CI green
- Require 1 reviewer approval (other teammate)
- Resolve comments, re-run checks

7. Merge to develop only when checks are green

## When to Merge Develop to Main
Merge develop -> main only when all release gates are true:

1. CI is green for latest develop commit
2. Critical workflows pass:
- build and test
- docker image build

3. Manual sanity checks pass:
- auth/register/login
- create task
- apply task
- accept application
- home UI loads without auth popup

4. No open high-severity issues

5. Release PR approved by both Zisan and Nafiz

## Commit Rules (Required)
- Keep commits small and atomic
- One logical change per commit
- Commit message format:
  - feat: ...
  - fix: ...
  - test: ...
  - ci: ...
  - docs: ...
  - refactor: ...

Examples:
- feat: add seller application acceptance flow
- fix: allow static index route in security config
- test: add integration tests for auth and task flow
- ci: fix mvnw execute permission on linux runner

## Conflict Prevention Rules
- Do not edit files owned by other person without prior sync
- If cross-module edits are needed, create a short sync issue first
- Rebase feature branches frequently on develop
- Prefer additive changes over broad rewrites

## Deployment Policy (Render)
### Should we deploy now as CD?
Short answer: not yet as automatic production CD.

Recommended now:
- Keep CI fully active on develop and main
- Add Render deployment as manual or release-gated

Why:
- Team is still integrating remaining work
- Frequent schema/security changes can break live environment
- Safer to stabilize and then enable auto deploy

## Recommended Deployment Stages

Stage 1 (Now):
- CI only (build/test/docker build)
- No automatic production deploy

Stage 2 (After stabilization):
- Auto deploy only on main
- Protect main with required checks and approvals

Stage 3 (Optional):
- Add preview/staging deploy from develop
- Keep production deploy from main only

## Render Readiness Checklist Before Enabling Auto CD
- Environment variables finalized (DB/JWT)
- Health endpoint and startup reliability confirmed
- DB migration strategy defined
- Rollback plan documented
- At least two consecutive green CI runs on main

## Daily Team Routine
- Start of day: sync from develop
- End of day: push feature branch and open/update PR
- Never push unfinished unstable work directly to develop/main
