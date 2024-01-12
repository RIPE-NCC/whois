
ALTER TABLE abuse_org_email ADD enduser_org_id varchar(256);

ALTER TABLE abuse_ticket ADD ticket_type char(1);

UPDATE abuse_ticket set ticket_type = 'O';

ALTER TABLE abuse_ticket CHANGE COLUMN ticket_type ticket_type char(1) NOT NULL;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92.14');
