package net.ripe.db.whois.internal.api.rnd.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.annotation.concurrent.Immutable;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class ObjectReference {

    private final long versionId;
    private final ObjectType refObjectType;
    private final CIString refPkey;
    private final ReferenceType referenceType;

    public ObjectReference(final long versionId, final ObjectType refObjectType, final CIString refPkey, final ReferenceType referenceType) {
        this.versionId = versionId;
        this.refObjectType = refObjectType;
        this.refPkey = refPkey;
        this.referenceType = referenceType;
    }

    public ObjectReference(final long versionId, final ObjectType refObjectType, final String refPkey, final ReferenceType referenceType) {
        this(versionId, refObjectType, ciString(refPkey), referenceType);
    }

    public ObjectType getRefObjectType() {
        return refObjectType;
    }

    public CIString getRefPkey() {
        return refPkey;
    }

    public long getVersionId() {
        return versionId;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectReference)) return false;

        ObjectReference that = (ObjectReference) o;

        if (versionId != that.versionId) return false;
        if (refObjectType != that.refObjectType) return false;
        if (!refPkey.equals(that.refPkey)) return false;
        if (referenceType != that.referenceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (versionId ^ (versionId >>> 32));
        result = 31 * result + refObjectType.hashCode();
        result = 31 * result + refPkey.hashCode();
        result = 31 * result + referenceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObjectReference{" +
                "versionId=" + versionId +
                ", refObjectType=" + refObjectType +
                ", refPkey=" + refPkey +
                ", referenceType=" + referenceType +
                '}';
    }
}

