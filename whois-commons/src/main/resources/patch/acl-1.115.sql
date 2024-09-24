ALTER TABLE version ADD PRIMARY KEY (version);
ALTER TABLE apikeys ADD CONSTRAINT apikey_prefix UNIQUE (apikey,uri_prefix);

TRUNCATE version;
INSERT INTO version VALUES ('acl-1.115');