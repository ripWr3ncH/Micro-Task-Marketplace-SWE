# Postman API Guide

This guide explains how API calls work in this project and how REST principles are followed.

## Base URL and Variables
Use environment file: [MicroTaskMarketplace.local.postman_environment.json](MicroTaskMarketplace.local.postman_environment.json)

Main variables:
- `baseUrl`
- `buyerEmail`, `sellerEmail`, `adminEmail`
- `defaultPassword`, `adminPassword`
- `buyerToken`, `sellerToken`, `adminToken`
- `taskId`, `applicationId`, `sellerUserId`, `buyerUserId`

## Recommended Run Order
1. Auth > Register Buyer
2. Auth > Register Seller
3. Auth > Login Buyer
4. Auth > Login Seller
5. Auth > Login Admin
6. Tasks > Create Task (Buyer)
7. Applications > Apply To Task (Seller)
8. Applications > Get Applications By Task (Buyer)
9. Applications > Accept Application (Buyer)
10. Admin Users > Get All Users (Admin)
11. Admin Users > Block Seller (Admin)
12. Admin Users > Login Blocked Seller (Should Fail)
13. Admin Users > Unblock Seller (Admin)
14. Admin Users > Login Seller (After Unblock)
15. Auth > Logout Active Token

## How API Calls Are Happening

### 1. Authentication Flow
Collection section: `Auth`

- Register endpoints send JSON payloads with role list.
- Login endpoints return JWT token.
- Test scripts automatically save tokens to environment variables.
- Logout sends `Authorization: Bearer <token>` and revokes token server-side.

### 2. Buyer Flow
Collection sections: `Tasks`, `Applications`

- Buyer creates task with `POST /api/v1/tasks`.
- Buyer manages own tasks with `GET /api/v1/tasks/mine` and `PUT /api/v1/tasks/{taskId}`.
- Buyer reviews seller applications with `GET /api/v1/applications/task/{taskId}`.
- Buyer accepts one seller with `POST /api/v1/applications/{applicationId}/accept`.

### 3. Seller Flow
Collection sections: `Tasks`, `Applications`

- Seller can browse tasks with `GET /api/v1/tasks`.
- Seller applies to task with `POST /api/v1/applications`.
- Seller sees own application statuses with `GET /api/v1/applications/mine`.

### 4. Admin Flow
Collection section: `Admin Users`

- Admin lists users with `GET /api/v1/admin/users`.
- Admin blocks/unblocks users using `PATCH` endpoints.
- Login-blocked-user test validates blocked users cannot authenticate.

## RESTfulness (How It Is Maintained)

### Resource-based URLs
- `/api/v1/auth/*`
- `/api/v1/tasks/*`
- `/api/v1/applications/*`
- `/api/v1/admin/users/*`

### Proper HTTP Methods
- `POST`: create actions (register, login, create task, apply, accept)
- `GET`: read data (tasks, applications, users)
- `PUT`: update a full resource shape for task updates
- `PATCH`: partial update for block/unblock
- `DELETE`: remove resource

### Status Code Usage
- `201 Created` for successful creation
- `200 OK` for successful reads/actions
- `204 No Content` for successful delete operations
- `400/401/403` for validation/auth/authorization failures

### Stateless Security
- Every protected request sends JWT in `Authorization` header.
- Server does not keep session state.

## Role-based Test Coverage in Collection
- Positive paths:
  - Buyer create/update task
  - Seller apply to task
  - Buyer accept application
  - Admin list/block/unblock users
- Negative paths:
  - Admin self-registration should fail
  - Seller create-task should fail (403)
  - Buyer apply-to-task should fail (403)
  - Blocked seller login should fail (401/403)

## Notes
- If users already exist, register requests may return `400`; this is expected and login requests should still work.
- Make sure `adminEmail` and `adminPassword` match your configured admin bootstrap credentials.
- Run collection in order so dynamic variables (`taskId`, `applicationId`, `sellerUserId`) are populated automatically.
