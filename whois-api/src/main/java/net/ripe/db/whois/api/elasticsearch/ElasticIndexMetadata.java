package net.ripe.db.whois.api.elasticsearch;

public class ElasticIndexMetadata {
    private final Integer serial;
    private final String source;

    public ElasticIndexMetadata(Integer serial, String source) {
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
