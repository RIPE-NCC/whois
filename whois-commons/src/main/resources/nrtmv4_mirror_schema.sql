/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

DROP TABLE IF EXISTS `version`;
CREATE TABLE `version`
(
    `version` varchar(80)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

DROP TABLE IF EXISTS `public_key`;
DROP TABLE IF EXISTS `last_notification_info`;


CREATE TABLE `public_key`
(
    `id`          int unsigned    NOT NULL AUTO_INCREMENT,
    `public_key`  VARBINARY(3000) NOT NULL,
    `next_public_key`     VARBINARY(3000),
    UNIQUE KEY `public_key_name_uk` (`public_key`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `last_update_info`
(
    `id`             int unsigned    NOT NULL AUTO_INCREMENT,
    `version`        int unsigned    NOT NULL,
    `last_delta_version` int unsigned    NOT NULL,
    `session_id`     varchar(128)    NOT NULL,
    `payload`    longblob     NOT NULL,
    `created`        bigint unsigned NOT NULL,
    UNIQUE KEY `last_notification_info__version__uk` (`version`),
    UNIQUE KEY `last_notification_info__last_delta_version__uk` (`last_delta_version`),
    UNIQUE KEY `last_notification_info__session_id__uk` (`session_id`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;