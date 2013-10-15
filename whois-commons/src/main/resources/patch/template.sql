--
-- Description:       < TODO: add a description of what the patch does >
--
-- Issue:             < TODO: add GitHub issue # >
--
-- Release Version:   < TODO: version >
--

USE WHOIS_UPDATE_RIPE;

DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;
DELIMITER //
CREATE PROCEDURE WHOIS_PATCH_PROCEDURE()
BEGIN
START TRANSACTION;

-- < TODO: insert data patch here>

-- < TODO: update version table>

UPDATE version SET version = 'whois-x.y.z';

COMMIT;
END//

CALL WHOIS_PATCH_PROCEDURE();
DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;

