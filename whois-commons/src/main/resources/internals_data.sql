-- this could be parsed as well, but we want to avoid that overhead for each integration test
INSERT INTO authoritative_resource VALUES ('test', 'AS102');
INSERT INTO authoritative_resource VALUES ('test', '::/0');
INSERT INTO authoritative_resource VALUES ('test', '0.0.0.0/0');
