# Role-Permission Matrix

Enforced at **three** independent layers for every capability below:
1. UI (menu visibility) — `fragments/layout.html` via `sec:authorize`
2. URL / controller (`@PreAuthorize` + `SecurityConfig` matchers)
3. Service layer (`TicketAccessGuard`, explicit role checks inside service
   methods) — this is the **authoritative** layer; (1) and (2) are
   defence-in-depth only.

| Capability | TEAM_LEAD | TECHNICAL_LEAD | DASHBOARD_HANDLER | DEVELOPER | SYSTEM_ADMIN |
|---|:---:|:---:|:---:|:---:|:---:|
| View all tickets | ✅ | ✅ | ✅ | ❌ (own only) | ✅ |
| View own assigned tickets | ✅ | ✅ | ✅ | ✅ | ✅ |
| Create ticket | ✅ | ✅ | ✅ (via import/manual) | ❌ | ✅ |
| Edit ticket master fields | ✅ | ✅ | ❌ | ❌ | ✅ |
| Assign / reassign ticket | ✅ | ✅ | ✅ | ❌ | ✅ |
| Update ticket status | ✅ | ✅ | ✅ | ✅ (own ticket only) | ✅ |
| Add activity / remarks | ✅ | ✅ | ✅ | ✅ (own ticket only) | ✅ |
| Add frontend/backend/DB technical change | ✅ | ✅ | ❌ | ✅ (own ticket only) | ✅ |
| Upload attachments | ✅ | ✅ | ✅ | ✅ (own ticket only) | ✅ |
| Upload Excel & manage import | ✅ | ✅ (if authorized) | ✅ | ❌ | ✅ |
| View ticket aging dashboard | ✅ | ✅ | ✅ | ✅ (own scope) | ✅ |
| View SLA risk dashboard | ✅ | ✅ | ✅ | ✅ (own scope) | ✅ |
| View/generate Daily & Weekly WSR | ✅ (team) | ✅ (team) | ✅ (team) | ✅ (personal) | ✅ |
| Generate Daily/Weekly/Quarterly/Annual reports | ✅ | ✅ | ✅ | ❌ | ✅ |
| View Team Capacity | ✅ | ✅ | ✅ | ❌ | ✅ |
| View Deployment Tracker | ✅ | ✅ | ✅ | ✅ (read-only) | ✅ |
| View Executive Command Center | ✅ | ✅ | ✅ | ❌ | ✅ |
| Manage users (create/activate/deactivate/reset) | ✅ | ❌ | ❌ | ❌ | ✅ |
| View audit logs | ✅ | ❌ | ❌ | ❌ | ✅ |
| Manage master data | ✅ | ✅ | ✅ | ❌ | ✅ |
| Delete tickets | ❌ (none — soft-deactivate only) | ❌ | ❌ | ❌ | ❌ |
| Edit/delete audit history or activity history | ❌ (immutable for everyone) | ❌ | ❌ | ❌ | ❌ |

## Object-level rule (critical)

A `DEVELOPER` who is technically permitted to view "ticket detail" pages in
general **must never** be able to open a ticket assigned to someone else.
This is enforced in `TicketAccessGuard.assertCanView()` /
`assertCanModifyAsDeveloper()`, called from every single-ticket
`TicketService` method — independent of role-level URL matchers, and
independent of what the developer types into the browser address bar.
