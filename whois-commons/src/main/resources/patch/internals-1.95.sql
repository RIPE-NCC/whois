
DROP TABLE IF EXISTS `default_maintainer_sync_history`;
CREATE TABLE `default_maintainer_sync_history` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `org` varchar(256) NOT NULL,
    `mntner` varchar(256) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `email` varchar(256) NOT NULL,
    `is_synchronised` tinyint(1) DEFAULT 0,PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.95');
