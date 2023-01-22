package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;

import jakarta.annotation.concurrent.Immutable;

import java.util.Objects;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public final class ObjectKey {
    final ObjectType objectType;
    final CIString pkey;

    public ObjectKey(final ObjectType objectType, final String pkey) {
        this(objectType, ciString(pkey));
    }

    public ObjectKey(final ObjectType objectType, final CIString pkey) {
        this.objectType = objectType;
        this.pkey = pkey;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public CIString getPkey() {
        return pkey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ObjectKey that = (ObjectKey) o;

        return Objects.equals(objectType, that.objectType) &&
                Objects.equals(pkey, that.pkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, pkey);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", objectType.getName(), pkey);
    }
}
