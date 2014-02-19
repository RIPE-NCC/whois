package net.ripe.db.whois.api.rest.compare;

import net.ripe.db.whois.query.endtoend.compare.ComparisonConfiguration;

public class RestExecutorConfiguration implements ComparisonConfiguration {

    public static RestExecutorConfiguration DEV1 = new RestExecutorConfiguration("dev1", "dbc-dev1", 1080, 1081);
    public static RestExecutorConfiguration DEV2 = new RestExecutorConfiguration("dev2", "dbc-dev2", 1080, 1081);
    public static RestExecutorConfiguration PRE1 = new RestExecutorConfiguration("pre1", "dbc-pre1", 1080, 1081);
    public static RestExecutorConfiguration PRE2 = new RestExecutorConfiguration("pre2", "dbc-pre2", 1080, 1081);

    final String identifier;
    final String host;
    final int ripePort;
    final int testPort;

    public RestExecutorConfiguration(String identifier, String host, int ripePort, int testPort) {
        this.identifier = identifier;
        this.host = host;
        this.ripePort = ripePort;
        this.testPort = testPort;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getHost() {
        return host;
    }

    public int getRipePort() {
        return ripePort;
    }

    public int getTestPort() {
        return testPort;
    }
}
