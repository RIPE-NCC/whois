package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.Message;

public class SmtpException extends RuntimeException {

    private final Message message;

    public SmtpException(final Message message) {
        this.message = message;
    }

    public Message message() {
        return message;
    }

    @Override
    public String getMessage() {
        return message.toString();
    }


}
