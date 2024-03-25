package net.ripe.db.whois.api.mail;

import java.util.List;

public record EmailMessageInfo(List<String> emailAddresses, String messageId) {
}
