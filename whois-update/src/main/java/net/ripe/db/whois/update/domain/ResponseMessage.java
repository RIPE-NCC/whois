package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.Util;

public class ResponseMessage {

    private final String subject;
    private final String message;
    private final String replyTo;

    public ResponseMessage(final String subject, final String message, final String replyTo) {
        this.subject = subject;
        this.message = message;
        this.replyTo = replyTo;
    }

    public ResponseMessage(final String subject, final String message) {
        this(subject, message, Util.EMPTY_STRING);
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public String getReplyTo() {
        return replyTo;
    }
}
