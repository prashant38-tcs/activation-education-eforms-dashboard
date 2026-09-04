# Database Design Notes

Full DDL: `src/main/resources/db/migration/V1__init_schema.sql`
Reference/master seed data: `V2__seed_reference_data.sql`

## Entity Relationship Overview

```
users ──< user_roles >── roles
users ──< password_reset_tokens

customer_master, ticket_type_master, priority_master, severity_master,
status_master, activity_type_master, team_master,
aging_threshold_config, sla_threshold_config, risk_weight_config,
deployment_environment_master, attachment_category_master,
report_setting_config                      (all standalone master tables)

tickets
 ├─ FK customer_id            -> customer_master
 ├─ FK ticket_type_id         -> ticket_type_master
 ├─ FK priority_id            -> priority_master
 ├─ FK severity_id            -> severity_master
 ├─ FK assigned_user_id       -> users
 ├─ FK previous_assignee_id   -> users
 ├─ FK assigned_by_id         -> users
 ├─ FK source_team_id         -> team_master
 ├─ FK dependency_team_id     -> team_master
 ├─ FK current_status_id      -> status_master
 │
 ├──< ticket_assignments        (assignment/reassignment history)
 ├──< ticket_activities         (immutable activity/progress history)
 ├──< ticket_comments           (chronological remarks)
 ├──< frontend_changes
 ├──< backend_changes
 ├──< database_changes
 ├──< deployment_details
 ├──< ticket_attachments
 ├──< sla_risk_history          (one row per recalculation, with reasons)
 └──< wsr_daily_snapshot         (materialised from ticket_activities)

import_batches ──< import_batch_rows ──> tickets (resulting_ticket_id, nullable)

notifications  -> users (recipient), tickets (related_ticket, nullable)
audit_logs     -> users (nullable, for pre-auth events), free-form ticket_number
```

## Key constraints & indexes

- `tickets.ticket_number` — UNIQUE, indexed
- `tickets.crm_id` — indexed (used as secondary match key on import)
- `tickets.version` — optimistic locking column (`@Version`), surfaced to the
  UI via a hidden `expectedVersion` field on every mutation form
- `users.version` — optimistic locking for concurrent profile/role edits
- Every child table carries its own indexed FK to `tickets.id` for fast
  per-ticket history loads
- `wsr_daily_snapshot.activity_id` — UNIQUE — the idempotency guard that
  prevents a WSR row ever being created twice for the same activity

## Soft-delete / deactivation strategy

Nothing is hard-deleted from `tickets`, `users`, or any master table.
- `tickets.is_active` / `is_archived` flags
- `users.active` flag (+ `account_locked` for temporary lock)
- All `*_master` tables carry an `active` flag; `MasterDataController`
  exposes a toggle instead of a delete endpoint.
