package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import org.joda.time.DateTime;

import java.util.List;

public interface ObjectReferenceUpdateDao {

    void createVersion(final ObjectVersion objectVersion);
    void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp);
    void createReference(final ObjectVersion from, final ObjectVersion to);

    List<ObjectVersion> getVersions(final long fromTimestamp);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType, final DateTime fromDate, final DateTime toDate);
    ObjectVersion getVersion(final ObjectType objectType, final String pkey, final int revision);

    List<ObjectVersion> findIncomingReferences(final ObjectVersion objectVersion);

    long getLatestVersionTimestamp();
}
