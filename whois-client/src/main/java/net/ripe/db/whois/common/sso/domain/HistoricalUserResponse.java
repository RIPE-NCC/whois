package net.ripe.db.whois.common.sso.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalUserResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    public Response response;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @XmlElement(required = true)
        public List<Results> results;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Results implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @XmlElement(required = true)
        public LocalDateTime eventDateTime;
        @XmlElement(required = true)
        public String action;
        @XmlElement(required = true)
        public List<AttributeChanges> attributeChanges;

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttributeChanges implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @XmlElement(required = true)
        public String name;
        @XmlElement(required = true)
        public String oldValue;
        @XmlElement(required = true)
        public String newValue;
    }

}
