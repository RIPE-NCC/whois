ALTER TABLE version ADD PRIMARY KEY (version);

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.115');
