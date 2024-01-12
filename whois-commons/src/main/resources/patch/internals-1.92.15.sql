ALTER TABLE abuse_email ADD link_sent_at datetime;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92.15');
