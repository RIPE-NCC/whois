package net.ripe.db.nrtm4.dao;

public record SnapshotObject(
    long id,
    long versionId,
    int objectId,
    int sequenceId,
    String rpsl
) {

}

