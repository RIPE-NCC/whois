--
-- support schema conversion to utf8mb4
--
ALTER TABLE version ADD PRIMARY KEY (version);

--
-- increase width of columns containing an email address to the maximum allowed by RFC 5321.
--
ALTER TABLE abuse_mailbox MODIFY COLUMN abuse_mailbox varchar(320);
ALTER TABLE e_mail MODIFY COLUMN e_mail varchar(320);
ALTER TABLE irt_nfy MODIFY COLUMN irt_nfy varchar(320);
ALTER TABLE mnt_nfy MODIFY COLUMN mnt_nfy varchar(320);
ALTER TABLE notify MODIFY COLUMN notify varchar(320);
ALTER TABLE ref_nfy MODIFY COLUMN ref_nfy varchar(320);
ALTER TABLE upd_to MODIFY COLUMN upd_to varchar(320);
--
-- Set version to Whois 1.115 release
--
TRUNCATE version;
INSERT INTO version VALUES ('whois-1.115');

