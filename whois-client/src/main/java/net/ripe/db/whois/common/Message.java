package net.ripe.db.whois.common;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Message {

    protected Messages.Type type;
    protected String text;
    protected Object[] args;
    protected String formattedText;

    protected Message() {}

    public Message(final Messages.Type type, final String text, final Object... args) {
        this.type = type;
        this.text = text;
        this.args = args;
        this.formattedText = formatMessage(text, args);
    }

    protected String formatMessage(String text, Object[] args) {
        return args.length == 0 ? text : String.format(text, args);
    }

    @Override
    public String toString() {
        return formattedText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Message message = (Message) o;
        return type == message.type && formattedText.equals(message.formattedText);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + formattedText.hashCode();
        return result;
    }

    public Messages.Type getType() {
        return type;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public String getText() {
        return text;
    }

    public Object[] getArgs() {
        return args;
    }
}
