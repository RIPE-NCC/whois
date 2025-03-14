package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.Hosts;

import java.time.LocalDateTime;

public class SmtpMessages {

    private SmtpMessages() {
        // do not instantiate
    }

    public static Message banner(final String applicationVersion) {
        // 220 mail.ripe.net ESMTP Exim 4.98 Thu, 13 Mar 2025 13:41:12 +0000
        // 220 ESHRYANE-PRO SMTP Whois 0.1-TEST 2025-03-13T23:22:13.021523
        return new Message(Messages.Type.INFO, "220 %s SMTP Whois %s %s", Hosts.getInstanceName(), applicationVersion, LocalDateTime.now());
    }

    public static Message internalError() {
        return new Message(Messages.Type.ERROR, "internal error occurred.");
    }
}
