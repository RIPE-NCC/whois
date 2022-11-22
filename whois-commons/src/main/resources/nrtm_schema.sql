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

DROP TABLE IF EXISTS `source`;
create table `source`
(
    `id`   int unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(40)  NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `source__name_uk` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `version_information`;
create table `version_information`
(
    `id`         int unsigned NOT NULL AUTO_INCREMENT,
    `source_id`  int unsigned NOT NULL,
    `version`    int unsigned NOT NULL DEFAULT '0',
    `session_id` varchar(128) NOT NULL DEFAULT '',
    `type`       varchar(128) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `version_information__source_version_uk` (`source_id`, `version`),
    UNIQUE KEY `version_information__session_version_uk` (`session_id`, `version`),
    CONSTRAINT `version_information__source_fk` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `snapshot_file`;
create table `snapshot_file`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `version_id` int(10) unsigned NOT NULL DEFAULT '0',
    `name`       varchar(256)     NOT NULL DEFAULT '',
    `hash`       varchar(128)     NOT NULL DEFAULT '',
    `created`    bigint unsigned  not null default unix_timestamp(),
    PRIMARY KEY (`id`),
    CONSTRAINT `snapshot_file__version_information_fk` FOREIGN KEY (`version_id`) REFERENCES `version_information` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `delta_file`;
create table `delta_file`
(
    `id`             int unsigned    NOT NULL AUTO_INCREMENT,
    `version_id`     int unsigned    NOT NULL DEFAULT '0',
    `name`           varchar(256)    NOT NULL DEFAULT '',
    `payload`        longblob        NOT NULL DEFAULT '',
    `hash`           varchar(128)    NOT NULL DEFAULT '',
    `last_serial_id` int             NOT NULL DEFAULT '0',
    `created`        bigint unsigned not null default unix_timestamp(),
    PRIMARY KEY (`id`),
    CONSTRAINT `delta_file__version_information_fk` FOREIGN KEY (`version_id`) REFERENCES `version_information` (`id`),
    UNIQUE KEY `delta_file__last_serial_id_uk` (last_serial_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `snapshot_object`;
create table `snapshot_object`
(
    `id`         int unsigned NOT NULL AUTO_INCREMENT,
    `whois_pkey` varchar(256) NOT NULL DEFAULT '',
    `payload`    longblob     NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `snapshot_object__whois_pkey_uk` (`whois_pkey`)
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
