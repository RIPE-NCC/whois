--
-- added table for querying object references in history
--

DROP TABLE IF EXISTS `object_reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object_reference` (
  `from_object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `from_pkey` varchar(254) NOT NULL DEFAULT '',
  `from_object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `from_sequence_id` int(10) unsigned NOT NULL DEFAULT '0',
  `to_object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `to_pkey` varchar(254) NOT NULL DEFAULT '',
  `to_object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `to_sequence_id` int(10) unsigned NOT NULL DEFAULT '0',
  `from_timestamp` int(10) unsigned NOT NULL DEFAULT '0',
  `to_timestamp` int(10) unsigned DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.74-1');
