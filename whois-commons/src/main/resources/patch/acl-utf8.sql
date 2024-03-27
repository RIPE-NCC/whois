-- convert ACL schema to UTF8


SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database ACL_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE acl_denied CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE acl_event CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE acl_sso_denied CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE acl_sso_event CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE acl_limit CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE acl_proxy CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE override_users CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE apikeys CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
