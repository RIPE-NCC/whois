package net.ripe.db.whois.internal.api.rnd.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.joda.time.DateTime;

import javax.annotation.concurrent.Immutable;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class ObjectVersion implements Comparable<ObjectVersion> {

    private final long versionId;
    private final CIString pkey;
    private final ObjectType type;
    private final int revision;
    private final DateTime fromDate;
    private final DateTime toDate;

    public ObjectVersion(final long versionId, final ObjectType type, final CIString pkey, final DateTime fromDate, final DateTime toDate, final int revision) {
        this.versionId = versionId;
        this.type = type;
        this.pkey = pkey;
        this.revision = revision;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public ObjectVersion(final long versionId, final ObjectType type, final String pkey, final DateTime fromDate, final DateTime toDate, final int revision) {
        this(versionId, type, ciString(pkey), fromDate, toDate, revision);
    }

    public ObjectVersion(final long versionId, final ObjectType type, final String pkey, final long fromTimestamp, final long toTimestamp, final int revision) {
        this(versionId, type, ciString(pkey),
                new DateTime(fromTimestamp*1000),
                toTimestamp == 0 ? null : new DateTime(toTimestamp*1000),
                revision);
    }

    public long getVersionId() {
        return versionId;
    }

    public ObjectType getType() {
        return type;
    }

    public CIString getPkey() {
        return pkey;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public DateTime getToDate() {
        return toDate;
    }

    public int getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectVersion)) return false;

        ObjectVersion that = (ObjectVersion) o;

        if (revision != that.revision) return false;
        if (versionId != that.versionId) return false;
        if (fromDate != null ? !fromDate.equals(that.fromDate) : that.fromDate != null) return false;
        if (pkey != null ? !pkey.equals(that.pkey) : that.pkey != null) return false;
        if (!toDate.equals(that.toDate)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (versionId ^ (versionId >>> 32));
        result = 31 * result + (pkey != null ? pkey.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + revision;
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + toDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ObjectVersion{" +
                "versionId=" + versionId +
                ", pkey=" + pkey +
                ", type=" + type +
                ", revision=" + revision +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }

    @Override
    public int compareTo(ObjectVersion o) {
        return Integer.compare(o.revision, revision);
    }
}
