--
-- added table for querying object references in history
--

DROP TABLE  IF EXISTS `object_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_version` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `from_timestamp` int(10) unsigned NOT NULL,
  `to_timestamp` int(10) unsigned DEFAULT NULL,
  `revision` int(10) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE  IF EXISTS `object_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_reference` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `from_version` int(11) unsigned NOT NULL,
  `to_version` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`from_version`) REFERENCES `object_version` (`id`),
  CONSTRAINT FOREIGN KEY (`to_version`) REFERENCES `object_version` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.74-1');
