package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import java.util.*;

import static net.ripe.db.whois.common.domain.CIString.ciString;

public enum ObjectType {
    AS_BLOCK("as-block", "ak"),
    AS_SET("as-set", "as"),
    AUT_NUM("aut-num", "an"),
    DOMAIN("domain", "dn"),
    FILTER_SET("filter-set", "fs"),
    INET6NUM("inet6num", "i6"),
    INETNUM("inetnum", "in"),
    INET_RTR("inet-rtr", "ir"),
    IRT("irt", "it"),
    KEY_CERT("key-cert", "kc"),
    MNTNER("mntner", "mt"),
    ORGANISATION("organisation", "oa"),
    PEERING_SET("peering-set", "ps"),
    PERSON("person", "pn"),
    POEM("poem", "po"),
    POETIC_FORM("poetic-form", "pf"),
    ROLE("role", "ro"),
    ROUTE("route", "rt"),
    ROUTE6("route6", "r6"),
    ROUTE_SET("route-set", "rs"),
    RTR_SET("rtr-set", "is");

    private static final Map<CIString, ObjectType> TYPE_NAMES;
    private static final Set<ObjectType> SET_OBJECTS;

    static {
        TYPE_NAMES = new HashMap<>(ObjectType.values().length * 2, 1);

        for (final ObjectType type : ObjectType.values()) {
            TYPE_NAMES.put(ciString(type.getName()), type);
            TYPE_NAMES.put(ciString(type.getShortName()), type);
        }

        final Set<ObjectType> setObjects = Sets.newHashSet();
        for (final ObjectType objectType : ObjectType.values()) {
            if (objectType.getName().endsWith("-set")) {
                setObjects.add(objectType);
            }
        }

        SET_OBJECTS = Collections.unmodifiableSet(Sets.newEnumSet(setObjects, ObjectType.class));
    }

    private final String name;
    private final String shortName;

    private ObjectType(final String name, final String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Boolean isResourceType() {
        return this == AUT_NUM || this == INET6NUM || this == INETNUM;
    }


    public static ObjectType getByName(final String name) throws IllegalArgumentException {
        final ObjectType ret = getByNameOrNull(name);
        if (ret == null) {
            throw new IllegalArgumentException("Invalid object type: " + name);
        }
        return ret;
    }

    public static ObjectType getByNameOrNull(final String name) {
        String nameOrNull = name;
        if (nameOrNull.length() == 3 && nameOrNull.charAt(0) == '*') {
            nameOrNull = nameOrNull.substring(1);
        }
        return TYPE_NAMES.get(ciString(nameOrNull));
    }

    public static ObjectType getByFirstAttribute(final AttributeType firstAttribute) {
        return ObjectType.getByName(firstAttribute.getName());
    }

    public static Set<ObjectType> getSets() {
        return SET_OBJECTS;
    }

    public static final Comparator<ObjectType> COMPARATOR = new Comparator<ObjectType>() {
        @Override
        public int compare(final ObjectType o1, final ObjectType o2) {
            return ObjectTemplate.getTemplate(o1).compareTo(ObjectTemplate.getTemplate(o2));
        }
    };
}
