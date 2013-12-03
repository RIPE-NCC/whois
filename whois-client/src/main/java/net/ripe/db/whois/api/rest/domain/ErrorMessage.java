package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "errormessage")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ErrorMessage {
    private static final Pattern BEGINNING_OF_QUERY_ERROR_MESSAGES = Pattern.compile("^(%+)(?:ERROR|WARNING):");
    private static final Pattern BEGINNING_OF_LINE_PERCENT_SIGNS = Pattern.compile("(?m)^%+ *");

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

        // TODO: instead of removing extra %/%% signs, we should make QueryMessages structured and add it where necessary
        Matcher matcher = BEGINNING_OF_QUERY_ERROR_MESSAGES.matcher(text);
        if (matcher.find()) {
            text = text.substring(matcher.group(1).length());
        }
        text = BEGINNING_OF_LINE_PERCENT_SIGNS.matcher(text).replaceAll("");
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

    @Override
    public String toString() {
        return String.format(text, args.toArray());
    }
}
