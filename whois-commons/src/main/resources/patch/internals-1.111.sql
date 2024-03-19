DROP TABLE IF EXISTS `outgoing_message`;
DROP TABLE IF EXISTS `email_status`;

CREATE TABLE `outgoing_message` (
     `message_id` varchar(80) NOT NULL,
     `email` varchar(80) NOT NULL,
     `last_update` datetime DEFAULT now(),
     PRIMARY KEY (`message_id`, `email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE INDEX outgoing_message_email_i ON outgoing_message(email);

CREATE TABLE `email_status` (
     `email` varchar(80) NOT NULL,
     `status` varchar(120) NOT NULL,
     `last_update` datetime DEFAULT now(),
     PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.111');
