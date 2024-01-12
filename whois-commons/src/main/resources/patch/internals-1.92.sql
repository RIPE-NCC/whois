CREATE TABLE `abuse_email` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `address` varchar(256) NOT NULL,
  `checked_at` timestamp null default null,
  `comment` varchar(256),
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` varchar(7) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE UNIQUE INDEX abuse_email_address_i on `abuse_email`(`address`);

CREATE TABLE `abuse_org_email` (
  `org_id` varchar(256) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` timestamp null default null,
  `email_id` int(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`org_id`, `email_id`),
  CONSTRAINT FOREIGN KEY (`email_id`) REFERENCES `abuse_email` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `abuse_ticket` (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL,
  `last_checked` timestamp NOT NULL,
  `last_modified` timestamp NOT NULL,
  `org_id` varchar(256) NOT NULL,
  `status` varchar(256),
  `ticket_id` int(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE INDEX abuse_ticket_org_id_i ON abuse_ticket(org_id);
CREATE UNIQUE INDEX abuse_ticket_ticket_id_i on abuse_ticket(ticket_id);

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.92');
