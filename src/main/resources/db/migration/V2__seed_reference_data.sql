-- =====================================================================
-- V2 - Reference / master data required for the application to function.
-- This is NOT sample ticket data. Safe to run in every environment.
-- =====================================================================

INSERT INTO roles (role_code, role_name, description) VALUES
 ('TEAM_LEAD', 'Team Lead', 'Full administrative access across the Activation Education EForms Team'),
 ('TECHNICAL_LEAD', 'Technical Lead', 'Technical governance, assignment and readiness approval'),
 ('DASHBOARD_HANDLER', 'Dashboard Handler', 'Ticket import, assignment, monitoring and reporting'),
 ('DEVELOPER', 'Developer', 'Access restricted to personally assigned tickets'),
 ('SYSTEM_ADMIN', 'System Administrator', 'Reserved for future platform administration');

INSERT INTO team_master (team_code, team_name, team_type) VALUES
 ('AE_EFORMS', 'Activation Education EForms Team', 'BOTH'),
 ('FRAMEWORK', 'Framework Team', 'DEPENDENCY'),
 ('DBA', 'Database Administration Team', 'DEPENDENCY'),
 ('UAT_TEAM', 'UAT Team', 'DEPENDENCY'),
 ('QA_TEAM', 'QA Team', 'DEPENDENCY'),
 ('INFRA', 'Infrastructure Team', 'DEPENDENCY'),
 ('CRM_INTEGRATION', 'CRM Integration Team', 'DEPENDENCY');

INSERT INTO ticket_type_master (type_code, type_name) VALUES
 ('BUG', 'Bug'), ('ENHANCEMENT', 'Enhancement'), ('NEW_REQUIREMENT', 'New Requirement'),
 ('CONFIG_CHANGE', 'Configuration Change'), ('PRODUCTION_SUPPORT', 'Production Support'),
 ('DATA_FIX', 'Data Fix'), ('CLARIFICATION', 'Clarification');

INSERT INTO priority_master (priority_code, priority_name, rank_order) VALUES
 ('P1_CRITICAL', 'P1 - Critical', 1),
 ('P2_HIGH', 'P2 - High', 2),
 ('P3_MEDIUM', 'P3 - Medium', 3),
 ('P4_LOW', 'P4 - Low', 4);

INSERT INTO severity_master (severity_code, severity_name, rank_order) VALUES
 ('SEV1_BLOCKER', 'Sev-1 Blocker', 1),
 ('SEV2_MAJOR', 'Sev-2 Major', 2),
 ('SEV3_MINOR', 'Sev-3 Minor', 3),
 ('SEV4_COSMETIC', 'Sev-4 Cosmetic', 4);

INSERT INTO status_master (status_code, display_name, is_terminal, is_open, sort_order) VALUES
 ('NEW', 'New', 0, 1, 10),
 ('ASSIGNED', 'Assigned', 0, 1, 20),
 ('WORK_IN_PROGRESS', 'Work In Progress', 0, 1, 30),
 ('UAT_IN_PROGRESS', 'UAT In Progress', 0, 1, 40),
 ('QA_IN_PROGRESS', 'QA In Progress', 0, 1, 50),
 ('REASSIGNED_TO_FRAMEWORK_TEAM', 'Reassigned To Framework Team', 0, 1, 60),
 ('REASSIGNED_TO_OTHER_TEAM', 'Reassigned To Other Team', 0, 1, 70),
 ('ON_HOLD', 'On Hold', 0, 1, 80),
 ('REOPENED', 'Reopened', 0, 1, 90),
 ('MOVED_TO_PRODUCTION', 'Moved To Production', 1, 0, 100),
 ('CLOSED', 'Closed', 1, 0, 110),
 ('CANCELLED', 'Cancelled', 1, 0, 120);

INSERT INTO activity_type_master (type_code, type_name) VALUES
 ('FRONTEND_CHANGES', 'Frontend Changes'),
 ('BACKEND_CHANGES', 'Backend Changes'),
 ('DATABASE_CHANGES', 'Database Changes'),
 ('BUG_FIX', 'Bug Fix'),
 ('ENHANCEMENT', 'Enhancement'),
 ('CONFIGURATION', 'Configuration'),
 ('DEPLOYMENT', 'Deployment'),
 ('PRODUCTION_SUPPORT', 'Production Support'),
 ('ANALYSIS', 'Analysis'),
 ('CODE_REVIEW', 'Code Review'),
 ('TESTING_SUPPORT', 'Testing Support'),
 ('CUSTOMER_CLARIFICATION', 'Customer Clarification'),
 ('FRAMEWORK_DEPENDENCY', 'Framework Dependency'),
 ('OTHER', 'Other');

INSERT INTO aging_threshold_config (bucket_code, min_days, max_days, color_code, label) VALUES
 ('HEALTHY', 0, 3, 'success', 'Healthy'),
 ('ATTENTION', 4, 7, 'warning', 'Needs Attention'),
 ('AT_RISK', 8, 15, 'orange', 'At Risk'),
 ('CRITICAL', 16, NULL, 'danger', 'Critical');

INSERT INTO sla_threshold_config (config_key, config_value, description) VALUES
 ('SLA_LOW_RISK_MAX', '40', 'Upper bound of Low Risk band'),
 ('SLA_MEDIUM_RISK_MAX', '70', 'Upper bound of Medium Risk band'),
 ('SLA_NO_ACTIVITY_ALERT_DAYS', '3', 'Days without activity before risk escalation'),
 ('SLA_APPROACHING_PRODUCTION_DAYS', '2', 'Days before estimated production date considered near'),
 ('SLA_SAME_STATUS_ALERT_DAYS', '5', 'Days in same status before flag'),
 ('AGING_ESCALATION_REVIEW_DAYS', '8', 'Aging days triggering "Needs review" escalation'),
 ('AGING_ESCALATION_CRITICAL_DAYS', '15', 'Aging days triggering "Critical" escalation');

INSERT INTO risk_weight_config (factor_code, factor_label, weight) VALUES
 ('AGING', 'Ticket Aging', 15),
 ('PRIORITY', 'Priority', 10),
 ('SEVERITY', 'Severity', 10),
 ('DAYS_TO_PRODUCTION', 'Days Remaining Until Expected Production Date', 12),
 ('DAYS_SINCE_ACTIVITY', 'Days Since Last Activity', 12),
 ('TIME_IN_STATUS', 'Time Spent In Current Status', 8),
 ('ON_HOLD', 'On Hold State', 8),
 ('FRAMEWORK_DEPENDENCY', 'Framework Dependency', 6),
 ('OTHER_TEAM_DEPENDENCY', 'Other-Team Dependency', 5),
 ('REASSIGNMENT_COUNT', 'Number Of Reassignments', 6),
 ('UAT_PENDING', 'UAT Pending Duration', 4),
 ('QA_PENDING', 'QA Pending Duration', 4),
 ('MISSING_ESTIMATED_DATE', 'Missing Estimated Production Date', 5),
 ('EXISTING_BREACH', 'Existing SLA Breach', 15);

INSERT INTO deployment_environment_master (env_code, env_name, sort_order) VALUES
 ('DEV', 'Development', 10), ('UAT', 'UAT', 20), ('QA', 'QA', 30),
 ('STAGING', 'Staging', 40), ('PRODUCTION', 'Production', 50);

INSERT INTO attachment_category_master (category_code, category_name) VALUES
 ('REQUIREMENT_DOC', 'Requirement Document'), ('SCREENSHOT', 'Screenshot'),
 ('TEST_EVIDENCE', 'Test Evidence'), ('SQL_SCRIPT', 'SQL Script'),
 ('DEPLOYMENT_EVIDENCE', 'Deployment Evidence'), ('OTHER', 'Other');

INSERT INTO report_setting_config (setting_key, setting_value, description) VALUES
 ('EXCEL_HEADER_FREEZE', 'true', 'Freeze first row on Excel exports'),
 ('DATE_FORMAT', 'dd-MMM-yyyy', 'Standard date format used across reports'),
 ('DEFAULT_TIMEZONE', 'Asia/Kolkata', 'Reporting timezone');
