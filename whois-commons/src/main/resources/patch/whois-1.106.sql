-- delete default maintainer history tables. Not longer in use.

DROP TABLE IF EXISTS `default_maintainer_history`;
DROP TABLE IF EXISTS `default_maintainer_sync_history`;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.106');