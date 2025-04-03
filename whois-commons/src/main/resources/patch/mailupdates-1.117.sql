ALTER TABLE mailupdates MODIFY COLUMN changed int(10) unsigned DEFAULT (UNIX_TIMESTAMP());

TRUNCATE version;
INSERT INTO version VALUES ('mailupdates-1.117');
