DROP TABLE  IF EXISTS `object_version`;
CREATE TABLE `object_version` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `pkey` varchar(254) NOT NULL DEFAULT '',
  `object_type` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `from_timestamp` int(10) unsigned NOT NULL,
  `to_timestamp` int(10) unsigned DEFAULT NULL,
  `revision` int(10) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_version` (`pkey`,`object_type`,`revision`),
  KEY `pkey` (`pkey`),
  KEY `object_type` (`object_type`),
  KEY `revision` (`revision`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE  IF EXISTS `object_reference`;
CREATE TABLE `object_reference` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `from_version` int(11) unsigned NOT NULL,
  `to_version` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_reference` (`from_version`,`to_version`),
  KEY `from_version` (`from_version`),
  KEY `to_version` (`to_version`),
  CONSTRAINT FOREIGN KEY (`from_version`) REFERENCES `object_version` (`id`),
  CONSTRAINT FOREIGN KEY (`to_version`) REFERENCES `object_version` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
