-- add key pairs to nrtm database

DROP TABLE IF EXISTS `key_pair`;
CREATE TABLE `key_pair`
(
    `id`          int unsigned    NOT NULL AUTO_INCREMENT,
    `private_key` VARBINARY(3000) NOT NULL,
    `public_key`  VARBINARY(3000) NOT NULL,
    `created`     bigint unsigned NOT NULL,
    `expires`     bigint unsigned NOT NULL,
    UNIQUE KEY `private_key_name_uk` (`private_key`),
    UNIQUE KEY `public_key_name_uk` (`public_key`),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

TRUNCATE version;
INSERT INTO version VALUES ('nrtm-1.107');
