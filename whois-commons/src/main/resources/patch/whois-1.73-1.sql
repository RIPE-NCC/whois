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

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.73-1');

