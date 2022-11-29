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
    `id`   int(11) unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(40)      NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `source__name_uk` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

DROP TABLE IF EXISTS `version_information`;
create table `version_information`
(
    `id`         int(11) unsigned NOT NULL AUTO_INCREMENT,
    `source_id`  int(11) unsigned NOT NULL,
    `version`    int(10) unsigned NOT NULL DEFAULT '0',
    `session_id` varchar(128)     NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `version_information__source_version_uk` (`source_id`, `version`),
    CONSTRAINT FOREIGN KEY (`source_id`) REFERENCES `source` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;

/*
drop table if exists `nrtm_object`;
create table `nrtm_object`
(
    `id`        int(11) unsigned NOT NULL AUTO_INCREMENT,
    `source_id` int(11) unsigned NOT NULL,
    `whois_key` varchar(256)     NOT NULL DEFAULT '',
    `payload`   varchar(4096)    NOT NULL DEFAULT '',
    PRIMARY KEY (`id`),
    CONSTRAINT FOREIGN KEY (`source_id`) REFERENCES `source` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
*/

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;
