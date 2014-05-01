--
-- Allows for case insensitive lookups in auth table.
-- executed in RC: Query OK, 10386 rows affected (1.78 sec)
--

ALTER TABLE `auth` MODIFY COLUMN `auth` varchar(90) NOT NULL DEFAULT '';

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.73-3');