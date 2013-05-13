package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.update.domain.ResponseMessage;
import org.springframework.mail.MailSendException;

public interface MailGateway {
    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    void sendEmail(String to, ResponseMessage responseMessage);

    @RetryFor(value = MailSendException.class, attempts = 20, intervalMs = 10000)
    void sendEmail(String to, String subject, String text);
}
