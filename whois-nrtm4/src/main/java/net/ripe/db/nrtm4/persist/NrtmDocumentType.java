package net.ripe.db.nrtm4.persist;

public enum NrtmDocumentType {
    DELTA,
    SNAPSHOT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
