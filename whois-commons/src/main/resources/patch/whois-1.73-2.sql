--
-- Table structure for table `sponsoring_org`
--

ALTER TABLE organisation_id DROP PRIMARY KEY, DROP KEY `range_end`, DROP KEY `space`, ADD PRIMARY KEY (`range_id`), ADD UNIQUE KEY `space` (`space`, `source`);

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.73-2');

