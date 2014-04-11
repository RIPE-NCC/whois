-- this could be parsed as well, but we want to avoid that overhead for each integration test
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS102');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS103');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS200');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS250');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS251');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS300');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS650');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', '::/0');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', '0.0.0.0/0');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS222');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS333');
-- legacy autnums
INSERT INTO legacy_autnums (autnum) VALUES ('103');
INSERT INTO legacy_autnums (autnum) VALUES ('12666');
INSERT INTO legacy_autnums (autnum) VALUES ('12668');