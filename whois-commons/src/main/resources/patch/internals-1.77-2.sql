DROP TABLE IF EXISTS `email_links`;
CREATE TABLE `email_links` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `hash` varchar(256) NOT NULL,
  `mntner` varchar(256) NOT NULL,
  `email` varchar(256) NOT NULL,
  `creation_date` int(10) unsigned NOT NULL DEFAULT '0',
  `expiry_date` int(10) unsigned NOT NULL DEFAULT '0',
  `created_by` varchar(256),
  `expired_by` varchar(256),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `forgot_password_audit_log`;
CREATE TABLE `forgot_password_audit_log` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `timestamp` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `entry` varchar(256) NOT NULL,
  `address` varchar(256) NOT NULL,
  `mntner` varchar(256),
  `email` varchar(256),
  `hash`  varchar(256),
  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`hash`) REFERENCES `email_links` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO version VALUES ('internals-1.77-2');

