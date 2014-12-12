package net.ripe.db.whois.query.handler;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.InetAddress;

@Component
@Profile(WhoisProfile.DEPLOYED)
public class WhoisLog {
    private final Logger logger = LoggerFactory.getLogger(WhoisLog.class);

    protected String formatMessage(final String api, final int personalObjects, final int nonPersonalObjects, @Nullable final QueryCompletionInfo completionInfo, final long executionTime, @Nullable final InetAddress remoteAddress, final Integer channelId, final String queryString) {
        return MessageFormatter.arrayFormat("{} PW-{}-INFO <{}+{}+0> {} {}ms [{}] --  {}",
                new Object[]{
                        String.format("%10d", channelId),
                        api,
                        personalObjects,
                        nonPersonalObjects,
                        completionInfo == null ? "" : completionInfo.name(),
                        executionTime,
                        remoteAddress != null ? remoteAddress.getHostAddress() : "NONE",
                        queryString
                }).getMessage();
    }

    public void logQueryResult(final String api, final int personalObjects, final int nonPersonalObjects, @Nullable final QueryCompletionInfo completionInfo, final long executionTime, @Nullable final InetAddress remoteAddress, final Integer channelId, final String queryString) {
        logger.info(formatMessage(api, personalObjects, nonPersonalObjects, completionInfo, executionTime, remoteAddress, channelId, queryString));
    }
}
