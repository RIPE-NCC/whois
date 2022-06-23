package net.ripe.db.whois.common.sso.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberDetailsResponse {

    @XmlElement(required = true)
    public List<Content> content;

    @XmlElement(name="page")
    public Paging paging;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {

        @XmlElement(required = true)
        public long membershipId;

        @XmlElement(required = true, name = "regId")
        public String regid;

        @XmlElement(required = true)
        public String name;

        @XmlElement(required = true, name = "orgObject")
        public String orgObjectId;

        @XmlElement(required = true)
        public ServiceLevel serviceLevel;

        @XmlElement(required = true)
        public SubscriptionStatus subscriptionStatus;

        @XmlElement(required = true)
        public String subscriptionType;

        @XmlElement(required = true)
        public Boolean euSanctioned;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServiceLevel {

        private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLevel.class);

        @XmlElement(required = true)
        public Level level;

        @XmlElement(required = true)
        public String reason;

        public enum Level {

            NORMAL, NONE, UNAVAILABLE, UNKNOWN;

            @JsonCreator
            public static Level forValue(final String value) {
                for (Level level : values()) {
                    if (level.name().equalsIgnoreCase(value)) {
                        return level;
                    }
                }
                LOGGER.warn("Unknown service level {}", value);
                return Level.UNKNOWN;
            }

            @JsonValue
            public String toJson() {
                return name().toLowerCase();
            }
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionStatus {

        @XmlElement(required = true)
        public String label;

        @XmlElement(required = true)
        public String value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Paging {

        @XmlElement(required = true)
        public int size;

        @XmlElement(required = true)
        public int totalElements;

        @XmlElement(required = true)
        public int totalPages;

        @XmlElement(required = true)
        public int number;
    }

}
