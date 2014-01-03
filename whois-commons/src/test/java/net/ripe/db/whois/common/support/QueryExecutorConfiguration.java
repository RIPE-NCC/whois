package net.ripe.db.whois.common.support;

public class QueryExecutorConfiguration {
    public static QueryExecutorConfiguration DEV1 = new QueryExecutorConfiguration("dev1", "dbc-dev1", 1043, 1044);
    public static QueryExecutorConfiguration DEV2 = new QueryExecutorConfiguration("dev2", "dbc-dev2", 1043, 1044);
    public static QueryExecutorConfiguration PRE1 = new QueryExecutorConfiguration("pre1", "dbc-pre1", 1043, 1044);
    public static QueryExecutorConfiguration PRE2 = new QueryExecutorConfiguration("pre2", "dbc-pre2", 1043, 1044);

    final String identifier;
    final String host;
    final int queryPort;
    final int nrtmPort;

    public QueryExecutorConfiguration(String identifier, String host, int queryPort, int nrtmPort) {
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
