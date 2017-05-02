package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "role")
@XmlEnum
@XmlJavaTypeAdapter(Role.Adapter.class)
public enum Role {

    REGISTRANT("registrant"),
    TECHNICAL("technical"),
    ADMINISTRATIVE("administrative"),
    ABUSE("abuse"),
    BILLING("billing"),
    REGISTRAR("registrar"),
    RESELLER("reseller"),
    SPONSOR("sponsor"),
    PROXY("proxy"),
    ZONE("zone");                   // TODO: [ES] not in RDAP spec, added for domain objects

    final String value;

    Role(final String value) {
        this.value = value;
    }

    public static class Adapter extends XmlAdapter<String, Role> {

        @Override
        public Role unmarshal(final String value) throws Exception {
            for (Role role : Role.values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException(value);
        }

        @Override
        public String marshal(final Role role) throws Exception {
            return role.value;
        }
    }
}