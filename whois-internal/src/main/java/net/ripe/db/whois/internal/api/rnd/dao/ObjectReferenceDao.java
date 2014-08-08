package net.ripe.db.whois.internal.api.rnd.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.api.rnd.ReferenceStreamHandler;
import net.ripe.db.whois.internal.api.rnd.VersionsStreamHandler;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

public interface ObjectReferenceDao {
    void streamIncoming(final ObjectVersion objectVersion, final ReferenceStreamHandler streamHandler);
    void streamOutgoing(final ObjectVersion objectVersion, final ReferenceStreamHandler streamHandler);
    void streamVersions(final String pkey, final ObjectType objectType, final VersionsStreamHandler versionsStreamHandler);

    ObjectVersion getVersion(final ObjectType objectType, final String pkey, final int revision);

}
