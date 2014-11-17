package net.ripe.db.whois.common.support;

public class QueryExecutorConfiguration {
    final String identifier;
    final String host;
    final int queryPort;
    final int nrtmPort;

    public QueryExecutorConfiguration(final String identifier, final String host, final int queryPort, final int nrtmPort) {
        this.identifier = identifier;
        this.host = host;
        this.queryPort = queryPort;
        this.nrtmPort = nrtmPort;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getHost() {
        return host;
    }

    public int getQueryPort() {
        return queryPort;
    }

    public int getNrtmPort() {
        return nrtmPort;
    }
}
