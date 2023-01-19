package net.ripe.db.nrtm4.dao;

import net.ripe.db.whois.common.rpsl.RpslObject;


public record SnapshotObject(
    long id,
    long versionId,
    int objectId,
    int sequenceId,
    RpslObject rpsl
) {

}

