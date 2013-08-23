DROP TABLE IF EXISTS `pending_updates`;
create table pending_updates (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `object_type` tinyint(3) unsigned NOT NULL,
  `pkey` varchar(254) NOT NULL,
  `stored_date` date NOT NULL DEFAULT '0000-00-00',
  `passed_authentications` VARCHAR(100) NOT NULL,
  `object` longblob NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `version`;
CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
