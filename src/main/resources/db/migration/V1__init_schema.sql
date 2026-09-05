-- =====================================================================
-- Activation Education EForms - Ticket Lifecycle and Delivery Dashboard
-- V1 - Core schema
-- =====================================================================

CREATE TABLE roles (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code       VARCHAR(40)  NOT NULL UNIQUE,
    role_name       VARCHAR(80)  NOT NULL,
    description     VARCHAR(255),
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code               VARCHAR(40)  UNIQUE,
    username                    VARCHAR(80)  NOT NULL UNIQUE,
    full_name                   VARCHAR(120) NOT NULL,
    email                       VARCHAR(150) UNIQUE,
    password_hash               VARCHAR(255) NOT NULL,
    active                      TINYINT(1)   NOT NULL DEFAULT 1,
    force_password_change       TINYINT(1)   NOT NULL DEFAULT 1,
    failed_login_attempts       INT          NOT NULL DEFAULT 0,
    account_locked              TINYINT(1)   NOT NULL DEFAULT 0,
    lock_time                   DATETIME     NULL,
    last_login_at               DATETIME     NULL,
    last_login_ip               VARCHAR(64)  NULL,
    password_changed_at         DATETIME     NULL,
    created_by                  VARCHAR(80),
    updated_by                  VARCHAR(80),
    created_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version                     BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_users_active ON users(active);

CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token       VARCHAR(150) NOT NULL UNIQUE,
    expiry_at   DATETIME NOT NULL,
    used        TINYINT(1) NOT NULL DEFAULT 0,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE team_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_code VARCHAR(60) NOT NULL UNIQUE,
    team_name VARCHAR(120) NOT NULL,
    team_type VARCHAR(30) NOT NULL DEFAULT 'DEPENDENCY',
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE customer_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(60) UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    customer_category VARCHAR(60),
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_name ON customer_master(customer_name);

CREATE TABLE ticket_type_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(60) NOT NULL UNIQUE,
    type_name VARCHAR(120) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE priority_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    priority_code VARCHAR(30) NOT NULL UNIQUE,
    priority_name VARCHAR(60) NOT NULL,
    rank_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE severity_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    severity_code VARCHAR(30) NOT NULL UNIQUE,
    severity_name VARCHAR(60) NOT NULL,
    rank_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE status_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    status_code VARCHAR(60) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    is_terminal TINYINT(1) NOT NULL DEFAULT 0,
    is_open TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE activity_type_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(60) NOT NULL UNIQUE,
    type_name VARCHAR(120) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE aging_threshold_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bucket_code VARCHAR(30) NOT NULL UNIQUE,
    min_days INT NOT NULL,
    max_days INT NULL,
    color_code VARCHAR(20) NOT NULL,
    label VARCHAR(60) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE sla_threshold_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(80) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE risk_weight_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    factor_code VARCHAR(80) NOT NULL UNIQUE,
    factor_label VARCHAR(150) NOT NULL,
    weight DECIMAL(5,2) NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE deployment_environment_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    env_code VARCHAR(40) NOT NULL UNIQUE,
    env_name VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE attachment_category_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(60) NOT NULL UNIQUE,
    category_name VARCHAR(120) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE report_setting_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE tickets (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number               VARCHAR(60)  NOT NULL UNIQUE,
    crm_id                      VARCHAR(60),
    customer_id                 BIGINT,
    ticket_title                VARCHAR(255) NOT NULL,
    ticket_description          TEXT,
    short_planned_milestone     VARCHAR(255),
    ticket_type_id              BIGINT,
    priority_id                 BIGINT,
    severity_id                 BIGINT,
    assigned_user_id            BIGINT,
    previous_assignee_id        BIGINT,
    assigned_by_id              BIGINT,
    assignment_date             DATETIME,
    source_team_id              BIGINT,
    dependency_team_id          BIGINT,
    created_date                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_closure_date       DATE,
    estimated_production_date  DATE,
    actual_production_date     DATE,
    last_activity_date          DATETIME,
    last_updated_date           DATETIME,
    closed_date                 DATETIME,
    current_status_id           BIGINT NOT NULL,
    current_stage               VARCHAR(60),
    sla_state                   VARCHAR(30) DEFAULT 'MET',
    sla_risk_score               DECIMAL(5,2) DEFAULT 0,
    sla_risk_category             VARCHAR(20) DEFAULT 'LOW',
    aging_days                  INT DEFAULT 0,
    on_hold                     TINYINT(1) NOT NULL DEFAULT 0,
    hold_reason                 VARCHAR(255),
    reassignment_count          INT NOT NULL DEFAULT 0,
    is_active                   TINYINT(1) NOT NULL DEFAULT 1,
    is_archived                 TINYINT(1) NOT NULL DEFAULT 0,
    created_by                  VARCHAR(80),
    updated_by                  VARCHAR(80),
    created_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version                     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ticket_customer FOREIGN KEY (customer_id) REFERENCES customer_master(id),
    CONSTRAINT fk_ticket_type FOREIGN KEY (ticket_type_id) REFERENCES ticket_type_master(id),
    CONSTRAINT fk_ticket_priority FOREIGN KEY (priority_id) REFERENCES priority_master(id),
    CONSTRAINT fk_ticket_severity FOREIGN KEY (severity_id) REFERENCES severity_master(id),
    CONSTRAINT fk_ticket_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_prev_assignee FOREIGN KEY (previous_assignee_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_assigned_by FOREIGN KEY (assigned_by_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_source_team FOREIGN KEY (source_team_id) REFERENCES team_master(id),
    CONSTRAINT fk_ticket_dependency_team FOREIGN KEY (dependency_team_id) REFERENCES team_master(id),
    CONSTRAINT fk_ticket_status FOREIGN KEY (current_status_id) REFERENCES status_master(id)
);
CREATE INDEX idx_ticket_number ON tickets(ticket_number);
CREATE INDEX idx_ticket_crm ON tickets(crm_id);
CREATE INDEX idx_ticket_assigned_user ON tickets(assigned_user_id);
CREATE INDEX idx_ticket_customer ON tickets(customer_id);
CREATE INDEX idx_ticket_status ON tickets(current_status_id);
CREATE INDEX idx_ticket_assignment_date ON tickets(assignment_date);
CREATE INDEX idx_ticket_expected_production ON tickets(estimated_production_date);
CREATE INDEX idx_ticket_actual_production ON tickets(actual_production_date);
CREATE INDEX idx_ticket_last_activity ON tickets(last_activity_date);
CREATE INDEX idx_ticket_sla_state ON tickets(sla_state);
CREATE INDEX idx_ticket_risk_category ON tickets(sla_risk_category);
CREATE INDEX idx_ticket_active ON tickets(is_active);

CREATE TABLE ticket_assignments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id       BIGINT NOT NULL,
    assigned_from_user_id BIGINT NULL,
    assigned_to_user_id   BIGINT NULL,
    assigned_to_team_id   BIGINT NULL,
    assigned_by_id  BIGINT NOT NULL,
    assignment_type VARCHAR(30) NOT NULL DEFAULT 'INITIAL',
    reason          VARCHAR(255),
    assigned_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ta_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_ta_from FOREIGN KEY (assigned_from_user_id) REFERENCES users(id),
    CONSTRAINT fk_ta_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id),
    CONSTRAINT fk_ta_team FOREIGN KEY (assigned_to_team_id) REFERENCES team_master(id),
    CONSTRAINT fk_ta_by FOREIGN KEY (assigned_by_id) REFERENCES users(id)
);
CREATE INDEX idx_ta_ticket ON ticket_assignments(ticket_id);

CREATE TABLE ticket_activities (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id               BIGINT NOT NULL,
    activity_datetime       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    previous_status_id      BIGINT,
    new_status_id           BIGINT,
    progress_percentage     INT DEFAULT 0,
    activity_type_id        BIGINT,
    work_summary            VARCHAR(500),
    detailed_remark         TEXT,
    blocker                 VARCHAR(500),
    root_cause              VARCHAR(500),
    action_taken            VARCHAR(500),
    next_action             VARCHAR(500),
    dependency              VARCHAR(255),
    estimated_completion_date DATE,
    hours_spent             DECIMAL(5,2),
    updated_by_id           BIGINT NOT NULL,
    source                  VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_act_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_act_prev_status FOREIGN KEY (previous_status_id) REFERENCES status_master(id),
    CONSTRAINT fk_act_new_status FOREIGN KEY (new_status_id) REFERENCES status_master(id),
    CONSTRAINT fk_act_type FOREIGN KEY (activity_type_id) REFERENCES activity_type_master(id),
    CONSTRAINT fk_act_user FOREIGN KEY (updated_by_id) REFERENCES users(id)
);
CREATE INDEX idx_activity_ticket ON ticket_activities(ticket_id);
CREATE INDEX idx_activity_date ON ticket_activities(activity_datetime);
CREATE INDEX idx_activity_user ON ticket_activities(updated_by_id);

CREATE TABLE ticket_comments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id       BIGINT NOT NULL,
    comment_type    VARCHAR(40) NOT NULL DEFAULT 'GENERAL',
    comment_text    TEXT NOT NULL,
    follow_up_date  DATE,
    created_by_id   BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (created_by_id) REFERENCES users(id)
);
CREATE INDEX idx_comment_ticket ON ticket_comments(ticket_id);

CREATE TABLE frontend_changes (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT NOT NULL,
    screen_form_name    VARCHAR(150),
    page_name           VARCHAR(150),
    html_file_name      VARCHAR(150),
    js_file_name        VARCHAR(150),
    css_file_name       VARCHAR(150),
    function_name       VARCHAR(150),
    ui_component        VARCHAR(150),
    validation_changed   VARCHAR(255),
    change_description  TEXT,
    review_status       VARCHAR(40) DEFAULT 'PENDING',
    deployment_status   VARCHAR(40) DEFAULT 'PENDING',
    created_by_id       BIGINT NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fe_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_fe_user FOREIGN KEY (created_by_id) REFERENCES users(id)
);
CREATE INDEX idx_fe_ticket ON frontend_changes(ticket_id);

CREATE TABLE backend_changes (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT NOT NULL,
    application_module  VARCHAR(150),
    jar_name            VARCHAR(150) NOT NULL,
    jar_version         VARCHAR(60),
    build_number        VARCHAR(60),
    pipeline_name       VARCHAR(150) NOT NULL,
    pipeline_reference  VARCHAR(255),
    package_name        VARCHAR(255),
    class_name          VARCHAR(255) NOT NULL,
    method_name         VARCHAR(255) NOT NULL,
    api_service_name    VARCHAR(255),
    change_description  TEXT,
    code_review_status  VARCHAR(40) DEFAULT 'PENDING',
    build_status        VARCHAR(40) DEFAULT 'PENDING',
    deployment_status   VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    environment         VARCHAR(30),
    deployment_date     DATETIME,
    deployment_owner_id BIGINT,
    rollback_required   TINYINT(1) DEFAULT 0,
    rollback_status     VARCHAR(40),
    rollback_remark      VARCHAR(500),
    created_by_id       BIGINT NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_be_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_be_owner FOREIGN KEY (deployment_owner_id) REFERENCES users(id),
    CONSTRAINT fk_be_user FOREIGN KEY (created_by_id) REFERENCES users(id)
);
CREATE INDEX idx_be_ticket ON backend_changes(ticket_id);
CREATE INDEX idx_be_status ON backend_changes(deployment_status);

CREATE TABLE database_changes (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id               BIGINT NOT NULL,
    database_name           VARCHAR(120),
    schema_name             VARCHAR(120),
    table_name              VARCHAR(120),
    column_name             VARCHAR(120),
    procedure_name          VARCHAR(120),
    function_name           VARCHAR(120),
    trigger_name            VARCHAR(120),
    view_name               VARCHAR(120),
    query_change_description TEXT,
    script_file_name        VARCHAR(255),
    execution_sequence      INT,
    backup_required         TINYINT(1) DEFAULT 0,
    rollback_script_available TINYINT(1) DEFAULT 0,
    data_migration_required TINYINT(1) DEFAULT 0,
    environment             VARCHAR(30),
    execution_status        VARCHAR(40) DEFAULT 'PENDING',
    executed_by_id          BIGINT,
    execution_date          DATETIME,
    validation_result       VARCHAR(255),
    dba_remark               VARCHAR(500),
    created_by_id           BIGINT NOT NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dc_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_dc_executed_by FOREIGN KEY (executed_by_id) REFERENCES users(id),
    CONSTRAINT fk_dc_user FOREIGN KEY (created_by_id) REFERENCES users(id)
);
CREATE INDEX idx_dc_ticket ON database_changes(ticket_id);

CREATE TABLE deployment_details (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT NOT NULL,
    environment         VARCHAR(30) NOT NULL,
    jar_name            VARCHAR(150),
    jar_version         VARCHAR(60),
    build_number        VARCHAR(60),
    pipeline_name       VARCHAR(150),
    pipeline_reference  VARCHAR(255),
    deployment_date     DATETIME,
    deployment_owner_id BIGINT,
    deployment_status   VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    validation_status   VARCHAR(40) DEFAULT 'PENDING',
    rollback_required   TINYINT(1) DEFAULT 0,
    rollback_status     VARCHAR(40),
    deployment_remark    VARCHAR(500),
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dd_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_dd_owner FOREIGN KEY (deployment_owner_id) REFERENCES users(id)
);
CREATE INDEX idx_dd_ticket ON deployment_details(ticket_id);
CREATE INDEX idx_dd_status ON deployment_details(deployment_status);
CREATE INDEX idx_dd_env ON deployment_details(environment);

CREATE TABLE ticket_attachments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT NOT NULL,
    category_id         BIGINT,
    original_file_name  VARCHAR(255) NOT NULL,
    stored_file_name    VARCHAR(255) NOT NULL,
    storage_path        VARCHAR(500) NOT NULL,
    content_type        VARCHAR(120),
    file_size_bytes     BIGINT,
    version_number      INT NOT NULL DEFAULT 1,
    uploaded_by_id      BIGINT NOT NULL,
    uploaded_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active              TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_att_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_att_category FOREIGN KEY (category_id) REFERENCES attachment_category_master(id),
    CONSTRAINT fk_att_user FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);
CREATE INDEX idx_att_ticket ON ticket_attachments(ticket_id);

CREATE TABLE import_batches (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_file_name  VARCHAR(255) NOT NULL,
    stored_file_name    VARCHAR(255),
    checksum            VARCHAR(128),
    uploaded_by_id      BIGINT NOT NULL,
    uploaded_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    row_count           INT DEFAULT 0,
    inserted_count      INT DEFAULT 0,
    updated_count       INT DEFAULT 0,
    rejected_count      INT DEFAULT 0,
    duplicate_count     INT DEFAULT 0,
    processing_status   VARCHAR(30) NOT NULL DEFAULT 'PENDING_PREVIEW',
    processing_result   VARCHAR(500),
    committed_at        DATETIME,
    CONSTRAINT fk_batch_user FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);
CREATE INDEX idx_batch_uploaded_at ON import_batches(uploaded_at);
CREATE INDEX idx_batch_checksum ON import_batches(checksum);

CREATE TABLE import_batch_rows (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id            BIGINT NOT NULL,
    row_number          INT NOT NULL,
    ticket_number        VARCHAR(60),
    crm_id              VARCHAR(60),
    raw_data_json       TEXT,
    row_classification  VARCHAR(30) NOT NULL,
    error_reason        VARCHAR(500),
    resulting_ticket_id BIGINT,
    processed           TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_row_batch FOREIGN KEY (batch_id) REFERENCES import_batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_row_ticket FOREIGN KEY (resulting_ticket_id) REFERENCES tickets(id)
);
CREATE INDEX idx_row_batch ON import_batch_rows(batch_id);
CREATE INDEX idx_row_classification ON import_batch_rows(row_classification);

CREATE TABLE sla_risk_history (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id           BIGINT NOT NULL,
    risk_score          DECIMAL(5,2) NOT NULL,
    risk_category       VARCHAR(20) NOT NULL,
    calculation_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    triggered_factors   VARCHAR(1000),
    recommended_action  VARCHAR(500),
    recovery_date       DATE,
    risk_override       TINYINT(1) DEFAULT 0,
    override_reason     VARCHAR(500),
    overridden_by_id    BIGINT,
    CONSTRAINT fk_risk_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_risk_user FOREIGN KEY (overridden_by_id) REFERENCES users(id)
);
CREATE INDEX idx_risk_ticket ON sla_risk_history(ticket_id);
CREATE INDEX idx_risk_category ON sla_risk_history(risk_category);

CREATE TABLE notifications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id    BIGINT NOT NULL,
    notification_type VARCHAR(60) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    related_ticket_id BIGINT,
    is_read         TINYINT(1) NOT NULL DEFAULT 0,
    read_at         DATETIME,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_ticket FOREIGN KEY (related_ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);
CREATE INDEX idx_notif_recipient ON notifications(recipient_id);
CREATE INDEX idx_notif_read ON notifications(is_read);

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,
    user_name       VARCHAR(120),
    role_snapshot   VARCHAR(120),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(80),
    entity_id       BIGINT,
    ticket_number   VARCHAR(60),
    old_value       TEXT,
    new_value       TEXT,
    ip_address      VARCHAR(64),
    user_agent      VARCHAR(255),
    source          VARCHAR(30) DEFAULT 'WEB',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_ticket ON audit_logs(ticket_number);
CREATE INDEX idx_audit_action ON audit_logs(action);

CREATE TABLE wsr_daily_snapshot (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    snapshot_date           DATE NOT NULL,
    developer_id            BIGINT NOT NULL,
    ticket_id               BIGINT NOT NULL,
    activity_id             BIGINT NOT NULL,
    customer_id             BIGINT,
    activity_type_id        BIGINT,
    previous_status_id      BIGINT,
    current_status_id       BIGINT,
    progress_percentage     INT,
    hours_spent             DECIMAL(5,2),
    remark                  VARCHAR(500),
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wsr_dev FOREIGN KEY (developer_id) REFERENCES users(id),
    CONSTRAINT fk_wsr_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    CONSTRAINT fk_wsr_activity FOREIGN KEY (activity_id) REFERENCES ticket_activities(id),
    CONSTRAINT uq_wsr_activity UNIQUE (activity_id)
);
CREATE INDEX idx_wsr_date ON wsr_daily_snapshot(snapshot_date);
CREATE INDEX idx_wsr_dev ON wsr_daily_snapshot(developer_id);
