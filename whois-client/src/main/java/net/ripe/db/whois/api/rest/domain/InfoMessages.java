package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlRootElement(name = "infoMessages")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(NON_EMPTY)
public class InfoMessages {
    @XmlElement(name = "infoMessage")
    protected List<InfoMessage> infoMessages;
    public InfoMessages() {
        infoMessages = Lists.newArrayList();
    }

    public InfoMessages(final List<InfoMessage> infoMessages) {
        this.infoMessages = infoMessages;
    }

    public void addMessage(final InfoMessage infoMessage){
        infoMessages.add(infoMessage);
    }

    public void setMessages(final List<InfoMessage> infoMessages) {
        this.infoMessages = infoMessages;
    }

    public List<InfoMessage> getMessages() {
        return infoMessages;
    }
}
