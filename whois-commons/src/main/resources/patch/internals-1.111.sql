--
-- TODO: keep synchronised with internals_schema.sql
--

DROP TABLE IF EXISTS `outgoing_message`;
DROP TABLE IF EXISTS `undeliverable_email`;

CREATE TABLE `outgoing_message` (
     `message_id` varchar(80) NOT NULL,
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE INDEX outgoing_message_email_i ON outgoing_message(email);

CREATE TABLE `undeliverable_email` (
     `email` varchar(80) NOT NULL,
     `last_update` datetime NOT NULL,
     PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.111');
