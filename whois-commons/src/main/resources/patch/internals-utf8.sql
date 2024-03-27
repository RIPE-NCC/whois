-- convert INTERNALS schema to UTF8



SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database INTERNALS_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE authoritative_resource CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE legacy_autnums CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE email_links CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE forgot_password_audit_log CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE default_maintainer_history CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE default_maintainer_sync_history CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE default_maintainer_in_progress CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE default_maintainer_sync CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE abuse_email CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE abuse_org_email CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE abuse_ticket CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE shedlock CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE non_auth_route CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE non_auth_route_unregistered_space CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE environment CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE outgoing_message CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE email_status CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
