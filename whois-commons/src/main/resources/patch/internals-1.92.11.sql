ALTER TABLE abuse_email ADD checked_at_dt datetime;
UPDATE abuse_email set checked_at_dt = checked_at;
ALTER TABLE abuse_email DROP COLUMN checked_at;
ALTER TABLE abuse_email CHANGE COLUMN checked_at_dt checked_at datetime;

ALTER TABLE abuse_email ADD created_at_dt datetime;
UPDATE abuse_email set created_at_dt = created_at;
ALTER TABLE abuse_email DROP COLUMN created_at;
ALTER TABLE abuse_email CHANGE COLUMN created_at_dt created_at datetime NOT NULL;

ALTER TABLE abuse_org_email ADD deleted_at_dt datetime;
UPDATE abuse_org_email set deleted_at_dt = deleted_at;
ALTER TABLE abuse_org_email DROP COLUMN deleted_at;
ALTER TABLE abuse_org_email CHANGE COLUMN deleted_at_dt deleted_at datetime;

ALTER TABLE abuse_org_email ADD created_at_dt datetime;
UPDATE abuse_org_email set created_at_dt = created_at;
ALTER TABLE abuse_org_email DROP COLUMN created_at;
ALTER TABLE abuse_org_email CHANGE COLUMN created_at_dt created_at datetime NOT NULL;

ALTER TABLE abuse_ticket ADD created_at_dt datetime;
UPDATE abuse_ticket set created_at_dt = created_at;
ALTER TABLE abuse_ticket DROP COLUMN created_at;
ALTER TABLE abuse_ticket CHANGE COLUMN created_at_dt created_at datetime NOT NULL;

ALTER TABLE abuse_ticket ADD last_checked_dt datetime;
UPDATE abuse_ticket set last_checked_dt = last_checked;
ALTER TABLE abuse_ticket DROP COLUMN last_checked;
ALTER TABLE abuse_ticket CHANGE COLUMN last_checked_dt last_checked datetime NOT NULL;

ALTER TABLE abuse_ticket ADD last_modified_dt datetime;
UPDATE abuse_ticket set last_modified_dt = last_modified;
ALTER TABLE abuse_ticket DROP COLUMN last_modified;
ALTER TABLE abuse_ticket CHANGE COLUMN last_modified_dt last_modified datetime NOT NULL;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92.11');
