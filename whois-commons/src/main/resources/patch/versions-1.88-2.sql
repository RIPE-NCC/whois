-- add lock table to versions database

DROP TABLE IF EXISTS `update_lock`;
CREATE TABLE `update_lock` (
  `global_lock` int(11) NOT NULL,
  PRIMARY KEY (`global_lock`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO update_lock VALUES (0);
