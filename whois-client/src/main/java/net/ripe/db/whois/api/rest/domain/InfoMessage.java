package net.ripe.db.whois.api.rest.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.common.Message;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "infoMessage")
public class InfoMessage extends WhoisResponseMessage {

    public InfoMessage(){super();}
    public InfoMessage(final Message message) {
        super(message);
    }

}
