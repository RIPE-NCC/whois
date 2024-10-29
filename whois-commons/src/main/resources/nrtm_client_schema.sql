-- MySQL dump 10.13  Distrib 5.1.61, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: NRTM_CLIENT_RIPE
-- ------------------------------------------------------
-- Server version	5.1.61-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


DROP TABLE IF EXISTS `version`;
DROP TABLE IF EXISTS `public_key`;
DROP TABLE IF EXISTS `last_mirror`;
DROP TABLE IF EXISTS `version_info`;


CREATE TABLE `version`
(
    `version` varchar(80)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `public_key`
(
    `id`          int unsigned    NOT NULL AUTO_INCREMENT,
    `public_key`  VARBINARY(3000) NOT NULL,
    `next_public_key`     VARBINARY(3000),
    UNIQUE KEY `public_key_name_uk` (`public_key`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `version_info`
(
    `id`             int unsigned    NOT NULL AUTO_INCREMENT,
    `source`         varchar(40)     NOT NULL,
    `version`        int unsigned    NOT NULL,
    `session_id`     varchar(128)    NOT NULL,
    `type`           varchar(128)    NOT NULL,
    `created`        bigint unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `version_info__session__source__version__type__uk` (`session_id`, `source`, `version`, `type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `last_mirror`
(
    `object_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `sequence_id` int(10) unsigned NOT NULL DEFAULT '1',
    `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
    `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
    `object` longblob NOT NULL,
    `pkey` varchar(254) NOT NULL DEFAULT '',
    PRIMARY KEY (`object_id`,`sequence_id`),
    KEY `last_pkey` (`pkey`),
    KEY `object_type_index` (`object_type`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;