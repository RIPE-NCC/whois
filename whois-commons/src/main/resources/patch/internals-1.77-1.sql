ALTER TABLE forgot_password_audit_log ADD user_sso_email VARCHAR(256) DEFAULT null;
INSERT INTO version VALUES ('internals-1.77-1');
