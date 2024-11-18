-- add pem_format column in in kay_pair table

ALTER TABLE key_pair ADD pem_format VARCHAR(255) NULL;

TRUNCATE version;
INSERT INTO version VALUES ('nrtm-1.115');

