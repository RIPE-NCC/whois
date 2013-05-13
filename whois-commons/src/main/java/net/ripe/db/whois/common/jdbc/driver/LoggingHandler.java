package net.ripe.db.whois.common.jdbc.driver;

public interface LoggingHandler {
    boolean canLog();

    void log(StatementInfo statementInfo, ResultInfo resultInfo);
}
