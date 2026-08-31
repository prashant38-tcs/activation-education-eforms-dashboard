# Activation Education EForms — Ticket Lifecycle and Delivery Management Dashboard

A production-ready, database-backed Spring Boot application for managing the
complete lifecycle of Activation Education EForms tickets — from allocation
through development, technical change tracking, UAT/QA, deployment, and
movement to production — with authentication, role-based authorization,
Excel import, aging, SLA risk scoring, automatic WSR generation, dashboards,
reporting, notifications, and a full audit trail.

> This is a **working application**, not a static mock-up: every dashboard
> card, button, and form is wired to a real controller → service →
> repository → MySQL database chain.

---

## 1. Technology Stack

| Layer          | Technology |
|----------------|------------|
| Frontend       | HTML5, CSS3, Bootstrap 5, vanilla JS + jQuery (DataTables only), Thymeleaf, Chart.js |
| Backend        | Java 8, Spring Boot 2.7, Spring MVC, Spring Security, Spring Data JPA / Hibernate |
| Database       | MySQL 8 (H2 for automated tests) |
| Auth           | Spring Security, BCrypt, session-based auth, RBAC |
| Excel          | Apache POI |
| PDF export     | OpenPDF (open-source iText fork) |
| Migrations     | Flyway |
| Build          | Maven |

No paid libraries or cloud-specific services are used anywhere.

---

## 2. Project Structure

```
eforms-app/
├── pom.xml
├── src/main/java/com/tcsion/eforms/
│   ├── EformsDashboardApplication.java
│   ├── config/          SecurityConfig, JpaAuditingConfig, AdminBootstrapSeeder,
│   │                    DevSampleDataSeeder, ScheduledJobs
│   ├── security/        CustomUserDetails, UserDetailsServiceImpl, LoginAttemptService,
│   │                    TicketAccessGuard (object-level authZ), auth handlers
│   ├── entity/          32 JPA entities (see docs/DATABASE_DESIGN.md)
│   ├── repository/      31 Spring Data JPA repositories
│   ├── dto/request      Request DTOs (never expose entities over APIs)
│   ├── dto/response     ApiResponse<T> envelope
│   ├── workflow/        WorkflowTransitionConfig + TicketWorkflowService (status state-machine)
│   ├── service/         TicketService, ExcelImportService, AgingService, SlaRiskService,
│   │   └─ impl/         WsrService, NotificationService, ReportService, FileStorageService,
│   │                    UserManagementService, AuditService (+ implementations)
│   ├── controller/      16 MVC controllers covering all required pages
│   └── exception/       GlobalExceptionHandler + typed exceptions
├── src/main/resources/
│   ├── application*.yml (default / dev / prod / test profiles)
│   ├── db/migration/    V1__init_schema.sql, V2__seed_reference_data.sql (Flyway)
│   ├── templates/       35 Thymeleaf pages (fragments/layout.html is the shared shell)
│   └── static/css|js    app.css, app.js
├── src/test/java/...    Unit tests (workflow, security, aging, duplicate-ticket rules)
└── docs/                Role matrix, ERD notes, API notes, setup/deploy/backup guides
```

---

## 3. Roles & Initial Users

See **docs/ROLE_MATRIX.md** for the full permission matrix. Initial named
accounts are created automatically on first startup by `AdminBootstrapSeeder`
— **not** via a hardcoded SQL password:

| Username               | Full Name             | Role               |
|------------------------|------------------------|--------------------|
| `sanjay.singh`          | Sanjay Singh            | TEAM_LEAD |
| `prashant.chaturvedi`   | Prashant Chaturvedi     | TECHNICAL_LEAD |
| `pooja.gehlod`          | Pooja Gehlod            | TECHNICAL_LEAD |
| `mayurika.srivastava`   | Mayurika Srivastava     | DASHBOARD_HANDLER |

Each account is created with a **one-time, cryptographically-random 16
character password** printed once to the application log at `WARN` level on
first boot (never committed to source, never a "well-known" default). The
account is flagged `force_password_change = true`, so the user is required
to set their own password immediately on first login. To control the
password explicitly instead (e.g. in a CI/CD pipeline), set an environment
variable before first boot, e.g.:

```
SEED_SANJAY_SINGH_PASSWORD=SomeStrongPassword!23
```

Developers are created afterwards through **User Management** (Team Lead /
System Admin only) — each gets a random temporary password shown once on
screen, again forcing a password change on first login.

---

## 4. Running Locally

### Prerequisites
- JDK 8+
- Maven 3.6+
- MySQL 8 (or use the `test` profile with in-memory H2 for a quick look)

### Database setup
```sql
CREATE DATABASE eforms_dashboard_dev CHARACTER SET utf8mb4;
CREATE USER 'eforms_dev'@'%' IDENTIFIED BY 'change_me_dev';
GRANT ALL PRIVILEGES ON eforms_dashboard_dev.* TO 'eforms_dev'@'%';
```
(See **docs/DATABASE_SETUP.md** for full instructions, including production
hardening.)

### Run
```bash
export DB_URL=jdbc:mysql://localhost:3306/eforms_dashboard_dev?useSSL=false&serverTimezone=Asia/Kolkata
export DB_USER=eforms_dev
export DB_PASSWORD=change_me_dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
The app starts on `http://localhost:8080`. Watch the console for the
one-time bootstrap passwords on first run.

To also load **synthetic, clearly-labelled** sample tickets spanning every
status (for demoing dashboards), the `dev` profile already sets
`app.sample-data.enabled=true` — this never runs in the `prod` profile.

### Tests
```bash
mvn test
```
Runs against the in-memory H2 database (`test` profile) — no external
dependency required.

---

## 5. Deployment

See **docs/DEPLOYMENT.md** for Docker + Linux systemd instructions. In
short:
```bash
mvn -Pprod clean package -DskipTests
docker build -t eforms-dashboard:1.0.0 .
docker run -e APP_PROFILE=prod -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... \
           -p 8080:8080 eforms-dashboard:1.0.0
```
All secrets are environment-based; nothing sensitive is baked into the
image or the source tree.

---

## 6. Key Design Notes

- **Object-level authorization**: `TicketAccessGuard` is invoked from inside
  `TicketService` for *every* single-ticket read/write — not just at the URL
  level — so a developer can never retrieve another developer's ticket by
  editing the URL or POST body, regardless of what the UI hides.
- **Workflow engine**: `WorkflowTransitionConfig` centralises the allowed
  status state-machine; `TicketWorkflowService` enforces both transition
  legality and the field-level mandatory rules before any status change is
  persisted.
- **SLA Risk engine** is explicitly rule-based and explainable
  (`SlaRiskService.explainRisk`) — it is **not** presented as AI/ML.
- **WSR** is generated automatically the instant a developer submits an
  activity update (`WsrService.recordFromActivity`), never hand-typed.
- **Excel import** is a strict two-phase pipeline (preview → confirm) and
  never overwrites developer-entered remarks/technical details with blank
  Excel values.
- **Audit log** and **ticket activity history** are append-only; no
  controller or service exposes an update/delete path for them.

---

## 7. Further Documentation

- `docs/ROLE_MATRIX.md` — full role-permission matrix
- `docs/DATABASE_DESIGN.md` — ERD notes and table relationships
- `docs/API_NOTES.md` — REST/JSON endpoints exposed for AJAX use
- `docs/DATABASE_SETUP.md` — schema creation & Flyway details
- `docs/DEPLOYMENT.md` — Docker/Linux deployment guide
- `docs/BACKUP_RESTORE.md` — backup/restore guidance
- `docs/IMPLEMENTATION_STATUS.md` — an honest map of what is fully
  implemented vs. what is scaffolded and needs a follow-up iteration
