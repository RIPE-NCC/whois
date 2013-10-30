--
-- Description:       Remove dummy values from database
--
-- Issue:             N/A
--
-- Release Version:   1.70.2
--

USE WHOIS_UPDATE_RIPE;

DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;
DELIMITER //
CREATE PROCEDURE WHOIS_PATCH_PROCEDURE()
BEGIN
START TRANSACTION;

-- as_set

UPDATE last l
  INNER JOIN as_set a ON l.object_id = a.object_id
  SET l.sequence_id = 0
  WHERE a.dummy = 1;
DELETE FROM as_set WHERE dummy = 1;
ALTER TABLE as_set DROP COLUMN dummy;

-- irt

ALTER TABLE irt DROP COLUMN dummy;

-- mntner

UPDATE last l
  INNER JOIN mntner m ON l.object_id = m.object_id
  SET l.sequence_id = 0
  WHERE m.dummy = 1;
DELETE FROM mntner WHERE dummy = 1;
ALTER TABLE mntner DROP COLUMN dummy;

-- organisation

ALTER TABLE organisation DROP COLUMN dummy;

-- person_role

UPDATE last l
  INNER JOIN person_role pr ON l.object_id = pr.object_id
  SET l.sequence_id = 0
  WHERE pr.dummy = 1;
DELETE FROM person_role WHERE dummy = 1;
ALTER TABLE person_role DROP COLUMN dummy;

-- route

ALTER TABLE route DROP COLUMN dummy;

-- route6

ALTER TABLE route6 DROP COLUMN dummy;

-- route_set

UPDATE last l
  INNER JOIN route_set rs ON l.object_id = rs.object_id
  SET l.sequence_id = 0
  WHERE rs.dummy = 1;
DELETE FROM route_set WHERE dummy = 1;
ALTER TABLE route_set DROP COLUMN dummy;

-- rtr_set

ALTER TABLE rtr_set DROP COLUMN dummy;

-- last

DELETE FROM last WHERE object_type = 100;


INSERT INTO version VALUES ('whois-1.70-2');

COMMIT;
END//

CALL WHOIS_PATCH_PROCEDURE();
DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;
