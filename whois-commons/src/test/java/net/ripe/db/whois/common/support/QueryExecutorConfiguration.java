package net.ripe.db.whois.common.support;

public class QueryExecutorConfiguration {
    public static QueryExecutorConfiguration DEV1 = new QueryExecutorConfiguration("dev1", "dbc-dev1", 1043);
    public static QueryExecutorConfiguration DEV2 = new QueryExecutorConfiguration("dev2", "dbc-dev2", 1043);
    public static QueryExecutorConfiguration PRE1 = new QueryExecutorConfiguration("pre1", "dbc-pre1", 1043);
    public static QueryExecutorConfiguration PRE2 = new QueryExecutorConfiguration("pre2", "dbc-pre2", 1043);

    final String identifier;
    final String host;
    final int port;

    public QueryExecutorConfiguration(String identifier, String host, int port) {
        this.identifier = identifier;
        this.host = host;
        this.port = port;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
