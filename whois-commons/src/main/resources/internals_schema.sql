-- TODO: the id columns is only there because of a mysql bug (see http://bugs.mysql.com/bug.php?id=58481http://bugs.mysql.com/bug.php?id=58481)
-- TODO: it should be dropped once we manage to upgrade
DROP TABLE IF EXISTS `authoritative_resource`;
CREATE TABLE `authoritative_resource` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `source` varchar(16) NOT NULL,
  `resource` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  KEY(`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `version`;
CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `legacy_autnums`;
CREATE TABLE `legacy_autnums` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `autnum` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `email_links`;
CREATE TABLE `email_links` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `hash` varchar(256) NOT NULL,
  `mntner` varchar(256) NOT NULL,
  `email` varchar(320) NOT NULL,
  `creation_date` int(10) unsigned NOT NULL DEFAULT '0',
  `expiry_date` int(10) unsigned NOT NULL DEFAULT '0',
  `created_by` varchar(256) DEFAULT NULL,
  `expired_by` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `forgot_password_audit_log`;
CREATE TABLE `forgot_password_audit_log` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `timestamp` int(10) UNSIGNED NOT NULL DEFAULT '0',
  `entry` varchar(256) NOT NULL,
  `address` varchar(256) NOT NULL,
  `mntner` varchar(256) DEFAULT NULL,
  `email` varchar(320) DEFAULT NULL,
  `hash`  varchar(256) DEFAULT NULL,
  `user_sso_email`  varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`hash`) REFERENCES `email_links` (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `default_maintainer_sync`;
CREATE TABLE `default_maintainer_sync` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `org` varchar(256) NOT NULL,
    `mntner` varchar(256) NOT NULL,
    `timestamp` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
    `email` varchar(320) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `abuse_email`;
CREATE TABLE `abuse_email` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `address` varchar(320) NOT NULL,
  `checked_at` datetime,
  `comment` varchar(256),
  `created_at` datetime NOT NULL,
  `status` varchar(7) NOT NULL,
  `link_sent_at` datetime,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX abuse_email_address_i on `abuse_email`(`address`);

DROP TABLE IF EXISTS `abuse_org_email`;
CREATE TABLE `abuse_org_email` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `org_id` varchar(256) NOT NULL,
  `enduser_org_id` varchar(256),
  `created_at` datetime NOT NULL,
  `deleted_at` datetime,
  `email_id` int(10) UNSIGNED NOT NULL,
  `object_type` tinyint(3) UNSIGNED NOT NULL,
  `object_pkey` varchar(254) NOT NULL,
  `abuse_nic_hdl` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`email_id`) REFERENCES `abuse_email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `abuse_ticket`;
CREATE TABLE `abuse_ticket` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `last_checked` datetime NOT NULL,
  `org_id` varchar(256) NOT NULL,
  `ticketia_status` varchar(256) NOT NULL,
  `ticket_id` int(10) UNSIGNED NOT NULL,
  `ticket_type` char(1) NOT NULL,
  `state` char(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX abuse_ticket_org_id_i ON abuse_ticket(org_id);
CREATE UNIQUE INDEX abuse_ticket_ticket_id_i on abuse_ticket(ticket_id);

DROP TABLE IF EXISTS `shedlock`;
CREATE TABLE `shedlock` (
    `name` VARCHAR(64),
    `lock_until` TIMESTAMP(3) NULL,
    `locked_at` TIMESTAMP(3) NULL,
    `locked_by`  VARCHAR(255),
    PRIMARY KEY (`name`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `non_auth_route`;
CREATE TABLE `non_auth_route` (
    `object_pkey` VARCHAR(254) NOT NULL,
    `created_at` DATE NOT NULL,
    PRIMARY KEY (`object_pkey`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `non_auth_route_unregistered_space`;
CREATE TABLE `non_auth_route_unregistered_space` (
    `object_pkey` VARCHAR(254) NOT NULL,
    `created_at` DATE NOT NULL,
    PRIMARY KEY (`object_pkey`)
)  ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `outgoing_message`;
CREATE TABLE `outgoing_message` (
   `message_id` varchar(80) NOT NULL,
   `email` varchar(320) NOT NULL,
   `last_update` datetime DEFAULT now(),
   PRIMARY KEY (`message_id`, `email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX outgoing_message_email_i ON outgoing_message(email);

DROP TABLE IF EXISTS `email_status`;
CREATE TABLE `email_status` (
   `email` varchar(320) NOT NULL,
   `status` varchar(120) NOT NULL,
   `message` longblob,
   `last_update` datetime DEFAULT now(),
   PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `email_status_history`;
CREATE TABLE `email_status_history` (
     `email` varchar(320) NOT NULL,
     `status` varchar(120) NOT NULL,
     `message` longblob,
     `archived_date` datetime DEFAULT now(),
     `reported_date` datetime NOT NULL,
     PRIMARY KEY (`email`, `reported_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `transfer_update_lock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transfer_update_lock` (
                                        `global_lock` int(11) NOT NULL,
                                        PRIMARY KEY (`global_lock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
