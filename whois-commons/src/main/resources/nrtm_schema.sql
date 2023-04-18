/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
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
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `notification_file`;
DROP TABLE IF EXISTS `delta_file`;
DROP TABLE IF EXISTS `snapshot_file`;
DROP TABLE IF EXISTS `snapshot_object`;
DROP TABLE IF EXISTS `version_info`;
DROP TABLE IF EXISTS `source`;

CREATE TABLE `source`
(
    `id`   int unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(40)  NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `source__name_uk` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `version_info`
(
    `id`             int unsigned    NOT NULL AUTO_INCREMENT,
    `source_id`      int unsigned    NOT NULL,
    `version`        int unsigned    NOT NULL,
    `session_id`     varchar(128)    NOT NULL,
    `type`           varchar(128)    NOT NULL,
    `last_serial_id` int             NOT NULL,
    `created`        bigint unsigned NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `snapshot_file`
(
    `id`         int unsigned NOT NULL AUTO_INCREMENT,
    `version_id` int unsigned NOT NULL,
    `name`       varchar(128) NOT NULL,
    `hash`       varchar(64)  NOT NULL,
    `payload`    longblob     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `snapshot_file__version_id__uk` (`version_id`),
    UNIQUE KEY `snapshot_file__name__uk` (`name`),
    CONSTRAINT `snapshot_file__version_id__fk` FOREIGN KEY (`version_id`) REFERENCES `version_info` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `delta_file`
(
    `id`         int unsigned NOT NULL AUTO_INCREMENT,
    `version_id` int unsigned NOT NULL,
    `name`       varchar(128) NOT NULL,
    `hash`       varchar(64)  NOT NULL,
    `payload`    longtext     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `delta_file__version_id__uk` (`version_id`),
    UNIQUE KEY `delta_file__name__uk` (`name`),
    CONSTRAINT `delta_file__version_id__fk` FOREIGN KEY (`version_id`) REFERENCES `version_info` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `notification_file`
(
    `id`         int unsigned    NOT NULL AUTO_INCREMENT,
    `version_id` int unsigned    NOT NULL,
    `created`    bigint unsigned NOT NULL,
    `payload`    longtext        NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `notification_file__version_id__fk` FOREIGN KEY (`version_id`) REFERENCES `version_info` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;
