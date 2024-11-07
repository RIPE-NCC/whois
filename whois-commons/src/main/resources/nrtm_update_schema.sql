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
DROP TABLE IF EXISTS `last`;

CREATE TABLE `version`
(
    `version` varchar(80)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `last`
(
    `object_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `sequence_id` int(10) unsigned NOT NULL DEFAULT '1',
    `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
    `object_type` varchar(254) NOT NULL DEFAULT '0',
    `object` longblob NOT NULL,
    `pkey` varchar(254) NOT NULL DEFAULT '',
    PRIMARY KEY (`object_id`,`sequence_id`),
    KEY `last_pkey` (`pkey`),
    KEY `object_type_index` (`object_type`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4;