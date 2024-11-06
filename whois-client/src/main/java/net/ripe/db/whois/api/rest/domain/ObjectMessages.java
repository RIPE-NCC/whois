package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlRootElement(name = "objectmessages")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(NON_EMPTY)
public class ObjectMessages {
    @XmlElement(name = "objectmessage")
    protected List<RpslMessage> objectMessages;
    public ObjectMessages() {
        objectMessages = Lists.newArrayList();
    }

    public ObjectMessages(final List<RpslMessage> objectMessages) {
        this.objectMessages = objectMessages;
    }

    public void addMessage(final RpslMessage objectMessage){
        objectMessages.add(objectMessage);
    }

    public void setMessages(final List<RpslMessage> objectMessages) {
        this.objectMessages = objectMessages;
    }

    public List<RpslMessage> getMessages() {
        return objectMessages;
    }
}
