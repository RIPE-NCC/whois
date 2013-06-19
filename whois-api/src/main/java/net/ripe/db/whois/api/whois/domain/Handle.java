package net.ripe.db.whois.api.whois.domain;

import net.ripe.db.whois.common.domain.CIString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "handle")
@XmlAccessorType(XmlAccessType.FIELD)
public class Handle {

    protected CIString handle;

    public Handle(final CIString handle) {
        this.handle = handle;
    }

    public Handle() {
        // required no-arg constructor
    }
}
