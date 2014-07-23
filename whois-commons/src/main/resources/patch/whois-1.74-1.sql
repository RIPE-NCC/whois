--
-- added table for querying object references in history
--

DROP TABLE  IF EXISTS `object_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_version` (
  `version_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `from_timestamp` int(10) unsigned NOT NULL,
  `to_timestamp` int(10) unsigned DEFAULT NULL,
  `revision` int(10) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE  IF EXISTS `object_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_reference` (
  `version_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `ref_type` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '0=referencing, 1=referenced by',
  KEY `version_id` (`version_id`),
  CONSTRAINT FOREIGN KEY (`version_id`) REFERENCES `object_version` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


TRUNCATE version;
INSERT INTO version VALUES ('whois-1.74-1');
