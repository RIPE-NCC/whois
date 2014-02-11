package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile({WhoisProfile.ENDTOEND, WhoisProfile.TEST})
public class TestWhoisLog extends WhoisLog implements Stub {
    List<String> messages = new ArrayList<>();

    @Override
    public void logQueryResult(final String api, final int personalObjects, final int nonPersonalObjects, @Nullable final QueryCompletionInfo completionInfo, final long executionTime, @Nullable final InetAddress remoteAddress, final Integer channelId, final String queryString) {
        messages.add(MessageFormatter.arrayFormat(
                "{} PW-{}-INFO <{}+{}+0> {} {}ms [{}] --  {}",
                new Object[] {String.format("%10d", channelId),
                api,
                personalObjects,
                nonPersonalObjects,
                completionInfo == null ? "" : completionInfo.name(),
                executionTime,
                remoteAddress != null ? remoteAddress.getHostAddress() : "NONE",
                queryString
        }).getMessage());
    }

    @Override
    public void reset() {
        messages.clear();
    }

    public String getMessage(int index) {
        return messages.get(index);
    }

    public List<String> getMessages() {
        return messages;
    }
}
