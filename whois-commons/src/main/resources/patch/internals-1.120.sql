DROP TABLE IF EXISTS `keycloak_apikey_details`;

CREATE TABLE `keycloak_apikey_details` (
        `apikeyId` varchar(320) NOT NULL,
        `expiresAt` datetime NOT NULL,
        PRIMARY KEY (`apikeyId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.120');