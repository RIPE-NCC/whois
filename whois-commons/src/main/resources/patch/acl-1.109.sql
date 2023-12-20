DROP TABLE IF EXISTS `acl_sso_denied`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acl_sso_denied` (
                                  `sso_id` varchar(50) NOT NULL,
                                  `comment` text,
                                  `denied_date` date NOT NULL DEFAULT '0000-00-00',
                                  PRIMARY KEY (`sso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acl_event`
--

DROP TABLE IF EXISTS `acl_sso_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acl_sso_event` (
                                 `sso_id` varchar(50) NOT NULL,
                                 `event_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
                                 `daily_limit` int(11) NOT NULL,
                                 `event_type` varchar(20) NOT NULL,
                                 PRIMARY KEY (`sso_id`,`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.109');