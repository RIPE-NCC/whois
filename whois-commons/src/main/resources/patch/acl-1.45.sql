-- Use ACL schema
--
-- For override, two default users should exist: 'dbase1' and 'dbase2'
--
-- username     : Case insensitive username
-- password     : MD5 password hash; empty password (MD5 d41d8cd98f00b204e9800998ecf8427e) will never be attempted
-- objecttypes  : Comma separated list of allowed object types for this user, when empty none are allowed
-- last_changed : For administration purposes only

CREATE TABLE override_users (
  username VARCHAR(255) NOT NULL,
  password VARCHAR(64) NOT NULL,
  objecttypes VARCHAR(255) NOT NULL,
  last_changed DATE
);

CREATE UNIQUE INDEX override_username_idx ON override_users (username);
