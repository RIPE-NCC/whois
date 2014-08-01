package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.ReferenceStreamHandler;
import net.ripe.db.whois.internal.api.rnd.VersionsStreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import java.util.List;

public interface ObjectReferenceDao {
    void streamIncoming(final ObjectVersion objectVersion, final ReferenceStreamHandler streamHandler);
    void streamOutgoing(final ObjectVersion objectVersion, final ReferenceStreamHandler streamHandler);

    ObjectVersion getVersion(final ObjectType objectType, final String pkey, final int revision);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType, final long fromTimestamp, final long toTimestamp);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType);
    List<ObjectVersion> getVersions(final long fromTimestamp, final long toTimestamp);
    void streamVersions(final String pkey, final ObjectType objectType, final VersionsStreamHandler versionsStreamHandler);

    void createVersion(final ObjectVersion objectVersion);
    void updateVersionToTimestamp(final ObjectVersion objectVersion, final long endTimestamp);
    void deleteVersion(final ObjectVersion objectVersion);

    void createReference(final ObjectVersion from, final ObjectVersion to);


}
