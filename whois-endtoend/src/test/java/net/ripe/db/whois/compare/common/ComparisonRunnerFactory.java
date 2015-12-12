package net.ripe.db.whois.compare.common;

import net.ripe.db.whois.compare.rest.RestComparisonRunner;
import net.ripe.db.whois.compare.rest.RestExecutor;
import net.ripe.db.whois.compare.telnet.TelnetComparisonRunner;
import net.ripe.db.whois.compare.telnet.TelnetExecutorFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.UnknownHostException;

public class ComparisonRunnerFactory {

    public ComparisonRunner createCompareResults(final ComparisonExecutorConfig config1,
                                               final ComparisonExecutorConfig config2,
                                               final QueryReader queryReader,
                                               final File targetDir,
                                               final TargetInterface targetInterface) throws UnknownHostException {

        if (targetInterface == TargetInterface.REST) {
            return new RestComparisonRunner(
                    queryReader,
                    targetDir,
                    new RestExecutor(config1),
                    new RestExecutor(config2));
        }

        final TelnetExecutorFactory telnetExecutorFactory = new TelnetExecutorFactory();

        if (targetInterface == TargetInterface.NRTM) {
            return new TelnetComparisonRunner(
                    queryReader,
                    targetDir,
                    telnetExecutorFactory.createTelnetExecutor(TargetInterface.NRTM, config1),
                    telnetExecutorFactory.createTelnetExecutor(TargetInterface.NRTM, config2),
                    LoggerFactory.getLogger("NrtmComparisonRunner"));

        } else if (targetInterface == TargetInterface.WHOIS) {
            return new TelnetComparisonRunner(
                    queryReader,
                    targetDir,
                    telnetExecutorFactory.createTelnetExecutor(TargetInterface.WHOIS, config1),
                    telnetExecutorFactory.createTelnetExecutor(TargetInterface.WHOIS, config2),
                    LoggerFactory.getLogger("WhoisComparisonRunner"));
        } else {
            throw new UnsupportedOperationException("This executor should only be used for REST/WHOIS/NRTM operations");
        }
    }
}
