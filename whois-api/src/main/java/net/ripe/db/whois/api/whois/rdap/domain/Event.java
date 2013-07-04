package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;

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
    protected XMLGregorianCalendar eventDate;
    protected String eventActor;

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(String value) {
        this.eventAction = value;
    }

    public XMLGregorianCalendar getEventDate() {
        return eventDate;
    }

    public void setEventDate(XMLGregorianCalendar value) {
        this.eventDate = value;
    }

    public String getEventActor() {
        return eventActor;
    }

    public void setEventActor(String value) {
        this.eventActor = value;
    }
}
