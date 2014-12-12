DROP TABLE IF EXISTS `apikeys`;
CREATE TABLE `apikeys` (
  `apikey` varchar(128) NOT NULL,
  `uri_prefix` varchar(128) NOT NULL,
  `comment` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


