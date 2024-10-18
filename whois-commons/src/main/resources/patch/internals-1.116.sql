ALTER TABLE email_status ADD COLUMN message longblob;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.116');

