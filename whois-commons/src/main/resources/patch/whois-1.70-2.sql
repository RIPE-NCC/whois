--
-- Description:       Remove dummy values from database
--
-- Issue:             N/A
--
-- Release Version:   1.70.2
--

-- as_set

DELETE FROM as_set WHERE dummy = 1;
ALTER TABLE as_set DROP COLUMN dummy;

-- irt

DELETE FROM irt WHERE dummy = 1;
ALTER TABLE irt DROP COLUMN dummy;

-- mntner

DELETE FROM mntner WHERE dummy = 1;
ALTER TABLE mntner DROP COLUMN dummy;

-- organisation

DELETE FROM organisation WHERE dummy = 1;
ALTER TABLE organisation DROP COLUMN dummy;

-- person_role

DELETE FROM person_role WHERE dummy = 1;
ALTER TABLE person_role DROP COLUMN dummy;

-- route

DELETE FROM route WHERE dummy = 1;
ALTER TABLE route DROP COLUMN dummy;

-- route6

DELETE FROM route6 WHERE dummy = 1;
ALTER TABLE route6 DROP COLUMN dummy;

-- route_set

DELETE FROM route_set WHERE dummy = 1;
ALTER TABLE route_set DROP COLUMN dummy;

-- rtr_set

DELETE FROM rtr_set WHERE dummy = 1;
ALTER TABLE rtr_set DROP COLUMN dummy;

-- last

DELETE FROM last WHERE object_type = 100;

UPDATE version SET version = 'whois-1.70';
