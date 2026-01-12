package net.ripe.db.whois.common.dao;

import com.google.common.base.MoreObjects;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.Validate;

import java.util.Objects;

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RpslObjectInfo that = (RpslObjectInfo) o;

        return Objects.equals(objectId, that.objectId);
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
        return MoreObjects.toStringHelper(RpslObjectInfo.class)
                .add("objectId", objectId)
                .add("objectType", objectType)
                .add("key", key)
                .toString();
    }
}
