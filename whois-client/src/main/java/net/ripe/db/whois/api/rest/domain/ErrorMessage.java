package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "errormessage")
@JsonInclude(NON_EMPTY)
public class ErrorMessage extends WhoisMessage {

    public ErrorMessage() {
        super();
    }

    ErrorMessage(final Attribute attribute, final String text, final List<Arg> args) {
        super("Error", attribute, text, args);
    }

    public ErrorMessage(final Message message) {
        super(message);
    }

    public ErrorMessage(final Message message, final RpslAttribute attribute) {
        super(message, attribute);
    }

}
