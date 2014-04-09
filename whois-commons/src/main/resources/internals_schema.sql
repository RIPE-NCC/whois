DROP TABLE IF EXISTS `pending_updates`;
CREATE TABLE pending_updates (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `object_type` tinyint(3) unsigned NOT NULL,
  `pkey` varchar(254) NOT NULL,
  `stored_date` date NOT NULL DEFAULT '0000-00-00',
  `passed_authentications` VARCHAR(100) NOT NULL,
  `object` longblob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `scheduler`;
CREATE TABLE `scheduler` (
  `date` date NOT NULL,
  `task` varchar(256) NOT NULL,
  `host` varchar(50) NOT NULL,
  `done` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`date`, `task`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- the id columns is only there because of a mysql bug (see http://bugs.mysql.com/bug.php?id=58481http://bugs.mysql.com/bug.php?id=58481)
-- it should be dropped once we manage to upgrade
DROP TABLE IF EXISTS `authoritative_resource`;
CREATE TABLE `authoritative_resource` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `source` varchar(16) NOT NULL,
  `resource` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY(`source`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `version`;
CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `legacy_autnums`;
CREATE TABLE `legacy_autnums` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `autnum` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;