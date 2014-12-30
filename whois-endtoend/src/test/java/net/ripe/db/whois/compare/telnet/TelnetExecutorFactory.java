package net.ripe.db.whois.compare.telnet;

import net.ripe.db.whois.common.support.QueryExecutorConfiguration;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.compare.common.ComparisonExecutor;
import net.ripe.db.whois.compare.common.TargetInterface;

import java.net.UnknownHostException;

public class TelnetExecutorFactory {

    public ComparisonExecutor createTelnetExecutor(final TargetInterface targetInterface,
                                                 final QueryExecutorConfiguration configuration) throws UnknownHostException {

        if (targetInterface == TargetInterface.NRTM) {
            return new NrtmQueryExecutor(configuration);
        } else if (targetInterface == TargetInterface.WHOIS) {
            return new WhoisQueryExecutor(configuration);
        } else {
            throw new UnsupportedOperationException("This executor should only be used for telnet-like operation");
        }
    }

    static class WhoisQueryExecutor extends TelnetQueryExecutor {
        public WhoisQueryExecutor (final QueryExecutorConfiguration configuration) throws UnknownHostException {
            super(configuration, new TelnetWhoisClient(configuration.getHost(), configuration.getQueryPort()));
        }
    }

    static class NrtmQueryExecutor extends TelnetQueryExecutor {
        public NrtmQueryExecutor (final QueryExecutorConfiguration configuration) throws UnknownHostException {
            super(configuration, new TelnetWhoisClient(configuration.getHost(), configuration.getNrtmPort()));
        }
    }
}
