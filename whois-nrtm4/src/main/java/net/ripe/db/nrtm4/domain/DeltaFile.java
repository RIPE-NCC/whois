package net.ripe.db.nrtm4.domain;

public record DeltaFile(long id, long versionId, String name, String hash, String payload) {

    public static DeltaFile of(final long versionId, final String name, final String hash, final String payload) {
        return new DeltaFile(0, versionId, name, hash, payload);
    }

}
