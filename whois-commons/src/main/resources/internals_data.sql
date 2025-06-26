-- this could be parsed as well, but we want to avoid that overhead for each integration test
-- all resources listed here are in region
-- the out of region resources should be mentioned in comments to they're easier to find when creating tests
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS1');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS102');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS103');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS123');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS200');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS222');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS250');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS251');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS300');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS333');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS400');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS456');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS650');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS789');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS12666');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', 'AS12668');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', '::/0');
INSERT INTO authoritative_resource (source, resource) VALUES ('test', '0.0.0.0/0');
-- legacy autnums
INSERT INTO legacy_autnums (autnum) VALUES ('103');
INSERT INTO legacy_autnums (autnum) VALUES ('12666');
INSERT INTO legacy_autnums (autnum) VALUES ('12668');

INSERT INTO transfer_update_lock VALUES (0);
