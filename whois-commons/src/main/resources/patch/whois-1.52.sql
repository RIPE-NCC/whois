CREATE TABLE `abuse_c` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `pe_ro_id` int(10) unsigned NOT NULL DEFAULT '0',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pe_ro_id`,`object_id`),
  KEY `object_type` (`object_type`),
  KEY `object_id` (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
