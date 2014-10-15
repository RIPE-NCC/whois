package net.ripe.db.whois.api.rest.compare;

import net.ripe.db.whois.query.endtoend.compare.ComparisonConfiguration;

public class RestExecutorConfiguration implements ComparisonConfiguration {
    public enum ResponseFormat {COMPACT, DEFAULT}

    public static RestExecutorConfiguration PRE1 = new RestExecutorConfiguration("db-pre-1", 1080, 1081, ResponseFormat.DEFAULT);
    public static RestExecutorConfiguration PRE2 = new RestExecutorConfiguration("db-pre-2", 1080, 1081, ResponseFormat.DEFAULT);

    final String host;
    final int ripePort;
    final int testPort;
    final ResponseFormat responseFormat;

    public RestExecutorConfiguration(final String host, final int ripePort, final int testPort, final ResponseFormat responseFormat) {
        this.host = host;
        this.ripePort = ripePort;
        this.testPort = testPort;
        this.responseFormat = responseFormat;
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

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }
}
