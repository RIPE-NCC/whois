package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.domain.ResponseMessage;

import java.util.Set;


public interface MailGateway {
    void sendEmail(String to, ResponseMessage responseMessage);
    void sendEmail(String to, String subject, String text, String replyTo);
    void sendEmail(Set<String> to, String subject, String text, String replyTo, boolean html); //Used by whois-internal
}
