package net.ripe.db.whois.common.sso.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberContactsResponse {

    @XmlElement(required = true)
    public Response response;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @XmlElement(required = true)
        public List<ContactDetails> results;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactDetails {
        @XmlElement(required = true)
        public String email;
        public String group;
        public String membershipId;
        public boolean active;

        public String getEmail() {
            return email;
        }
        public String getGroup() {
            return group;
        }
        public String getMembershipId() {
            return membershipId;
        }

        public boolean isActive() {
            return active;
        }
    }
}
