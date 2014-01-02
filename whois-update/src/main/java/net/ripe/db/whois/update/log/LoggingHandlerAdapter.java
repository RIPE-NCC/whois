package net.ripe.db.whois.update.log;

import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggingHandlerAdapter implements LoggingHandler {
    private LoggerContext loggerContext;

    public LoggingHandlerAdapter() {
    }

    @Autowired
    public LoggingHandlerAdapter(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    @Override
    public boolean canLog() {
        return loggerContext != null && loggerContext.canLog();
    }

    @Override
    public void log(final StatementInfo statementInfo, final ResultInfo resultInfo) {
        if (loggerContext != null) {
            loggerContext.logQuery(statementInfo, resultInfo);
        }
    }
}
