package net.ripe.db.whois.common;

import javax.annotation.Nullable;
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

    protected String formatMessage(final String text, final Object[] args) {
        return args.length == 0 ? text : String.format(text, args);
    }

    @Override
    public String toString() {
        return formattedText;
    }

    @Override
    public boolean equals(final Object o) {
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

    @Nullable
    public Messages.Type getType() {
        return type;
    }

    @Nullable
    public String getFormattedText() {
        return formattedText;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public Object[] getArgs() {
        return args;
    }
}
