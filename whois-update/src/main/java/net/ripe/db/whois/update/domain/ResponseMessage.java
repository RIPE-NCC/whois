package net.ripe.db.whois.update.domain;

import java.util.Optional;

public class ResponseMessage {
    private final String subject;
    private final String message;
    private final Optional<String> replyTo;

    public ResponseMessage(final String subject, final String message, final Optional<String> replyTo) {
        this.subject = subject;
        this.message = message;
        this.replyTo = replyTo;
    }

    public ResponseMessage(final String subject, final String message) {
        this(subject, message, Optional.empty());
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public Optional<String> getReplyTo() {
        return replyTo;
    }
}
