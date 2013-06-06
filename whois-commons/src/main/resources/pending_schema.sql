
DROP TABLE IF EXISTS `pending_updates`;
create table pending_updates (
  `object_type` tinyint(3) unsigned NOT NULL,
  `pkey` varchar(254) NOT NULL,
  `stored_date` date NOT NULL DEFAULT '0000-00-00',
  `authenticated_by` VARCHAR(100) NOT NULL,
  `object` longblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `version`;
CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;