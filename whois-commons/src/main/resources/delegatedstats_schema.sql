DROP TABLE IF EXISTS `delegated_stats`;
create table delegated_stats (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
	`source` varchar(20) NOT NULL,
	`country` varchar(10),
	`type` varchar(10),
	`resource_start` varchar(20) NOT NULL DEFAULT '',
	`resource_end` varchar(20) NOT NULL DEFAULT '',
  `prefix_length` int NOT NULL,
	`value` varchar(10),
  `status` varchar(20),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;