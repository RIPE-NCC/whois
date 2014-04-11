--
-- with the ~20K records in the table in production, this executed under 1 second when run on RC
--

ALTER TABLE organisation_id DROP PRIMARY KEY, DROP KEY `range_end`, DROP KEY `space`, ADD PRIMARY KEY (`range_id`), ADD UNIQUE KEY `space` (`space`, `source`);

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.73-2');

