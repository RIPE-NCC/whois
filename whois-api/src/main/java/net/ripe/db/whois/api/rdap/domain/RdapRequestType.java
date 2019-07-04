package net.ripe.db.whois.api.rdap.domain;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.common.rpsl.ObjectType;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;

public enum RdapRequestType {

    AUTNUM {
        public Set<ObjectType> getWhoisObjectTypes(String key) {
            return ImmutableSet.of(AUT_NUM, AS_BLOCK);
        }
    },

    DOMAIN {
        public Set<ObjectType> getWhoisObjectTypes(String key) {
            return ImmutableSet.of(ObjectType.DOMAIN);
        }
    },

    IP {
        public Set<ObjectType> getWhoisObjectTypes(String key) {
            return key.contains(":") ? ImmutableSet.of(INET6NUM) : ImmutableSet.of(INETNUM);
        }
    },

    ENTITY {
        public Set<ObjectType> getWhoisObjectTypes(String key) {
            return key.startsWith("ORG-") ? ImmutableSet.of(ORGANISATION) : ImmutableSet.of(PERSON, ROLE, MNTNER);
        }
    },

    NAMESERVER {
        public Set<ObjectType> getWhoisObjectTypes(String key) {
            throw new IllegalStateException("Nameserver is not supported");
        }
    };

    abstract public Set<ObjectType> getWhoisObjectTypes(String key);
}
