package net.ripe.db.nrtm4.persist;

public class NrtmSource {

    private final String value;

    public NrtmSource(final String value) {
        this.value = value;
    }

    public String name() {
        return value;
    }
}
