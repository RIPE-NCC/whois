
DROP TABLE IF EXISTS `default_maintainer_in_progress`;
CREATE TABLE `default_maintainer_in_progress` (
    `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `org` varchar(256) NOT NULL,
    `mntner` varchar(256) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `uuid` varchar(256) NOT NULL,
    `email` varchar(256),
    PRIMARY KEY (`id`),
    UNIQUE KEY `org_idx` (`org`),
    UNIQUE KEY `mntner_idx` (`mntner`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO  `default_maintainer_in_progress` (`org`, `mntner`, `timestamp`, `uuid`,  `email`) select `org`, `mntner`, `timestamp`, `uuid`,  `email` from `default_maintainer_history` where in_progress = 1;

DROP TABLE IF EXISTS `default_maintainer_sync`;
CREATE TABLE `default_maintainer_sync` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `org` varchar(256) NOT NULL,
    `mntner` varchar(256) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `email` varchar(256) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO  `default_maintainer_sync` (`org`, `mntner`, `timestamp`, `email`) select `org`, `mntner`, `timestamp`,  `email`  from default_maintainer_sync_history where id in (select t1.id  from default_maintainer_sync_history t1 left join default_maintainer_sync_history t2 on t1.mntner = t2.mntner and t1.timestamp < t2. timestamp where  t2.mntner  is null) and is_synchronised=1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.101');
