package net.ripe.db.whois.api.mail;

import jakarta.mail.internet.MimeMessage;

import java.util.List;

public record EmailMessageInfo(List<String> emailAddresses, String messageId, MimeMessage message) {
}
