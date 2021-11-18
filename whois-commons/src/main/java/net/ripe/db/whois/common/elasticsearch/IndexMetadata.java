package net.ripe.db.whois.common.elasticsearch;

public class IndexMetadata {
    private final Integer serial;
    private final String source;

    public IndexMetadata(Integer serial, String source) {
        this.serial = serial;
        this.source = source;
    }

    public Integer getSerial() {
        return serial;
    }

    public String getSource() {
        return source;
    }
}
