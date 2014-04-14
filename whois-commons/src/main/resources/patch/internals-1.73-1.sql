USE INTERNALS_RIPE;

DROP TABLE IF EXISTS `legacy_autnums`;
CREATE TABLE `legacy_autnums` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `autnum` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.73-1');
