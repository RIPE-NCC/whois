package net.ripe.db.whois.update.log;

import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Used to allow LoggingDriver from whois-commons to access LoggerContext from whois-update
 */
@Component
public class LoggingHandlerAdapter implements LoggingHandler {
    private LoggerContext loggerContext;

    @Autowired
    public LoggingHandlerAdapter(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public void log(final StatementInfo statementInfo, final ResultInfo resultInfo) {
        loggerContext.logQuery(statementInfo, resultInfo);
    }
}
