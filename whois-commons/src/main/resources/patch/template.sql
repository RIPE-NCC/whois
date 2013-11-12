--
-- Description:       < TODO: add a description of what the patch does >
--
-- Issue:             < TODO: add GitHub issue # >
--
-- Release Version:   < TODO: version >
--

DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;
DELIMITER //
CREATE PROCEDURE WHOIS_PATCH_PROCEDURE()
MAIN: BEGIN

DECLARE patch_version varchar(255) DEFAULT 'whois-x.y.z';

DECLARE version_check int DEFAULT 0;
SET version_check := (SELECT COUNT(*) FROM version WHERE version = patch_version);
IF (version_check > 0) THEN
    -- patch has already been applied
    LEAVE MAIN;
END IF;

START TRANSACTION;

-- < TODO: insert data patch here>

-- < TODO: update version table>

INSERT INTO version VALUES (patch_version);

COMMIT;
END//

CALL WHOIS_PATCH_PROCEDURE();
DROP PROCEDURE IF EXISTS WHOIS_PATCH_PROCEDURE;