package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "errormessage")
@JsonInclude(NON_EMPTY)
public class ErrorMessage implements Comparable<ErrorMessage> {
    @XmlAttribute(required = true)
    private String severity;          // TODO: severity should be enum
    @XmlElement
    private Attribute attribute;
    @XmlAttribute(required = true)
    private String text;
    @XmlElement
    private List<Arg> args;

    ErrorMessage(final String severity, final Attribute attribute, final String text, final List<Arg> args) {
        this.severity = severity;
        this.attribute = attribute;
        this.text = text;
        this.args = args;
    }

    public ErrorMessage(final Message message) {
        this.severity = message.getType().toString();
        this.attribute = null;
        this.text = message.getText();
        this.args = Lists.newArrayList();
        for (Object arg : message.getArgs()) {
            this.args.add(new Arg(arg.toString()));
        }
    }

    public ErrorMessage(final Message message, final RpslAttribute attribute) {
        this(message);
        this.attribute = new Attribute(attribute.getKey(), attribute.getValue());
    }

    public ErrorMessage() {
        this.args = Lists.newArrayList();
    }

    public String getSeverity() {
        return severity;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getText() {
        return text;
    }

    public List<Arg> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return (args.isEmpty()) ? text : String.format(text, args.toArray());
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
    public int compareTo(final ErrorMessage errorMessage) {
        final Messages.Type thisType = Messages.Type.valueOf(severity.toUpperCase());
        final Messages.Type otherType = Messages.Type.valueOf(errorMessage.getSeverity().toUpperCase());
        return thisType.compareTo(otherType);
    }
}
