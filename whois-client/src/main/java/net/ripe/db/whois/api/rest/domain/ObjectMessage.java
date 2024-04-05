package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.common.Message;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "objectMessage")
@JsonInclude(NON_EMPTY)
public class ObjectMessage extends WhoisMessage {

    public ObjectMessage(){
        super();
    }

    public ObjectMessage(final Message message) {
        super(message);
    }

}
