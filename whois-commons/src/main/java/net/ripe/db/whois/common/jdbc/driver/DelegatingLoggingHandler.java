package net.ripe.db.whois.common.jdbc.driver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DelegatingLoggingHandler implements LoggingHandler {
    public interface Delegate extends LoggingHandler {
    }

    private Delegate delegate;

    @Autowired(required = false)
    void setDelegate(final Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean canLog() {
        return delegate != null && delegate.canLog();
    }

    @Override
    public void log(final StatementInfo statementInfo, final ResultInfo resultInfo) {
        delegate.log(statementInfo, resultInfo);
    }
}
