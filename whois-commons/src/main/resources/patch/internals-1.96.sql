
DROP TABLE IF EXISTS `non_auth_route`;
CREATE TABLE `non_auth_route` (
    `object_pkey` VARCHAR(254) NOT NULL,
    `created_at` DATE NOT NULL,
    PRIMARY KEY (`object_pkey`)
)  ENGINE=InnoDB DEFAULT CHARSET=latin1;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.96');
