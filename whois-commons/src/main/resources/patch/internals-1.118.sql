DROP TABLE IF EXISTS `email_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_status_history` (
        `email` varchar(320) NOT NULL,
        `status` varchar(120) NOT NULL,
        `message` longblob,
        `history_update` datetime DEFAULT now(),
        `email_status_update` datetime NOT NULL,
        PRIMARY KEY (`email`, `email_status_update`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.118');