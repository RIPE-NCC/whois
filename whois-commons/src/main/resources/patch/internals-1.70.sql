--
-- REMEMBER TO REMOVE DATASOURCE DEFINITIONS FROM whois.properties TOO!
--
CREATE DATABASE INTERNALS_PRE;

RENAME TABLE PENDING_PRE.pending_updates TO INTERNALS_PRE.pending_updates;
DROP DATABASE PENDING_PRE;

DROP DATABASE SCHEDULER_PRE;

USE INTERNALS_PRE;

CREATE TABLE `authoritative_resource` (
  `source` varchar(16) NOT NULL,
  `resource` varchar(128) NOT NULL,
  KEY(`source`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `version` (
  `version` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO version VALUES ('internals-1.70');

CREATE TABLE `scheduler` (
  `date` date NOT NULL,
  `task` varchar(256) NOT NULL,
  `host` varchar(50) NOT NULL,
  `done` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`date`, `task`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO scheduler VALUES (now(), 'AuthoritativeResourceImportTask', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'AutomaticPermanentBlocks', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'AutomaticPermanentBlocksCleanup', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'Bootstrap', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'DatabaseTextExport', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'GrsImporter', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'PendingUpdatesCleanup', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'ResetPersonalObjectAccounting', 'initial', 1);
INSERT INTO scheduler VALUES (now(), 'UnrefCleanup', 'initial', 1);
