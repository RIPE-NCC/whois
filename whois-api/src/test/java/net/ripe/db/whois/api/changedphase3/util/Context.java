package net.ripe.db.whois.api.changedphase3.util;

import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;

public class Context {
    private int restPort;
    private int syncUpdatePort;
    private WhoisObjectMapper whoisObjectMapper;
    private boolean debug = true;

    public Context(int restPort, int syncUpdatePort, WhoisObjectMapper whoisObjectMapper) {
        this.restPort = restPort;
        this.syncUpdatePort = syncUpdatePort;
        this.whoisObjectMapper = whoisObjectMapper;
    }

    public int getRestPort() {
        return restPort;
    }

    public int getSyncUpdatePort() {
        return syncUpdatePort;
    }

    public WhoisObjectMapper getWhoisObjectMapper() {
        return whoisObjectMapper;
    }

    public boolean isDebug() {
        return debug;
    }

}
