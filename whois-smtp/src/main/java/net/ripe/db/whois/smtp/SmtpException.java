package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.Message;

public class SmtpException extends RuntimeException {

    private final String message;

    public SmtpException(final Message message) {
        this.message = message.toString();
    }

    public SmtpException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
