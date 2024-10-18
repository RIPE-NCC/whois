DROP TABLE IF EXISTS `email_status`;
CREATE TABLE `email_status` (
                                `email` varchar(320) NOT NULL,
                                `status` varchar(120) NOT NULL,
                                `message` longblob,
                                `last_update` datetime DEFAULT now(),
                                PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


TRUNCATE version;
INSERT INTO version VALUES ('internals-1.116');

