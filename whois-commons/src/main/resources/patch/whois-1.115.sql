--
-- support schema conversion to utf8mb4
--
ALTER TABLE version ADD PRIMARY KEY (version);

--
-- increase width of columns containing an email address to the maximum allowed by RFC 5321.
--
ALTER TABLE abuse_mailbox CHANGE COLUMN abuse_mailbox varchar(320) NOT NULL DEFAULT '';
ALTER TABLE e_mail CHANGE COLUMN email varchar(320) NOT NULL DEFAULT '';
ALTER TABLE irt_nfy CHANGE COLUMN irt_nfy varchar(320) NOT NULL DEFAULT '';
ALTER TABLE mnt_nfy CHANGE COLUMN mnt_nfy varchar(320) NOT NULL DEFAULT '';
ALTER TABLE notify CHANGE COLUMN notify varchar(320) NOT NULL DEFAULT '';
ALTER TABLE ref_nfy CHANGE COLUMN ref_nfy varchar(320) NOT NULL DEFAULT '';
ALTER TABLE upd_to CHANGE COLUMN upd_to varchar(320) NOT NULL DEFAULT '';

--
-- Set version to Whois 1.115 release
--
TRUNCATE version;
INSERT INTO version VALUES ('whois-1.115');

