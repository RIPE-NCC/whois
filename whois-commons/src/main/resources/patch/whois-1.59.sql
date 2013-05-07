DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `object_id` int(10) unsigned NOT NULL DEFAULT '0',
  `tag_id` int(2) NOT NULL DEFAULT '0',
  `data` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.59');
