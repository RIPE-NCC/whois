ALTER TABLE version ADD PRIMARY KEY (version);

TRUNCATE version;
INSERT INTO version VALUES ('whois-1.115');
