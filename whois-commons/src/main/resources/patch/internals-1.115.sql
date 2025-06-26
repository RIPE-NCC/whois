--
-- support schema conversion to utf8mb4
--
ALTER TABLE version ADD PRIMARY KEY (version);

--
-- increase width of columns containing an email address to the maximum allowed by RFC 5321.
--
ALTER TABLE email_links MODIFY COLUMN email varchar(320) NOT NULL;
ALTER TABLE forgot_password_audit_log MODIFY COLUMN email varchar(320) DEFAULT NULL;
ALTER TABLE default_maintainer_sync MODIFY COLUMN email varchar(320) NOT NULL;
ALTER TABLE abuse_email MODIFY COLUMN address varchar(320) NOT NULL;
ALTER TABLE outgoing_message MODIFY COLUMN email varchar(320) NOT NULL;
ALTER TABLE email_status MODIFY COLUMN email varchar(320) NOT NULL;

--
-- We no longer need default maintainer history
--
DROP TABLE IF EXISTS `default_maintainer_history`;
DROP TABLE IF EXISTS `default_maintainer_sync_history`;

--
-- Set version to Whois 1.115 release
--

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.115');

