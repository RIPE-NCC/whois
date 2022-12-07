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

DROP TABLE IF EXISTS `version`;
create table `version`
(
    `id`             int unsigned NOT NULL AUTO_INCREMENT,
    `source_id`      int unsigned NOT NULL,
    `version`        int unsigned NOT NULL,
    `session_id`     varchar(128) NOT NULL,
    `type`           varchar(128) NOT NULL,
    `last_serial_id` int          NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `version__source_fk` FOREIGN KEY (`source_id`) REFERENCES `source` (`id`),
    UNIQUE KEY `version__source__version_uk` (`source_id`, `version`),
    UNIQUE KEY `version__session__version_uk` (`session_id`, `version`),
    UNIQUE KEY `version__type__last_serial_id_uk` (`type`, `last_serial_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `published_file`;
create table `published_file`
(
    `id`         int unsigned    NOT NULL AUTO_INCREMENT,
    `version_id` int unsigned    NOT NULL,
    `type`           varchar(128) NOT NULL,
    `name`       varchar(256)    NOT NULL,
    `hash`       varchar(256)    NOT NULL,
    `created`    bigint unsigned NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `published_file__version_fk` FOREIGN KEY (`version_id`) REFERENCES `version` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `snapshot_object`;
create table `snapshot_object`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT,
    `version_id`  int unsigned NOT NULL,
    `serial_id`   int          NOT NULL,
    `object_type` int          NOT NULL,
    `pkey`        varchar(256) NOT NULL,
    `payload`     longtext     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `snapshot_object__serial_id_uk` (`serial_id`),
    CONSTRAINT `snapshot_object__version_fk` FOREIGN KEY (`version_id`) REFERENCES `version` (`id`)
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
