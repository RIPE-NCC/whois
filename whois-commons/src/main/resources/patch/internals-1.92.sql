DROP TABLE IF EXISTS `abuse_email`;
CREATE TABLE `abuse_email` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `address` varchar(256) NOT NULL,
  `checked_at` timestamp null default null,
  `comment` varchar(256),
  `status` varchar(256),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE UNIQUE INDEX abuse_email_address_i on `abuse_email`(`address`);

DROP TABLE IF EXISTS `abuse_org_email`;
CREATE TABLE `abuse_org_email` (
  `org_id` varchar(256) NOT NULL,
  `email_id` int(10) UNSIGNED NOT NULL,
  `deleted_at` timestamp null default null,
  PRIMARY KEY (`org_id`, `email_id`),
  CONSTRAINT FOREIGN KEY (`email_id`) REFERENCES `abuse_email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92');
