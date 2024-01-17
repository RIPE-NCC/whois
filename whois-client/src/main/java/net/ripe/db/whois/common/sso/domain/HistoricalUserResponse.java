package net.ripe.db.whois.common.sso.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        @XmlJavaTypeAdapter(DateTimeAdapter.class)
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

    public static class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public LocalDateTime unmarshal(final String v) {
            return  LocalDateTime.from(DATE_TIME_FORMATTER.parse(v));
        }

        @Override
        public String marshal(final LocalDateTime v) {
            return DATE_TIME_FORMATTER.format(v.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")));
        }

    }
}
