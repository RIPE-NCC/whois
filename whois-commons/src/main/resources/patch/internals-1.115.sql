--
-- support schema conversion to utf8mb4
--
ALTER TABLE version ADD PRIMARY KEY (version);

--
-- increase width of columns containing an email address to the maximum allowed by RFC 5321.
--
ALTER TABLE email_links CHANGE COLUMN email varchar(320) NOT NULL;
ALTER TABLE forgot_password_audit_log CHANGE COLUMN email varchar(320) DEFAULT NULL;
ALTER TABLE default_maintainer_history CHANGE COLUMN email varchar(320);
ALTER TABLE default_maintainer_sync_history CHANGE COLUMN email varchar(320) NOT NULL;
ALTER TABLE default_maintainer_sync CHANGE COLUMN email varchar(320) NOT NULL;
ALTER TABLE abuse_email CHANGE COLUMN address varchar(320) NOT NULL;
ALTER TABLE outgoing_message CHANGE COLUMN email varchar(320) NOT NULL;
ALTER TABLE email_status CHANGE COLUMN email varchar(320) NOT NULL;

--
-- Set version to Whois 1.115 release
--

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.115');

