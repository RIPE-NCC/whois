package net.ripe.db.whois.common.domain;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public class Tag {
    private final CIString type;
    private final int objectId;
    private final String value;

    public Tag(final CIString type, final int objectId) {
        this(type, objectId, null);
    }

    public Tag(final CIString type, @Nullable final String value) {
        this(type, -1, value);
    }

    public Tag(final CIString type, final int objectId, @Nullable final String value) {
        this.type = type;
        this.objectId = objectId;
        this.value = value;
    }

    public CIString getType() {
        return type;
    }

    public int getObjectId() {
        return objectId;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Tag tag = (Tag) o;

        return Objects.equals(objectId, tag.objectId) &&
                Objects.equals(type, tag.type) &&
                Objects.equals(value, tag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, objectId, value);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "type=" + type +
                ", objectId=" + objectId +
                ", value='" + value + '\'' +
                '}';
    }
}
