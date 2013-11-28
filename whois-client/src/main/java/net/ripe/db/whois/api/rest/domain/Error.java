package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "error")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Error {

    @XmlAttribute(required = true)
    protected String severity;
    @XmlElement
    protected Attribute attribute;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute(required = true)
    protected String text;
    @XmlElement
    protected List<Arg> args;

    public Error(String severity, Attribute attribute, String name, String text, List<Arg> args) {
        this.severity = severity;
        this.attribute = attribute;
        this.name = name;
        this.text = text;
        this.args = args;
    }

    public Error() {
    }

    public String getSeverity() {
        return severity;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public List<Arg> getArgs() {
        return args;
    }
}
