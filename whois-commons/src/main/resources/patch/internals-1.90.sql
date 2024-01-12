ALTER TABLE default_maintainer_history DROP COLUMN result;

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.90');
