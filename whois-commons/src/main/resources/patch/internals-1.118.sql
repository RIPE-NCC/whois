DROP TABLE IF EXISTS `email_status_archived`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_status_archived` (
        `email` varchar(320) NOT NULL,
        `status` varchar(120) NOT NULL,
        `message` longblob,
        `archived_date` datetime DEFAULT now(),
        `reported_date` datetime NOT NULL,
        PRIMARY KEY (`email`, `reported_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.118');