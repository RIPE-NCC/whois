package net.ripe.db.whois.api.rest.domain;


import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "errors")
@XmlAccessorType(XmlAccessType.FIELD)
public class Errors {

    @XmlElement(name = "errors")
    protected List<Error> errors = Lists.newArrayList();

    public void addError(Error error) {
        errors.add(error);
    }

    public Errors() {
        // required no-arg constructor
    }

    public List<Error> geterrors() {
        return errors;
    }
}
