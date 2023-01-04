package net.ripe.db.nrtm4.dao;

import com.google.common.base.MoreObjects;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.util.Objects;


public class RpslObjectKey {

    private CIString primaryKey;
    private ObjectType objectType;

    public RpslObjectKey(final CIString primaryKey, final ObjectType objectType) {
        this.primaryKey = primaryKey;
        this.objectType = objectType;
    }

    public RpslObjectKey(final String primaryKey, final ObjectType objectType) {
        this(CIString.ciString(primaryKey), objectType);
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public CIString getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RpslObjectKey that = (RpslObjectKey)o;
        return Objects.equals(primaryKey, that.primaryKey) &&
            Objects.equals(objectType, that.objectType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey, objectType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("primaryKey", primaryKey)
            .add("objectType", objectType)
            .toString();
    }
}
