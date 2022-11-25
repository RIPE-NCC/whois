package net.ripe.db.nrtm4.persist;

public enum NrtmDocumentType {
    delta,
    snapshot;

    public String lowerName() {
        return name().toLowerCase();
    }
}
