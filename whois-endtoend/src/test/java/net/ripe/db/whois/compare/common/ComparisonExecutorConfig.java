package net.ripe.db.whois.compare.common;

import net.ripe.db.whois.common.support.QueryExecutorConfiguration;

public class ComparisonExecutorConfig extends QueryExecutorConfiguration {
    public enum ResponseFormat {COMPACT, DEFAULT}

    public static ComparisonExecutorConfig PRE1 = new ComparisonExecutorConfig("db-pre-1", 1043, 1044, 1080, 1081, ResponseFormat.DEFAULT);
    public static ComparisonExecutorConfig PRE2 = new ComparisonExecutorConfig("db-pre-2", 1043, 1044, 1080, 1081, ResponseFormat.DEFAULT);

    final int ripePort;
    final int testPort;
    final ResponseFormat responseFormat;

    public ComparisonExecutorConfig(final String host, final int queryPort, final int nrtmPort,
                                    final int ripePort, final int testPort, final ResponseFormat responseFormat) {
        super(host, host, queryPort, nrtmPort);
        this.ripePort = ripePort;
        this.testPort = testPort;
        this.responseFormat = responseFormat;
    }

    public int getRipePort() {
        return ripePort;
    }

    public int getTestPort() {
        return testPort;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }
}
