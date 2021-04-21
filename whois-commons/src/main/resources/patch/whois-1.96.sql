
DROP TABLE IF EXISTS `status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `status` varchar(80) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`status`,`object_id`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.96');
