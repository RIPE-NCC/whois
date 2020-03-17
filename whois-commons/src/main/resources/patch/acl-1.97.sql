DROP TABLE IF EXISTS `acl_mirror`;
DROP TABLE IF EXISTS `acl_mirror_delete_log`;

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.97');