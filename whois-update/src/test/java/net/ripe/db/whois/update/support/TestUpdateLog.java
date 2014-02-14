package net.ripe.db.whois.update.support;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.Stub;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.log.UpdateLog;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile({WhoisProfile.ENDTOEND, WhoisProfile.TEST})
public class TestUpdateLog extends UpdateLog implements Stub {

    List<String> messages = new ArrayList<>();

    @Override
    public void logUpdateResult(final UpdateRequest updateRequest, final UpdateContext updateContext, final Update update, final Stopwatch stopwatch) {
        messages.add(super.formatMessage(updateRequest, updateContext, update, stopwatch));
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
