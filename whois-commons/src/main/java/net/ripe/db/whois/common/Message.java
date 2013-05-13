package net.ripe.db.whois.common;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class Message {
    private final Messages.Type type;
    private final String value;

    public Message(final Messages.Type type, final String value, final Object... args) {
        this.type = type;
        this.value = prettyPrint(type, args.length == 0 ? value : String.format(value, args));
    }

    private static String prettyPrint(final Messages.Type type, final String value) {
        if (value.startsWith("%") || value.startsWith("#")) {
            return value;
        }

        return FormatHelper.prettyPrint("***" + type + ": ", value, 12, 80);
    }

    @Override
    public String toString() {
        return value;
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
        return type == message.type && value.equals(message.value);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    public Messages.Type getType() {
        return type;
    }
}
