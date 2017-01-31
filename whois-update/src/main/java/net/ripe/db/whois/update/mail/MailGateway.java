package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.domain.ResponseMessage;

import java.util.Optional;

public interface MailGateway {
    void sendEmail(String to, ResponseMessage responseMessage);
    void sendEmail(String to, ResponseMessage responseMessage, String replyTo);
    void sendEmail(String to, String subject, String text);
    void sendEmail(String to, String subject, String text, String replyTo);
}
