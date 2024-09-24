DROP TABLE IF EXISTS `transfer_update_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfer_update_lock` (
  `global_lock` int(11) NOT NULL,
  PRIMARY KEY (`global_lock`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO transfer_update_lock VALUES (0);

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.114');
