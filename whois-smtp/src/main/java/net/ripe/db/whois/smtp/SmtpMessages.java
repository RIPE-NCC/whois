package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.Hosts;

public class SmtpMessages {

    private SmtpMessages() {
        // do not instantiate
    }

    public static Message banner(final String applicationVersion) {
        // 220 mail.ripe.net ESMTP Exim 4.98 Thu, 13 Mar 2025 13:41:12 +0000
        return new Message(Messages.Type.INFO, "220 %s SMTP Whois %s", Hosts.getInstanceName(), applicationVersion);
    }

    public static Message internalError() {
        return new Message(Messages.Type.ERROR, "internal error occurred.");
    }
}
