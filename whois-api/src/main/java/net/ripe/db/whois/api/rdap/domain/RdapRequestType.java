package net.ripe.db.whois.api.rdap.domain;

import com.google.common.collect.ImmutableSet;
import net.ripe.db.whois.api.rdap.RdapException;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.eclipse.jetty.http.HttpStatus;

import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;

public enum RdapRequestType {

    AUTNUM {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return ImmutableSet.of(AUT_NUM, AS_BLOCK);
        }
    },

    AUTNUMS {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return ImmutableSet.of(AUT_NUM);
        }
    },

    DOMAIN {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return ImmutableSet.of(ObjectType.DOMAIN);
        }
    },

    DOMAINS {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return ImmutableSet.of(ObjectType.DOMAIN);
        }
    },

    IP {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return key.contains(":") ? ImmutableSet.of(INET6NUM) : ImmutableSet.of(INETNUM);
        }
    },

    IPS {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return key.contains(":") ? ImmutableSet.of(INET6NUM) : ImmutableSet.of(INETNUM);
        }
    },

    ENTITY {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            return ImmutableSet.of(PERSON, ROLE, MNTNER);
        }
    },

    NAMESERVER {
        public Set<ObjectType> getWhoisObjectTypes(final String key) {
            throw new RdapException("Not Implemented", "Nameserver not supported", HttpStatus.NOT_IMPLEMENTED_501);
        }
    };

    abstract public Set<ObjectType> getWhoisObjectTypes(final String key);
}
