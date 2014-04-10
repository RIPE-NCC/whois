-- MySQL dump 10.13  Distrib 5.1.61, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: WHOIS_UPDATE_RIPE
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

--
-- Table structure for table `abuse_c`
--

DROP TABLE IF EXISTS `abuse_c`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `abuse_c` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `abuse_mailbox`
--

DROP TABLE IF EXISTS `abuse_mailbox`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `abuse_mailbox` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `abuse_mailbox` varchar(80) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`abuse_mailbox`,`object_id`),
  KEY `abuse_mailbox_object_id_index` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `admin_c`
--

DROP TABLE IF EXISTS `admin_c`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_c` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `as_block`
--

DROP TABLE IF EXISTS `as_block`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `as_block` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `begin_as` int(10) unsigned NOT NULL DEFAULT '0',
  `end_as` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`),
  KEY `begin_as` (`begin_as`),
  KEY `end_as` (`end_as`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `as_set`
--

DROP TABLE IF EXISTS `as_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `as_set` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `as_set` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `as_set` (`as_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `aut_num`
--

DROP TABLE IF EXISTS `aut_num`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `aut_num` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `aut_num` char(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `aut_num` (`aut_num`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `auth`
--

DROP TABLE IF EXISTS `auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auth` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `auth` varchar(90) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`auth`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `domain`
--

DROP TABLE IF EXISTS `domain`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domain` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `domain` varchar(254) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ds_rdata`
--

DROP TABLE IF EXISTS `ds_rdata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ds_rdata` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `ds_rdata` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`ds_rdata`,`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `e_mail`
--

DROP TABLE IF EXISTS `e_mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `e_mail` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `e_mail` varchar(80) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`e_mail`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `filter_set`
--

DROP TABLE IF EXISTS `filter_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `filter_set` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `filter_set` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `filter_set` (`filter_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fingerpr`
--

DROP TABLE IF EXISTS `fingerpr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fingerpr` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `fingerpr` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`fingerpr`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `form`
--

DROP TABLE IF EXISTS `form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `form` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `form_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`form_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '0',
  `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `object` longblob NOT NULL,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`,`sequence_id`),
  KEY `history_pkey` (`pkey`),
  KEY `history_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ifaddr`
--

DROP TABLE IF EXISTS `ifaddr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ifaddr` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `ifaddr` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`ifaddr`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inaddr_arpa`
--

DROP TABLE IF EXISTS `inaddr_arpa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inaddr_arpa` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `begin_in` int(10) unsigned NOT NULL DEFAULT '0',
  `end_in` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inet6num`
--

DROP TABLE IF EXISTS `inet6num`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inet6num` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `i6_msb` varchar(20) NOT NULL DEFAULT '',
  `i6_lsb` varchar(20) NOT NULL DEFAULT '',
  `prefix_length` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `netname` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `netname` (`netname`),
  KEY `i6_msb` (`i6_msb`),
  KEY `i6_lsb` (`i6_lsb`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inet_rtr`
--

DROP TABLE IF EXISTS `inet_rtr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inet_rtr` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `inet_rtr` varchar(254) NOT NULL DEFAULT '',
  `local_as` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `inet_rtr` (`inet_rtr`),
  KEY `local_as` (`local_as`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inetnum`
--

DROP TABLE IF EXISTS `inetnum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inetnum` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `begin_in` int(10) unsigned NOT NULL DEFAULT '0',
  `end_in` int(10) unsigned NOT NULL DEFAULT '0',
  `netname` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `netname` (`netname`),
  KEY `begin_in` (`begin_in`),
  KEY `end_in` (`end_in`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `interface`
--

DROP TABLE IF EXISTS `interface`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `interface` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `interface_v6_msp` varchar(20) NOT NULL DEFAULT '',
  `interface_v6_lsp` varchar(20) NOT NULL DEFAULT '',
  `interface_v4` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`interface_v6_msp`,`interface_v6_lsp`,`interface_v4`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `irt`
--

DROP TABLE IF EXISTS `irt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `irt` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `irt` varchar(80) NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`),
  KEY `irt` (`irt`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `irt_nfy`
--

DROP TABLE IF EXISTS `irt_nfy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `irt_nfy` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `irt_nfy` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`irt_nfy`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `key_cert`
--

DROP TABLE IF EXISTS `key_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `key_cert` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `key_cert` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `key_cert` (`key_cert`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `last`
--

DROP TABLE IF EXISTS `last`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `last` (
  `object_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '1',
  `timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `object` longblob NOT NULL,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`,`sequence_id`),
  KEY `last_pkey` (`pkey`),
  KEY `object_type_index` (`object_type`)
) ENGINE=InnoDB AUTO_INCREMENT=12650254 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mbrs_by_ref`
--

DROP TABLE IF EXISTS `mbrs_by_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mbrs_by_ref` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member_of`
--

DROP TABLE IF EXISTS `member_of`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `member_of` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `set_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`set_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_by`
--

DROP TABLE IF EXISTS `mnt_by`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_by` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_domains`
--

DROP TABLE IF EXISTS `mnt_domains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_domains` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_irt`
--

DROP TABLE IF EXISTS `mnt_irt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_irt` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `irt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`irt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_lower`
--

DROP TABLE IF EXISTS `mnt_lower`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_lower` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_nfy`
--

DROP TABLE IF EXISTS `mnt_nfy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_nfy` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_nfy` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`mnt_nfy`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_ref`
--

DROP TABLE IF EXISTS `mnt_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_ref` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mnt_routes`
--

DROP TABLE IF EXISTS `mnt_routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mnt_routes` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mntner`
--

DROP TABLE IF EXISTS `mntner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mntner` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mntner` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `mntner` (`mntner`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `names`
--

DROP TABLE IF EXISTS `names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `names` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nic_hdl`
--

DROP TABLE IF EXISTS `nic_hdl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nic_hdl` (
  `range_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `range_start` int(10) unsigned NOT NULL DEFAULT '0',
  `range_end` int(10) unsigned NOT NULL DEFAULT '0',
  `space` char(4) NOT NULL DEFAULT '',
  `source` char(10) NOT NULL DEFAULT '',
  PRIMARY KEY (`range_id`,`range_start`,`range_end`),
  KEY `range_start` (`range_start`),
  KEY `range_end` (`range_end`),
  KEY `space` (`space`,`source`)
) ENGINE=InnoDB AUTO_INCREMENT=1924502 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notify`
--

DROP TABLE IF EXISTS `notify`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notify` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `notify` varchar(80) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`notify`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nserver`
--

DROP TABLE IF EXISTS `nserver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `nserver` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `host` varchar(254) NOT NULL DEFAULT '',
  PRIMARY KEY (`host`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `org`
--

DROP TABLE IF EXISTS `org`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `org_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`org_id`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `org_name`
--

DROP TABLE IF EXISTS `org_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `org_name` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`name`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organisation`
--

DROP TABLE IF EXISTS `organisation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organisation` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `organisation` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`organisation`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organisation_id`
--

DROP TABLE IF EXISTS `organisation_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organisation_id` (
  `range_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `range_end` int(10) unsigned NOT NULL DEFAULT '0',
  `space` char(4) NOT NULL DEFAULT '',
  `source` char(10) NOT NULL DEFAULT '',
  PRIMARY KEY (`range_id`),
  UNIQUE KEY `space` (`space`,`source`)
) ENGINE=InnoDB AUTO_INCREMENT=22661 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `peering_set`
--

DROP TABLE IF EXISTS `peering_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `peering_set` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `peering_set` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `peering_set` (`peering_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_role`
--

DROP TABLE IF EXISTS `person_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person_role` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `nic_hdl` varchar(30) NOT NULL DEFAULT '',
  `object_type` tinyint(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`object_id`),
  KEY `nic_hdl` (`nic_hdl`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ping_hdl`
--

DROP TABLE IF EXISTS `ping_hdl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ping_hdl` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poem`
--

DROP TABLE IF EXISTS `poem`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `poem` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `poem` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `poem` (`poem`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `poetic_form`
--

DROP TABLE IF EXISTS `poetic_form`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `poetic_form` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `poetic_form` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`poetic_form`),
  KEY `poetic_form` (`poetic_form`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ref_nfy`
--

DROP TABLE IF EXISTS `ref_nfy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ref_nfy` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `ref_nfy` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`ref_nfy`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `referral_by`
--

DROP TABLE IF EXISTS `referral_by`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `referral_by` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `mnt_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`mnt_id`,`object_id`),
  KEY `object_id` (`object_id`),
  KEY `object_type` (`object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `route`
--

DROP TABLE IF EXISTS `route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `prefix` int(10) unsigned NOT NULL DEFAULT '0',
  `prefix_length` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `origin` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `origin` (`origin`,`prefix`,`prefix_length`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `route6`
--

DROP TABLE IF EXISTS `route6`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route6` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `r6_msb` varchar(20) NOT NULL DEFAULT '',
  `r6_lsb` varchar(20) NOT NULL DEFAULT '',
  `prefix_length` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `origin` varchar(13) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `origin` (`origin`,`r6_msb`,`r6_lsb`,`prefix_length`),
  KEY `r6_msb` (`r6_msb`),
  KEY `r6_lsb` (`r6_lsb`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `route_set`
--

DROP TABLE IF EXISTS `route_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route_set` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `route_set` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `route_set` (`route_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rtr_set`
--

DROP TABLE IF EXISTS `rtr_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rtr_set` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `rtr_set` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`),
  KEY `rtr_set` (`rtr_set`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `serials`
--

DROP TABLE IF EXISTS `serials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `serials` (
  `serial_id` int(11) NOT NULL AUTO_INCREMENT,
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `sequence_id` int(10) unsigned NOT NULL DEFAULT '0',
  `atlast` tinyint(4) unsigned NOT NULL DEFAULT '0',
  `operation` tinyint(4) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`serial_id`),
  KEY `object` (`object_id`,`sequence_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25050923 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sponsoring_org`
--

DROP TABLE IF EXISTS `sponsoring_org`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sponsoring_org` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `org_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`org_id`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tags` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `tag_id` varchar(50) NOT NULL DEFAULT '',
  `data` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`object_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tech_c`
--

DROP TABLE IF EXISTS `tech_c`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tech_c` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `upd_to`
--

DROP TABLE IF EXISTS `upd_to`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `upd_to` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `upd_to` varchar(80) NOT NULL DEFAULT '',
  PRIMARY KEY (`upd_to`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `update_lock`
--

DROP TABLE IF EXISTS `update_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `update_lock` (
  `global_lock` int(11) NOT NULL,
  PRIMARY KEY (`global_lock`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `version`
--

DROP TABLE IF EXISTS `version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `x509`
--

DROP TABLE IF EXISTS `x509`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `x509` (
  `keycert_id` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`keycert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `zone_c`
--

DROP TABLE IF EXISTS `zone_c`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `zone_c` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-09-27 12:13:22
