package net.ripe.db.whois.common.sso.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateTokenResponse implements Serializable {

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
        public Content content;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @XmlElement(required = true)
        public String firstName;
        @XmlElement(required = true)
        public String lastName;
        @XmlElement(required = true)
        public String email;
        @XmlElement(required = true)
        public String id;
        @XmlElement(required = true)
        public boolean active;
        @XmlElement(required = true)
        public List<AccessRole> accessRoles;

        @JsonProperty("Name")
        public String getName() {
            return(firstName + " " + lastName);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccessRole implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @XmlElement(required = true)
        public long membershipId;
        @XmlElement(required = true)
        public List<String> permissions;
    }

}
