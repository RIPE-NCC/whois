package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.GregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "event", propOrder = {
    "eventAction",
    "eventDate",
    "eventActor"
})
public class Event
    implements Serializable
{
    protected String eventAction;
    @XmlSchemaType(name = "dateTime")
    protected GregorianCalendar eventDate;
    protected String eventActor;

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String value) {
        this.eventAction = value;
    }

    public GregorianCalendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(GregorianCalendar value) {
        this.eventDate = value;
    }

    public String getEventActor() {
        return eventActor;
    }

    public void setEventActor(String value) {
        this.eventActor = value;
    }
}
