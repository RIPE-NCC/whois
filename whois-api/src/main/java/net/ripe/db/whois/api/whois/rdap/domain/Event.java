package net.ripe.db.whois.api.whois.rdap.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "event", propOrder = {
    "eventAction",
    "eventDate",
    "eventActor"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Event implements Serializable {
    protected String eventAction;
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    protected LocalDateTime eventDate;
    protected String eventActor;

    public String getEventAction() {
        return eventAction;
    }

    public void setEventAction(final String value) {
        this.eventAction = value;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(final LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventActor() {
        return eventActor;
    }

    public void setEventActor(final String value) {
        this.eventActor = value;
    }
}
