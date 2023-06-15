package net.ripe.db.whois.common.sso.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidateTokenResponse {

    @XmlElement(required = true)
    public Response response;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @XmlElement(required = true)
        public Content content;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
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
    public static class AccessRole {
        @XmlElement(required = true)
        public long membershipId;
        @XmlElement(required = true)
        public List<String> permissions;
    }

}
