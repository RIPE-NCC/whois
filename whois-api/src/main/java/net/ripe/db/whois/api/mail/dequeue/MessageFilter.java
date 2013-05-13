package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageFilter {
    private final LoggerContext loggerContext;

    @Autowired
    public MessageFilter(final LoggerContext loggerContext) {
        this.loggerContext = loggerContext;
    }

    public boolean shouldProcess(final MailMessage message) {
        String replyTo = message.getReplyTo();
        if (replyTo == null || !replyTo.contains("@")) {
            loggerContext.log(new Message(Messages.Type.INFO, "Not processing message, invalid reply address: %s", replyTo));
            return false;
        }

        replyTo = replyTo.toLowerCase();
        if (replyTo.contains("localhost") || replyTo.contains("127.0.0.1")) {
            loggerContext.log(new Message(Messages.Type.INFO, "Not processing message, reply to localhost: %s", replyTo));
            return false;
        }

        return true;
    }
}
