package net.ripe.db.nrtm4.domain;

public record VersionedDeltaFile(long id, long version, String sessionID, String name, String hash) {
}
