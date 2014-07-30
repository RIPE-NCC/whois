package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.StreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import java.util.List;

public interface ObjectReferenceDao {

    List<ObjectReference> getOutgoing(final long versionId);
    List<ObjectReference> getIncoming(final long versionId);

    ObjectVersion getVersion(final ObjectType objectType, final String pkey, final int revision);
    List<ObjectVersion> getVersions(final String pkey, final ObjectType objectType);
    void streamVersions(final String pkey, final ObjectType objectType, final StreamHandler streamHandler);

    void createVersion(final ObjectVersion objectVersion);
    void deleteVersion(final ObjectVersion objectVersion);

}
