package net.ripe.db.whois.api.changedphase3;

import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;

public class Context {
    private int restPort;
    private WhoisObjectMapper whoisObjectMapper;

    public Context(int restPort, WhoisObjectMapper whoisObjectMapper) {
        this.restPort = restPort;
        this.whoisObjectMapper = whoisObjectMapper;
    }

    public int getRestPort() {
        return restPort;
    }

    public WhoisObjectMapper getWhoisObjectMapper() {
        return whoisObjectMapper;
    }
}
