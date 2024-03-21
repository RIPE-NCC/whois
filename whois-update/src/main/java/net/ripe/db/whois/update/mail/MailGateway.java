package net.ripe.db.whois.update.mail;

import net.ripe.db.whois.update.domain.ResponseMessage;

import java.util.Set;


public interface MailGateway {
    void sendEmail(String to, ResponseMessage responseMessage);
    void sendEmail(String to, String subject, String text, String replyTo);
    void sendHtmlEmail(final Set<String> recipients, final String subject, final String text); //Used by whois-internal

    void sendEmail(final Set<String> recipients, final String subject, final String text); //Used by whois-internal
}
