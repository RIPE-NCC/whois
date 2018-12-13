ALTER TABLE abuse_org_email ADD object_type tinyint(3) UNSIGNED NULL;
ALTER TABLE abuse_org_email ADD object_pkey varchar(254) NULL;
ALTER TABLE abuse_org_email ADD abuse_nic_hdl varchar(30) NULL;

UPDATE abuse_org_email set object_type=18;
UPDATE abuse_org_email set object_pkey=org_id;
UPDATE abuse_org_email set abuse_nic_hdl='DUMMY-RIPE';

ALTER TABLE abuse_org_email CHANGE COLUMN object_type object_type tinyint(3) UNSIGNED NOT NULL;
ALTER TABLE abuse_org_email CHANGE COLUMN object_pkey object_pkey varchar(254) NOT NULL;
ALTER TABLE abuse_org_email CHANGE COLUMN abuse_nic_hdl abuse_nic_hdl varchar(30) NOT NULL;

ALTER TABLE abuse_org_email DROP PRIMARY KEY;

ALTER TABLE abuse_org_email ADD id int(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92.13');
