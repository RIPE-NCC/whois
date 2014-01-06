package net.ripe.db.whois.common.jdbc.driver;

public interface LoggingHandler {
    void log(StatementInfo statementInfo, ResultInfo resultInfo);
}
