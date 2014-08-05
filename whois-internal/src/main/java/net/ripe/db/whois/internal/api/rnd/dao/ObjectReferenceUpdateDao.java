package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import java.util.List;

public interface ObjectReferenceUpdateDao {

    void createReference(final ObjectVersion from, final ObjectVersion to);
    List<ObjectVersion> findIncomingReferences(final ObjectVersion objectVersion);
    List<ObjectVersion> findOutgoingReferences(final ObjectVersion objectVersion);

    void createVersion(final ObjectVersion objectVersion);
    void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp);
    void deleteVersion(final ObjectVersion objectVersion);

    List<ObjectVersion> getVersions(final long fromTimestamp);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType, final long fromTimestamp, final long toTimestamp);
    ObjectVersion getVersion(final ObjectType objectType, final String pkey, final int revision);

}
