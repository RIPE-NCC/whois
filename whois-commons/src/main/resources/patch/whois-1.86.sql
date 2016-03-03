
use WHOIS_UPDATE_RIPE;

DROP TABLE IF EXISTS `transfer_update_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfer_update_lock` (
  `global_lock` int(11) NOT NULL,
  PRIMARY KEY (`global_lock`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

INSERT INTO transfer_update_lock VALUES (0);

truncate ACL_RIPE.version; INSERT INTO ACL_RIPE.version VALUES ('whois-1.86');
truncate INTERNALS_RIPE.version; INSERT INTO INTERNALS_RIPE.version VALUES ('whois-1.86');
truncate MAILUPDATES_RIPE.version; INSERT INTO MAILUPDATES_RIPE.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_AFRINIC_GRS.version; INSERT INTO WHOIS_MIRROR_AFRINIC_GRS.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_APNIC_GRS.version; INSERT INTO WHOIS_MIRROR_APNIC_GRS.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_ARIN_GRS.version; INSERT INTO WHOIS_MIRROR_ARIN_GRS.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_JPIRR_GRS.version; INSERT INTO WHOIS_MIRROR_JPIRR_GRS.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_LACNIC_GRS.version; INSERT INTO WHOIS_MIRROR_LACNIC_GRS.version VALUES ('whois-1.86');
truncate WHOIS_MIRROR_RADB_GRS.version; INSERT INTO WHOIS_MIRROR_RADB_GRS.version VALUES ('whois-1.86');
truncate WHOIS_UPDATE_RIPE.version; INSERT INTO WHOIS_UPDATE_RIPE.version VALUES ('whois-1.86');