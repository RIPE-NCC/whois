/*
Migrate existing ADMIN database, ACL and ACL6 table data, into the new ACL schema.

USE WHOIS_DEV_ACL;

SELECT prefix, daily_limit, convert(concat('Imported at ', CURRENT_TIMESTAMP) using utf8) FROM (
          SELECT  convert(concat(inet_ntoa(prefix),'/',prefix_length) USING utf8) AS prefix,
                  maxbonus AS daily_limit
  FROM acl
  WHERE deny != 1
        UNION
          SELECT  convert(concat(
          hex((prefix1 & 0xFFFF0000) >> 16),':',hex(prefix1 & 0xFFFF),
                  ':',
                  hex((prefix2 & 0xFFFF0000) >> 16),':',hex(prefix2 & 0xFFFF),
                  ':',
                  hex((prefix3 & 0xFFFF0000) >> 16),':',hex(prefix3 & 0xFFFF),
                  ':',
                  hex((prefix4 & 0xFFFF0000) >> 16),':',hex(prefix4 & 0xFFFF),
                  '/',prefix_length) USING utf8) AS prefix,
                  maxbonus AS daily_limit
  FROM acl6
  WHERE deny != 1
        ) AS t
  WHERE daily_limit = -1 OR daily_limit > 5000;


DELETE FROM acl_limit;

INSERT INTO acl_limit(prefix,daily_limit,comment) VALUES
();

*/
INSERT INTO acl_limit(prefix,daily_limit,comment) VALUES
('127.0.0.1/32','-1','Imported at 2012-02-20 10:47:58'),
('192.36.125.26/32','10000','Imported at 2012-02-20 10:47:58'),
('193.0.0.0/23','-1','Imported at 2012-02-20 10:47:58'),
('193.0.0.214/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.0.215/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.0/24','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.9/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.31/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.47/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.61/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.232/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.1.237/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.3.1/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.4.149/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.5.22/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.5.23/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.10.0/23','-1','Imported at 2012-02-20 10:47:58'),
('193.0.10.130/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.20/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.31/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.45/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.46/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.47/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.48/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.19.232/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.21.0/24','-1','Imported at 2012-02-20 10:47:58'),
('193.0.21.57/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.21.59/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.23.0/24','-1','Imported at 2012-02-20 10:47:58'),
('193.0.23.6/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.23.7/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.23.8/32','-1','Imported at 2012-02-20 10:47:58'),
('193.0.23.9/32','-1','Imported at 2012-02-20 10:47:58'),
('202.12.29.8/32','-1','Imported at 2012-02-20 10:47:58'),
('203.119.0.114/32','-1','Imported at 2012-02-20 10:47:58'),
('203.119.0.116/32','-1','Imported at 2012-02-20 10:47:58'),
('0:0:0:0:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:610:240:0:0:0:0:0/48','-1','Imported at 2012-02-20 10:47:58'),
('2001:610:240:1:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:610:240:11:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:610:240:25:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:67C:64:0:0:0:0:0/48','-1','Imported at 2012-02-20 10:47:58'),
('2001:67C:2E8:1:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:67C:2E8:5:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:838:1:1:0:0:0:0/64','10000','Imported at 2012-02-20 10:47:58'),
('2001:DC0:2001:0:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:DC0:2001:6:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:DC0:2001:7:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58'),
('2001:44B8:61:0:0:0:0:0/64','-1','Imported at 2012-02-20 10:47:58');
