package net.ripe.db.whois.query.support;

import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.handler.WhoisLog;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile({WhoisProfile.TEST})
public class TestWhoisLog extends WhoisLog implements Stub {
    List<String> messages = new ArrayList<>();

    @Override
    public void logQueryResult(final String api, final int personalObjects, final int nonPersonalObjects, @Nullable final QueryCompletionInfo completionInfo, final long executionTime, @Nullable final InetAddress remoteAddress, final Integer channelId, final String queryString) {
        messages.add(formatMessage(api, personalObjects, nonPersonalObjects, completionInfo, executionTime, remoteAddress, channelId, queryString));
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
