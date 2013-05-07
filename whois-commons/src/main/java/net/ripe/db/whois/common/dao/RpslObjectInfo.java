package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

public class RpslObjectInfo implements Identifiable, Comparable<RpslObjectInfo> {
    private final int objectId;
    private final ObjectType objectType;
    private final String key;

    public RpslObjectInfo(final int objectId, final ObjectType objectType, final CIString key) {
        this(objectId, objectType, key.toString());
    }

    public RpslObjectInfo(final int objectId, final ObjectType objectType, final String key) {
        Validate.notNull(objectType);
        this.objectId = objectId;
        this.objectType = objectType;
        this.key = key;
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && objectId == ((RpslObjectInfo) o).objectId;
    }

    @Override
    public int hashCode() {
        return objectId;
    }

    @Override
    public int compareTo(final RpslObjectInfo o) {
        final int result = ObjectTemplate.getTemplate(objectType).compareTo(ObjectTemplate.getTemplate(o.getObjectType()));
        if (result != 0) {
            return result;
        }

        return key.compareTo(o.key);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(RpslObjectInfo.class)
                .append("objectId", objectId)
                .append("objectType", objectType)
                .append("key", key)
                .toString();
    }
}
