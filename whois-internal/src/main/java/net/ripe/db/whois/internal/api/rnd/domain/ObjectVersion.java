package net.ripe.db.whois.internal.api.rnd.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.annotation.concurrent.Immutable;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class ObjectVersion implements Comparable<ObjectVersion> {

    private final long versionId;
    private final CIString pkey;
    private final ObjectType type;
    private final Interval interval;
    private final int revision;

    public ObjectVersion(final long versionId, final ObjectType type, final CIString pkey, final Interval interval, final int revision) {
        this.versionId = versionId;
        this.type = type;
        this.pkey = pkey;
        this.interval = interval;
        this.revision = revision;
    }

    public ObjectVersion(final long versionId, final ObjectType type, final String pkey, final Interval interval, final int revision) {
        this(versionId, type, ciString(pkey), interval, revision);
    }

    public ObjectVersion(final long versionId, final ObjectType type, final String pkey, final long fromTimestamp, final long toTimestamp, final int revision) {
        this(versionId, type, ciString(pkey), new Interval(new DateTime(fromTimestamp*1000L),
                                        new DateTime(toTimestamp == Long.MAX_VALUE ? Long.MAX_VALUE : toTimestamp*1000L)), revision);
    }

    public ObjectVersion(final long versionId, final ObjectType type, final CIString pkey, final long fromTimestamp, final long toTimestamp, final int revision) {
        this(versionId, type, pkey, new Interval(new DateTime(fromTimestamp*1000L),
                                                 new DateTime(toTimestamp == Long.MAX_VALUE ? Long.MAX_VALUE : toTimestamp*1000L)), revision);
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

    public Interval getInterval() {
        return interval;
    }

    public int getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectVersion)) return false;

        ObjectVersion that = (ObjectVersion) o;

        if (versionId != that.versionId) return false;
        if (!interval.equals(that.interval)) return false;
        if (!pkey.equals(that.pkey)) return false;
        if (type != that.type) return false;
        if (revision != that.revision) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (versionId ^ (versionId >>> 32));
        result = 31 * result + type.hashCode();
        result = 31 * result + pkey.hashCode();
        result = 31 * result + interval.hashCode();
        result = 31 * result + (int) (revision ^ (revision >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ObjectVersion{" +
                "versionId=" + versionId +
                ", type=" + type +
                ", pkey=" + pkey +
                ", interval=" + interval +
                ", revision=" + revision +
                '}';
    }

    @Override
    public int compareTo(ObjectVersion o) {
        return Integer.compare(o.revision, revision);
    }
}
