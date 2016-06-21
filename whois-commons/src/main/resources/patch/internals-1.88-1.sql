DROP TABLE IF EXISTS `default_maintainer_history`;
CREATE TABLE `default_maintainer_history` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `org` varchar(256) NOT NULL,
  `mntner` varchar(256) NOT NULL,
  `timestamp` timestamp NOT NULL,
  `uuid` varchar(256) NOT NULL,
  `email` varchar(256),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.88-1');
