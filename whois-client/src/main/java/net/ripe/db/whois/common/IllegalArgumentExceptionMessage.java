package net.ripe.db.whois.common;

public class IllegalArgumentExceptionMessage extends IllegalArgumentException {
    protected final Message message;

    public IllegalArgumentExceptionMessage(Message message) {
        super(message.getFormattedText());
        this.message = message;
    }

    public Message getExceptionMessage() {
        return message;
    }
}
