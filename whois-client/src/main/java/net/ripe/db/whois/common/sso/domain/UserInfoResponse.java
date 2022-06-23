package net.ripe.db.whois.common.sso.domain;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class UserInfoResponse {

    @XmlElement
    public User user;
    @XmlElement
    public List<Organisation> organisations;
    @XmlElement
    public List<Member> members;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    public static class User {
        public String username;
        public String uuid;
        public boolean active;
        public String displayName;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    public static class Organisation {
        public String orgObjectId;
        public String organisationName;
        public List<String> roles;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement
    public static class Member {
        public long membershipId;
        public String regId;
        public String orgObjectId;
        public String organisationName;
        public List<String> roles;
    }
}
