-- Convert Versions schema to UTF8
--

-- TODO: [ES] How long to run in production?
--          Took 66 mins on a standalone metal server

-- TODO: how much extra disk space is needed during the conversion?

SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database VERSIONS_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE object_reference CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE object_version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE serials CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
