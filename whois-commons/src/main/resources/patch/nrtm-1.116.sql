-- drop public_key column in in kay_pair table

ALTER TABLE key_pair DROP COLUMN public_key;

TRUNCATE version;
INSERT INTO version VALUES ('nrtm-1.116');

