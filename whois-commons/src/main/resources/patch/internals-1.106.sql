-- delete default maintainer history tables. Not longer in use.

DROP TABLE IF EXISTS `default_maintainer_history`;
DROP TABLE IF EXISTS `default_maintainer_sync_history`;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.106');