package net.ripe.db.whois.common.dao.jdbc.domain;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;

import java.util.Map;

public class ObjectTypeIds {
    private static final Map<ObjectType, Integer> BY_OBJECT_TYPE = Maps.newEnumMap(ObjectType.class);
    private static final Map<Integer, ObjectType> BY_TYPE_ID;

    static {
        BY_OBJECT_TYPE.put(ObjectType.AS_BLOCK, 0);
        BY_OBJECT_TYPE.put(ObjectType.AS_SET, 1);
        BY_OBJECT_TYPE.put(ObjectType.AUT_NUM, 2);
        BY_OBJECT_TYPE.put(ObjectType.DOMAIN, 3);
        BY_OBJECT_TYPE.put(ObjectType.FILTER_SET, 14);
        BY_OBJECT_TYPE.put(ObjectType.INET6NUM, 5);
        BY_OBJECT_TYPE.put(ObjectType.INETNUM, 6);
        BY_OBJECT_TYPE.put(ObjectType.INET_RTR, 4);
        BY_OBJECT_TYPE.put(ObjectType.IRT, 17);
        BY_OBJECT_TYPE.put(ObjectType.KEY_CERT, 7);
        BY_OBJECT_TYPE.put(ObjectType.MNTNER, 9);
        BY_OBJECT_TYPE.put(ObjectType.ORGANISATION, 18);
        BY_OBJECT_TYPE.put(ObjectType.PEERING_SET, 15);
        BY_OBJECT_TYPE.put(ObjectType.PERSON, 10);
        BY_OBJECT_TYPE.put(ObjectType.POEM, 20);
        BY_OBJECT_TYPE.put(ObjectType.POETIC_FORM, 21);
        BY_OBJECT_TYPE.put(ObjectType.ROLE, 11);
        BY_OBJECT_TYPE.put(ObjectType.ROUTE, 12);
        BY_OBJECT_TYPE.put(ObjectType.ROUTE6, 19);
        BY_OBJECT_TYPE.put(ObjectType.ROUTE_SET, 13);
        BY_OBJECT_TYPE.put(ObjectType.RTR_SET, 16);

        BY_TYPE_ID = Maps.newHashMapWithExpectedSize(BY_OBJECT_TYPE.size());
        for (final Map.Entry<ObjectType, Integer> objectTypeIntegerEntry : BY_OBJECT_TYPE.entrySet()) {
            BY_TYPE_ID.put(objectTypeIntegerEntry.getValue(), objectTypeIntegerEntry.getKey());
        }

        for (final ObjectType objectType : ObjectType.values()) {
            Validate.notNull(BY_OBJECT_TYPE.get(objectType), "No ObjectDao for: " + objectType);
        }
    }

    public static Integer getId(final ObjectType objectType) {
        return BY_OBJECT_TYPE.get(objectType);
    }

    public static ObjectType getType(final int serialType) throws IllegalArgumentException {
        final ObjectType objectType = BY_TYPE_ID.get(serialType);
        if (objectType == null) {
            throw new IllegalArgumentException("Object type with objectTypeId " + serialType + " not found");
        }

        return objectType;
    }
}
