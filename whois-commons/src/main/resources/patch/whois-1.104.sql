-- delete tags functionality from whois

DROP TABLE IF EXISTS `tags`;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.104');

