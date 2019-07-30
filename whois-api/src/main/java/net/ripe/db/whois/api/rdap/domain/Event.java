package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "event", propOrder = {
    "eventAction",
    "eventDate",
    "eventActor"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public class Event implements Serializable {
    @XmlElement(required = true)
    protected Action eventAction;
    @XmlSchemaType(name = "dateTime")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @XmlElement(required = true)
    protected LocalDateTime eventDate;
    @XmlElement(required = false)
    protected String eventActor;

    public Action getEventAction() {
        return eventAction;
    }

    public void setEventAction(final Action value) {
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

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        return Objects.equals(((Event)object).eventAction, eventAction) &&
            Objects.equals(((Event)object).eventDate, eventDate) &&
            Objects.equals(((Event)object).eventActor, eventActor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventAction, eventDate, eventActor);
    }
}
