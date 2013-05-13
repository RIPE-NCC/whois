package net.ripe.db.whois.query.handler;

import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.Locale;

@Component
public class WhoisLog {
    private final Logger logger;

    public WhoisLog() {
        this(LoggerFactory.getLogger(WhoisLog.class));
    }

    public WhoisLog(Logger logger) {
        this.logger = logger;
    }

    public void logQueryResult(final String api, final int personalObjects, final int nonPersonalObjects, @Nullable final QueryCompletionInfo completionInfo, final long executionTime, @Nullable final InetAddress remoteAddress, final Integer channelId, final String queryString) {
        logger.info(
                "{} PW-{}-INFO <{}+{}+0> {} {}s [{}] --  {}",
                String.format("%10d", channelId),
                api,
                personalObjects,
                nonPersonalObjects,
                completionInfo == null ? "" : completionInfo.name(),
                formatExecutionTime(executionTime),
                remoteAddress != null ? remoteAddress.getHostAddress() : "NONE",
                queryString
        );
    }

    private String formatExecutionTime(long elapsedMillis) {
        return String.format((Locale) null, "%.2f", elapsedMillis / 1000.0);
    }
}
