--
-- TODO: keep synchronised with internals_schema.sql
--

DROP TABLE IF EXISTS `outgoing_message`;
DROP TABLE IF EXISTS `undeliverable_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outgoing_message` (
     `message_id` varchar(80) NOT NULL,
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `undeliverable_email` (
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.111');
