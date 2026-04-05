# Role Validation Checklist (Buyer, Seller, Admin)

Use this checklist to verify end-to-end behavior for each role.

## 1) Automated Baseline

Run all backend tests:

```powershell
.\mvnw.cmd test
```

Focus file for role flows:

- `src/test/java/com/logarithm/microtask/integration/MarketplaceIntegrationTest.java`

## 2) Manual UI Validation

Start app:

```powershell
.\mvnw.cmd spring-boot:run
```

Open:

- `http://localhost:8081/index.html`

Prepare 3 accounts:

- Buyer account
- Seller account
- Admin account (bootstrap admin from env/config, not self-signup)

## 3) Buyer Validation

- Login as Buyer and confirm redirect to buyer dashboard.
- Create a new task from `create-task.html`.
- Confirm task appears on buyer dashboard (`/tasks/mine` data).
- Open Application Management for that task.
- Accept one seller application.
- Confirm accepted app is `ACCEPTED`, others are `REJECTED`.
- Update task status to `COMPLETED` (after it became `IN_PROGRESS`).
- Delete own task and confirm it no longer appears.

## 4) Seller Validation

- Login as Seller and confirm redirect to Available Tasks.
- Confirm only `OPEN` tasks are shown in seller board.
- Apply to one or more open tasks.
- Confirm duplicate apply on same task is rejected.
- Confirm seller cannot create tasks.
- Confirm seller cannot accept applications.
- Confirm seller can view own applications from API (`GET /api/v1/applications/mine`) and status transitions.

## 5) Admin Validation

- Login as Admin and confirm redirect to `admin-panel.html`.
- Confirm dashboard metrics load:
  - Total/Open/In-Progress/Completed tasks
  - Total/Pending applications
  - Total/Blocked users
- From admin task table:
  - Change task status (OPEN/IN_PROGRESS/COMPLETED)
  - Delete task
  - Jump to application management for selected task
- From admin user table:
  - View all users
  - Block and unblock non-admin users
  - Delete non-admin users when they have no active assignments
- From application management:
  - Load applications by task
  - Load all applications (`Load All (Admin)`)
  - Accept pending application if needed

## 6) Security/Negative Checks

- Unauthenticated request to protected APIs should return `401`.
- Buyer should not apply to task.
- Seller should not create task.
- Non-owner buyer should not accept another buyer's task application.
- Non-admin should not access `/api/v1/applications/admin/all`.
- Non-admin should not access `/api/v1/admin/users`.
