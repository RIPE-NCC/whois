package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class Message {

    protected Messages.Type type;
    protected RpslAttribute rpslAttribute;
    protected String text;
    protected Object[] args;
    protected String formattedText;

    protected Message() {}

    public Message(final Messages.Type type, final String text, final Object... args) {
       this(type,null,text, args);
    }

    public Message(final Message message, final RpslAttribute rpslAttribute) {
        this(message.type,rpslAttribute,message.text, message.args);
    }

    public Message(final Messages.Type type, final RpslAttribute rpslAttribute, final String text, final Object... args) {
        this.type = type;
        this.text = text;
        this.args = args;
        this.formattedText = formatMessage(text, args);
        this.rpslAttribute = rpslAttribute;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Message message = (Message) o;

        return Objects.equals(type, message.type) &&
                Objects.equals(formattedText, message.formattedText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, formattedText);
    }

    @Nullable
    public Messages.Type getType() {
        return type;
    }

    @Nullable
    public RpslAttribute getRpslAttribute() {
        return rpslAttribute;
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
