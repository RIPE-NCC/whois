package net.ripe.db.whois.api.whois.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class RdapVCardArray {

    @XmlElement(name = "vcardArray")
    protected List<RdapVCard> vCards;

    public void addVCard(RdapVCard vCard) {
        vCards.add(vCard);
    }

    public RdapVCardArray() {
        // required no-arg constructor
    }
}
