-- convert NRTM schema to UTF8

SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database NRTM_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE source CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE version_info CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE snapshot_file CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE delta_file CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE notification_file CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE key_pair CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
