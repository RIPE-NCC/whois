package net.ripe.db.nrtm4.domain;

public record DeltaFile(long id, long versionId, String name, String hash, String payload, long created) {

}
