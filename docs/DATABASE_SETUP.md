# Database Setup Instructions

## 1. Create the database and application user (MySQL 8)

```sql
CREATE DATABASE eforms_dashboard_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'eforms_dev'@'%' IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';
GRANT ALL PRIVILEGES ON eforms_dashboard_dev.* TO 'eforms_dev'@'%';
FLUSH PRIVILEGES;
```

For production, create a dedicated, least-privilege user instead of reusing
the dev user, and never grant broader privileges than the app needs.

## 2. Migrations (Flyway)

Migrations run automatically on application startup:
- `V1__init_schema.sql` — full table/index/FK creation
- `V2__seed_reference_data.sql` — roles, statuses, priorities, severities,
  activity types, aging/SLA/risk configuration, teams, environments,
  attachment categories. This is **reference data**, safe in every
  environment (dev, test, prod).

No ticket, customer, or user data is seeded via SQL — see
`AdminBootstrapSeeder` (Java, always runs, idempotent, no hardcoded
passwords) and `DevSampleDataSeeder` (Java, `dev` profile only, clearly
labelled synthetic data).

To add a new migration, create `V3__<description>.sql` in
`src/main/resources/db/migration/` — Flyway will apply it exactly once, in
order, and record it in the `flyway_schema_history` table.

## 3. Connection configuration

Set via environment variables (see `application-dev.yml` / `application-prod.yml`):

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:mysql://host:3306/db?useSSL=false&serverTimezone=Asia/Kolkata` |
| `DB_USER` / `DB_PASSWORD` | Credentials — never hardcoded in source |
| `DB_POOL_SIZE` | HikariCP max pool size (prod default 20) |

## 4. Verifying the schema

```sql
SHOW TABLES;
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```
