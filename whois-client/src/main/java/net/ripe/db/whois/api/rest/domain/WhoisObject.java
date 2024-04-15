package net.ripe.db.whois.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.Collections;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "type",
        "link",
        "source",
        "primaryKey",
        "attributes",
        "resourceHolder",
        "abuseContact",
        "managed",
        "objectMessages",
})
@JsonInclude(NON_EMPTY)
@XmlRootElement(name = "object")
public class WhoisObject {

    @XmlElement
    private Link link;

    @XmlElement
    private Source source;

    @XmlElement(name = "primary-key")
    private PrimaryKey primaryKey;

    @XmlElement(name = "attributes", required = true)
    private Attributes attributes;

    @XmlElement(name = "resource-holder")
    private ResourceHolder resourceHolder;

    @XmlElement(name = "abuse-contact")
    private AbuseContact abuseContact;

    @XmlElement(name = "managed")
    private Boolean managed;

    @XmlAttribute(required = true)
    private String type;

    @XmlAttribute
    private Action action;

    @XmlAttribute
    private Integer version;

    @XmlElement(name = "objectmessages")
    private ObjectMessages objectMessages;

    public WhoisObject() {
        // required no-arg constructor
    }

    private WhoisObject(
            final Link link,
            final Source source,
            final PrimaryKey primaryKey,
            final Attributes attributes,
            final String type,
            final Action action,
            final Integer version,
            final ResourceHolder resourceHolder,
            final AbuseContact abuseContact,
            final Boolean managed,
            final ObjectMessages objectMessages) {
        this.link = link;
        this.source = source;
        this.primaryKey = primaryKey;
        this.attributes = attributes;
        this.type = type;
        this.action = action;
        this.version = version;
        this.resourceHolder = resourceHolder;
        this.abuseContact = abuseContact;
        this.managed = managed;
        this.objectMessages = objectMessages;
    }



    // builder

    public static class Builder {
        private Link link;
        private Source source;
        private PrimaryKey primaryKey;
        private Attributes attributes;
        private String type;
        private Action action;
        private Integer version;
        private ResourceHolder resourceHolder;
        private AbuseContact abuseContact;
        private Boolean managed;

        private ObjectMessages objectMessages;

        public Builder link(final Link link) {
            this.link = link;
            return this;
        }

        public Builder source(final Source source) {
            this.source = source;
            return this;
        }

        public Builder primaryKey(final PrimaryKey primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public Builder primaryKey(final List<Attribute> attributes) {
            this.primaryKey = new PrimaryKey(attributes);
            return this;
        }

        public Builder attributes(final Attributes attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder attributes(final List<Attribute> attributes) {
            this.attributes = new Attributes(attributes);
            return this;
        }

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder action(final Action action) {
            this.action = action;
            return this;
        }

        public Builder version(final Integer version) {
            this.version = version;
            return this;
        }

        public Builder resourceHolder(final ResourceHolder resourceHolder) {
            this.resourceHolder = resourceHolder;
            return this;
        }

        public Builder abuseContact(final AbuseContact abuseContact) {
            this.abuseContact = abuseContact;
            return this;
        }

        public Builder managed(final Boolean managed) {
            this.managed = managed;
            return this;
        }

        public Builder objectMessages(final ObjectMessages objectMessages) {
            this.objectMessages = objectMessages;
            return this;
        }

        public WhoisObject build() {
            return new WhoisObject(
                    link,
                    source,
                    primaryKey,
                    attributes,
                    type,
                    action,
                    version,
                    resourceHolder,
                    abuseContact,
                    managed,
                    objectMessages);
        }
    }

    // getters & setters

    public Link getLink() {
        return link;
    }

    public void setLink(final Link value) {
        this.link = value;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(final Source value) {
        this.source = value;
    }

    public List<Attribute> getPrimaryKey() {
        return primaryKey != null ? primaryKey.getAttributes() : Collections.<Attribute>emptyList();
    }

    public void setPrimaryKey(final List<Attribute> value) {
        this.primaryKey = new PrimaryKey(value);
    }

    public List<Attribute> getAttributes() {
        return attributes != null ? attributes.getAttributes() : Collections.<Attribute>emptyList();
    }

    public void setAttributes(final List<Attribute> value) {
        this.attributes = new Attributes(value);
    }

    public String getType() {
        return type;
    }

    public void setType(final String value) {
        this.type = value;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public ResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    public void setResourceHolder(final ResourceHolder resourceHolder) {
        this.resourceHolder = resourceHolder;
    }

    public AbuseContact getAbuseContact() {
        return abuseContact;
    }

    public void setAbuseContact(final AbuseContact abuseContact) {
        this.abuseContact = abuseContact;
    }

    public void setObjectMessages(final ObjectMessages objectMessages) {
        this.objectMessages = objectMessages;
    }

    public ObjectMessages getObjectMessages() {
        return objectMessages != null ? objectMessages : new ObjectMessages();
    }

    public Boolean isManaged() {
        return managed;
    }

    public void setManaged(final Boolean managed) {
        this.managed = managed;
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (Attribute attribute : getAttributes()) {
            builder.append(attribute.toString()).append('\n');
        }
        return builder.toString();
    }

}
