# Implementation Status — Honest Coverage Map

This document exists so nobody has to guess what is production-ready versus
what still needs a follow-up iteration.

## ✅ Fully implemented and wired end-to-end

- Database schema (32 tables), Flyway migrations, indexes, FKs, optimistic
  locking columns, soft-delete/active flags
- Authentication: BCrypt, session-based login, failed-attempt lockout,
  forced first-login password change, secure logout, session timeout,
  last-login tracking, CSRF, security headers
- Role-based access control at URL **and** service **and** object level
  (`TicketAccessGuard` — a developer genuinely cannot open another
  developer's ticket by editing the URL)
- Ticket master CRUD, assignment/reassignment history, immutable activity
  history, comments
- Status workflow engine with centralised transition rules + mandatory
  field validation per target status
- Frontend / backend / database technical-change tracking tables and forms
- Excel import: template download, header/type/value validation, row
  classification (new/changed/unchanged/invalid/duplicate), preview,
  transactional confirm, rejected-row export, checksum-based duplicate
  detection
- Ticket Aging engine (bucketed, freezes at production date) with scheduled
  hourly recalculation
- SLA Risk engine: transparent weighted scoring, explainable "why is this
  high risk" per ticket, configurable via `risk_weight_config` /
  `sla_threshold_config`
- WSR auto-generation from every activity update (no manual re-typing)
- Notifications (in-app, extensible email hook), audit log (immutable),
  file attachment storage with type/size validation and safe on-disk naming
- Reports: Daily (carried-forward/opened/on-hold/reassigned/closed/next-day
  logic), Weekly, Quarterly, Annual — Excel/CSV/PDF export with formula-
  injection protection and audit logging of every download
- 16 controllers covering all required pages, 35 Thymeleaf templates,
  responsive Bootstrap 5 UI, Chart.js visualisations, DataTables listing
- Unit tests for the workflow engine, object-level authorization, aging
  calculation, and duplicate-ticket prevention

## ⚠️ Implemented at a solid first-pass level — recommended hardening before go-live

- **Weekly/quarterly/annual report "activity during week" and "forwarded"
  logic** uses pragmatic approximations rather than a full historical
  point-in-time snapshot engine. For strict auditor-grade historical
  accuracy, consider adding a nightly ticket-status snapshot table.
- **Report aggregation queries** load tickets into memory rather than fully
  projecting via aggregate SQL — fine at moderate ticket volumes, but should
  be revisited with dedicated aggregate/projection queries at very high
  volumes.
- **Email notifications** are stubbed at the integration point but no SMTP
  send logic is wired yet — by design, so the app never *requires* email to
  start, but this should be completed before relying on email alerts
  operationally.
- **Saved filters per user** (explicitly optional) are not implemented.
- **JWT stateless auth** dependency is included but the shipped
  configuration uses session-based auth. Enabling JWT for a stateless API
  surface is a straightforward addition on top of the existing
  `UserDetailsServiceImpl`.
- **Mapstruct** dependency is included but mapper classes were not
  generated in this pass — DTO↔entity mapping is done inline in service
  methods for clarity given the scope delivered.

## 🧪 Testing coverage note

Included tests are true unit tests (no Spring context, fast, deterministic)
covering the highest-risk logic: the workflow state machine, object-level
authorization, aging math, and duplicate-ticket prevention. Full
`@SpringBootTest`/`MockMvc` integration tests are a natural next increment
and the project is structured (clean layering, DI everywhere) to make
adding them straightforward.
