package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "errormessage")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ErrorMessage {

    @XmlAttribute(required = true)
    protected String severity;
    @XmlElement
    protected Attribute attribute;
    @XmlAttribute(required = true)
    protected String text;
    @XmlElement
    protected List<Arg> args;

    public ErrorMessage(String severity, Attribute attribute, String text, List<Arg> args) {
        this.severity = severity;
        this.attribute = attribute;
        this.text = text;
        this.args = args;
    }

    public ErrorMessage() {
    }

    public ErrorMessage(Message message) {
        this.severity = message.getType().toString();
        this.attribute = null;
        this.text = message.getText();
        this.args = Lists.newArrayList();
        for (Object arg : message.getArgs()) {
            this.args.add(new Arg(arg.toString()));
        }
    }

    public ErrorMessage(Message message, RpslAttribute attribute) {
        this(message);
        this.attribute = new Attribute(attribute.getKey(), attribute.getValue());
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
}
