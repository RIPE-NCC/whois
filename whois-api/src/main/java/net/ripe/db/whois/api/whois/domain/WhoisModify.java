package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "whois-modify")
public class WhoisModify {

    @XmlElement
    private Add add;
    @XmlElement
    private Remove remove;
    @XmlElement
    private Replace replace;

    public WhoisModify(final Add add) {
        this.add = add;
    }

    public WhoisModify(final Remove remove) {
        this.remove = remove;
    }

    public WhoisModify(final Replace replace) {
        this.replace = replace;
    }

    public WhoisModify() {
        // required no-arg constructor
    }

    public Add getAdd() {
        return add;
    }

    public Remove getRemove() {
        return remove;
    }

    public Replace getReplace() {
        return replace;
    }

    @XmlRootElement(name = "add")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Add {
        @XmlAttribute(required = false)
        protected int index = -1;
        @XmlElement(name = "attributes")
        protected Attributes attributes;

        public Add(final int index, final List<Attribute> attributes) {
            this.index = index;
            this.attributes = new Attributes(attributes);
        }

        public Add(final List<Attribute> attributes) {
            this.attributes = new Attributes(attributes);
        }

        public Add() {
            // required no-arg constructor
        }

        public int getIndex() {
            return index;
        }

        public List<Attribute> getAttributes() {
            return attributes != null ? attributes.attributes : null;
        }
    }

    @XmlRootElement(name = "remove")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Remove {
        @XmlAttribute(required = false)
        protected int index = -1;
        @XmlAttribute(name = "attribute-type", required = false)
        protected String attributeType;

        public Remove(final int index) {
            this.index = index;
        }

        public Remove(final String attributeType) {
            this.attributeType = attributeType;
        }

        public Remove() {
            // required no-arg constructor
        }

        public int getIndex() {
            return index;
        }

        public String getAttributeType() {
            return attributeType;
        }
    }

    @XmlRootElement(name = "replace")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Replace {
        @XmlAttribute(name = "attribute-type", required = false)
        @JsonProperty("@attribute-type")
        protected String attributeType;
        @XmlElement(name = "attributes")
        protected Attributes attributes;

        public Replace(String attributeType, final List<Attribute> attributes) {
            this.attributeType = attributeType;
            this.attributes = new Attributes(attributes);
        }

        public Replace() {
            // required no-arg constructor
        }

        public String getAttributeType() {
            return attributeType;
        }

        public List<Attribute> getAttributes() {
            return attributes.attributes;
        }
    }
}
