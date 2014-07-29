package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import java.util.List;

public interface ObjectReferenceDao {

    List<ObjectVersion> getObjectVersion(final ObjectType fromObjectType, final String pkey, final long timestamp);
    List<ObjectReference> getOutgoing(final long versionId);
    List<ObjectReference> getIncoming(final long versionId);
}
