-- add isActive column in in kay_pair table

ALTER TABLE key_pair ADD is_active bit(1) NOT NULL DEFAULT b'0'

--There is only one active key entry
UPDATE key_pair SET is_active = b'1';

TRUNCATE version;
INSERT INTO version VALUES ('nrtm-1.114');

