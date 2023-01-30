package net.ripe.db.nrtm4.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;


public record SnapshotObject(
    long id,
    long sourceId,
    int objectId,
    int sequenceId,
    RpslObject rpsl
) {

}

