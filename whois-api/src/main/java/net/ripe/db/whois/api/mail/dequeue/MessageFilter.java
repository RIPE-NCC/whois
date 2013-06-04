package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.validator.routines.EmailValidator;
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
        String replyToEmail = message.getReplyToEmail();
        if (replyToEmail == null) {
            loggerContext.log(new Message(Messages.Type.INFO, "Not processing message, missing reply address"));
            return false;
        }

        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(replyToEmail)) {
            loggerContext.log(new Message(Messages.Type.INFO, "Not processing message, reply address invalid: %s", replyToEmail));
            return false;
        }

        replyToEmail = replyToEmail.toLowerCase();
        if (replyToEmail.endsWith("localhost") || replyToEmail.endsWith("127.0.0.1")) {
            loggerContext.log(new Message(Messages.Type.INFO, "Not processing message, reply to localhost: %s", replyToEmail));
            return false;
        }

        return true;
    }
}
