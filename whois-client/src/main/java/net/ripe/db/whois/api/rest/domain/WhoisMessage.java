package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public abstract class WhoisMessage implements Comparable<WhoisMessage>{

    @XmlAttribute(required = true)
    protected String severity;          // TODO: severity should be enum
    @XmlElement
    protected Attribute attribute;
    @XmlAttribute(required = true)
    protected String text;
    @XmlElement
    protected List<Arg> args;

    public WhoisMessage(final String severity, final Attribute attribute, final String text, final List<Arg> args) {
        this.severity = severity;
        this.attribute = attribute;
        this.text = text;
        this.args = args;
    }

    public WhoisMessage(final Message message) {
        this.severity = message.getType().toString();
        this.attribute = null;
        this.text = message.getText();
        this.args = Lists.newArrayList();
        for (Object arg : message.getArgs()) {
            this.args.add(new Arg(arg.toString()));
        }
    }

    public WhoisMessage(final Message message, final RpslAttribute attribute) {
        this(message);
        this.attribute = new Attribute(attribute.getKey(), attribute.getValue());
    }

    public WhoisMessage() {
        this.args = Lists.newArrayList();
    }

    @Nullable
    public String getSeverity() {
        return severity;
    }

    @Nullable
    public Attribute getAttribute() {
        return attribute;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public List<Arg> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return (args == null || args.isEmpty() || text == null) ?
                text :
                String.format(text, args.toArray());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || (o.getClass() != getClass())) {
            return false;
        }

        final ErrorMessage errorMessage = (ErrorMessage)o;

        return (Objects.equals(severity, errorMessage.getSeverity()) &&
                Objects.equals(attribute, errorMessage.getAttribute()) &&
                Objects.equals(text, errorMessage.getText()) &&
                Objects.equals(args, errorMessage.getArgs()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, attribute, text, args);
    }

    @Override
    public int compareTo(final WhoisMessage errorMessage) {
        final Messages.Type thisType = Messages.Type.valueOf(severity.toUpperCase());
        final Messages.Type otherType = Messages.Type.valueOf(errorMessage.getSeverity().toUpperCase());
        return thisType.compareTo(otherType);
    }
}
