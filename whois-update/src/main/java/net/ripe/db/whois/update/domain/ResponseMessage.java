package net.ripe.db.whois.update.domain;

public class ResponseMessage {
    private final String subject;
    private final String message;

    public ResponseMessage(final String subject, final String message) {
        this.subject = subject;
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }
}
