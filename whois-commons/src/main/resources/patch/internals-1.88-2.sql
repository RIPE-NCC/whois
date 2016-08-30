DELETE FROM scheduler WHERE task = 'UnrefCleanup';
INSERT INTO scheduler VALUES (now(), 'CleanupUnreferencedObjects', 'initial', 1);

TRUNCATE version;
INSERT INTO version VALUES ('internals-1.88-2');
