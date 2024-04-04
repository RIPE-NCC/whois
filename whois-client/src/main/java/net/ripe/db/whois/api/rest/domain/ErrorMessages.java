package net.ripe.db.whois.api.rest.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@XmlRootElement(name = "errormessages")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(NON_EMPTY)
public class ErrorMessages {

    @XmlElement(name = "errormessage")
    protected List<ErrorMessage> errorMessages;

    public ErrorMessages(final List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public ErrorMessages() {
        errorMessages = Lists.newArrayList();
    }
    public void addMessage(final ErrorMessage error) {
        errorMessages.add(error);
    }

    public void setMessages(final List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public List<ErrorMessage> getMessages() {
        return errorMessages;
    }
}
