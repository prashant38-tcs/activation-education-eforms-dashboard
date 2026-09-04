# API Notes

The application is primarily a server-rendered (Thymeleaf) MVC app; a small
set of JSON endpoints support AJAX interactions. All JSON responses use the
consistent `ApiResponse<T>` envelope (`success`, `message`, `data`,
`errors`, `timestamp`) and **never** serialize JPA entities directly.

| Method | Path | Purpose | Auth |
|---|---|---|---|
| GET | `/tickets/api/check-ticket-number?ticketNumber=...` | Live-check ticket number uniqueness while typing | Any authenticated admin role |
| GET | `/notifications/api/unread-count` | Polled every 60s to update the notification bell badge | Any authenticated user |

## Extending to a full REST API

The layered architecture (Controller → Service → Repository, with request
DTOs already defined in `dto/request`) makes it straightforward to add a
parallel `@RestController` surface under `/api/v1/**` if external system
integration is required later — reuse the existing `TicketService`,
`ExcelImportService`, etc. and map results into new response DTOs (do not
reuse entities as response bodies). `SecurityConfig` already reserves
`/api/admin/**` for TEAM_LEAD/SYSTEM_ADMIN as a placeholder pattern for this
future extension.
