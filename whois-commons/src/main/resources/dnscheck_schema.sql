-- Slightly adjusted for our needs:
-- * foreign constraints removed
-- * irrelevant non-default fields removed
-- * auto_increment start values removed
--
--
-- MySQL dump 10.13  Distrib 5.1.65, for apple-darwin12.2.0 (i386)
--
-- Host: dnscheck.ripe.net    Database: dnscheck
-- ------------------------------------------------------
-- Server version	5.1.61

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

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `messages` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) NOT NULL DEFAULT '',
  `arguments` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `language` varchar(16) NOT NULL DEFAULT 'en-US',
  `formatstring` varchar(255) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `langtag` (`tag`,`language`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `queue`
--

DROP TABLE IF EXISTS `queue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `queue` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) DEFAULT NULL,
  `priority` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `inprogress` datetime DEFAULT NULL,
  `tester_pid` int(10) unsigned DEFAULT NULL,
  `source_id` int(10) unsigned DEFAULT NULL,
  `source_data` varchar(255) DEFAULT NULL,
  `fake_parent_glue` text,
  PRIMARY KEY (`id`),
  KEY `queue_domain` (`domain`(15)),
  KEY `queue_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `results`
--

DROP TABLE IF EXISTS `results`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `results` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `test_id` int(10) unsigned NOT NULL,
  `timestamp` datetime DEFAULT NULL,
  `level` varchar(16) DEFAULT NULL,
  `message` varchar(255) NOT NULL DEFAULT '',
  `arg0` varchar(255) DEFAULT NULL,
  `arg1` varchar(255) DEFAULT NULL,
  `arg2` varchar(255) DEFAULT NULL,
  `arg3` varchar(255) DEFAULT NULL,
  `arg4` varchar(255) DEFAULT NULL,
  `arg5` varchar(255) DEFAULT NULL,
  `arg6` varchar(255) DEFAULT NULL,
  `arg7` varchar(255) DEFAULT NULL,
  `arg8` varchar(255) DEFAULT NULL,
  `arg9` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `tests` (`test_id`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tests`
--

DROP TABLE IF EXISTS `tests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tests` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(255) NOT NULL DEFAULT '',
  `begin` datetime DEFAULT NULL,
  `end` datetime DEFAULT NULL,
  `count_critical` int(10) unsigned DEFAULT '0',
  `count_error` int(10) unsigned DEFAULT '0',
  `count_warning` int(10) unsigned DEFAULT '0',
  `count_notice` int(10) unsigned DEFAULT '0',
  `count_info` int(10) unsigned DEFAULT '0',
  `source_id` int(10) unsigned DEFAULT NULL,
  `source_data` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `tests_domain` (`domain`(15)),
  KEY `tests_begin` (`begin`),
  KEY `tests_source_data` (`source_id`,`source_data`)
) ENGINE=InnoDB DEFAULT CHARSET=ascii;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-11-26 16:06:20
