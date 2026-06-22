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
public class UserDetailsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @XmlElement(required = true)
    public String status;

    @XmlElement(required = true)
    public UserDetailsResponse.Content content;

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
        public String login;
        @XmlElement(required = true)
        public String uuid;
        @XmlElement(required = true)
        public boolean active;
        @XmlElement(required = true)
        public List<ValidateTokenResponse.AccessRole> accessRoles;

        @JsonProperty("Name")
        public String getName() {
            return(firstName + " " + lastName);
        }
    }
}
