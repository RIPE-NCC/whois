package net.ripe.db.nrtm4.persist;

public enum NrtmDocumentType {
    DELTA,
    SNAPSHOT;

    public String nameToLowerCase() {
        return name().toLowerCase();
    }
}
