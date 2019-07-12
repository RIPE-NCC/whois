ALTER TABLE abuse_ticket DROP COLUMN last_modified;

ALTER TABLE abuse_ticket CHANGE COLUMN status ticketia_status varchar(256) NOT NULL ;

ALTER TABLE abuse_ticket ADD state char(1) NULL;

UPDATE abuse_ticket SET state='C' WHERE ticketia_status='CLOSED';
UPDATE abuse_ticket SET state='O' WHERE ticketia_status='OPEN';

-- new tickets
UPDATE abuse_ticket SET state='N'
WHERE ticketia_status='PENDING'
AND created_at > DATE_SUB(NOW(), interval 7 day);

-- first reminder
UPDATE abuse_ticket SET state='1'
WHERE ticketia_status='PENDING'
AND created_at < DATE_SUB(NOW(), interval 7 day)
AND created_at > DATE_SUB(NOW(), interval 14 day);

-- second reminder
UPDATE abuse_ticket SET state='2'
WHERE ticketia_status='PENDING'
AND created_at < DATE_SUB(NOW(), interval 14 day);

ALTER TABLE abuse_ticket CHANGE COLUMN state state char(1) NOT NULL ;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92.12');
