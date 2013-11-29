package net.ripe.db.whois.api.rest.domain;


import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "errormessages")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorMessages {

    @XmlElement(name = "errormessages")
    protected List<ErrorMessage> errorMessages = Lists.newArrayList();

    public void addErrorMessage(ErrorMessage error) {
        errorMessages.add(error);
    }

    public ErrorMessages() {
        // required no-arg constructor
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }
}
