package net.ripe.db.whois.rdap.domain;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "role")
@XmlEnum
@XmlJavaTypeAdapter(Role.Adapter.class)
public enum Role {

    REGISTRANT("registrant"),
    TECHNICAL("technical"),
    ADMINISTRATIVE("administrative"),
    ABUSE("abuse"),
    BILLING("billing"),
    NOC("noc"),
    NOTIFICATIONS("notifications"),
    REGISTRAR("registrar"),
    RESELLER("reseller"),
    SPONSOR("sponsor"),
    PROXY("proxy"),
    UNKNOWN("unknown"),             // catch-all for role values not in RDAP spec
    ZONE("zone");                   // TODO: [ES] not in RDAP spec, added for domain objects

    final String value;

    Role(final String value) {
        this.value = value;
    }

    public static class Adapter extends XmlAdapter<String, Role> {

        @Override
        public Role unmarshal(final String value) {
            for (Role role : Role.values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String marshal(final Role role) {
            return role.value;
        }
    }
}
