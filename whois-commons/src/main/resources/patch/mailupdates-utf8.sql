-- convert MAILUPDATES schema to UTF8

SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database MAILUPDATES_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE mailupdates CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
