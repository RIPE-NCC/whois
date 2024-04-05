package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlRootElement(name = "objectMessages")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(NON_EMPTY)
public class ObjectMessages {
    @XmlElement(name = "objectMessage")
    protected List<ObjectMessage> infoMessages;
    public ObjectMessages() {
        infoMessages = Lists.newArrayList();
    }

    public ObjectMessages(final List<ObjectMessage> infoMessages) {
        this.infoMessages = infoMessages;
    }

    public void addMessage(final ObjectMessage infoMessage){
        infoMessages.add(infoMessage);
    }

    public void setMessages(final List<ObjectMessage> infoMessages) {
        this.infoMessages = infoMessages;
    }

    public List<ObjectMessage> getMessages() {
        return infoMessages;
    }
}
