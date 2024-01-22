-- Convert Whois schema to UTF8
--
-- TODO: [ES] how to track character set in object BLOB column in history and last tables?
--      Do we add a new 'utf8' tinyint / boolean column, or store full character set name?
--      Do we store *both* a UTF8 version *and* a latin-1 transliteration?

-- TODO: [ES] update database and client defaults
--      [client]
--      default-character-set=utf8mb4
--      [mysql]
--      default-character-set=utf8mb4
--      [mysqld]
--      collation-server = utf8mb4_unicode_ci
--      init-connect='SET NAMES utf8mb4'
--      character-set-server = utf8mb4
--

-- TODO: [ES] do we run this script separately on the master and slave(s) ?

-- TODO: [ES] how much temporary disk space is needed for the migration?

-- TODO: [ES] how much time is needed for the migration?
--      eg. history table = 52 mins, last table = 17 mins (tested on metal server)
--          Entire script ran in 82m12.701s on a standalone metal server

-- TODO: Also convert mirror databases, TEST, mailupdates etc.

SET autocommit = 0;
START TRANSACTION;

-- DATABASE
alter database WHOIS_UPDATE_RIPE character set = 'utf8mb4' collate = 'utf8mb4_general_ci';

-- TABLES
ALTER TABLE abuse_c CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE abuse_mailbox CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE admin_c CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE as_block CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE as_set CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE aut_num CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE auth CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE author CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE domain CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE ds_rdata CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE e_mail CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE filter_set CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE fingerpr CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE form CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE history CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE ifaddr CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE inaddr_arpa CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE inet6num CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE inet_rtr CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE inetnum CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE interface CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE irt CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE irt_nfy CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE key_cert CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE last CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mbrs_by_ref CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE member_of CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_by CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_domains CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_irt CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_lower CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_nfy CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_ref CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mnt_routes CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE mntner CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE names CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE nic_hdl CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE notify CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE nserver CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE org CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE org_name CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE organisation CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE organisation_id CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE peering_set CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE person_role CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE ping_hdl CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE poem CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE poetic_form CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE ref_nfy CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE route CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE route6 CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE route_set CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE rtr_set CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE serials CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE sponsoring_org CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE status CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE tech_c CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE transfer_update_lock CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE upd_to CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE update_lock CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE version CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE x509 CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';
ALTER TABLE zone_c CONVERT TO CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_general_ci';

COMMIT;
