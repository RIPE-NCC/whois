package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.domain.ResponseMessage;

public interface MailGateway {
    void sendEmail(String to, ResponseMessage responseMessage);

    void sendEmail(String to, String subject, String text);
}
