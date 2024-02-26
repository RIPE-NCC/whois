DROP TABLE IF EXISTS `bounced_mail`;
DROP TABLE IF EXISTS `bounced_email_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `in_progress_mail` (
     `messageId` varchar(80) NOT NULL,
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`messageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `undeliverable_email` (
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.111');
