package net.ripe.db.nrtm4.domain;

public record SnapshotFile(long id, long versionId, String name, String hash) {

    public static SnapshotFile of(final long versionId, final String name, final String hash) {
        return new SnapshotFile(0, versionId, name, hash);
    }

}
