-- adding indexes to timestamp columns speeds up incremental update

ALTER TABLE `object_version` ADD INDEX `from_timestamp` (`from_timestamp`);
ALTER TABLE `object_version` ADD INDEX `to_timestamp` (`to_timestamp`);
