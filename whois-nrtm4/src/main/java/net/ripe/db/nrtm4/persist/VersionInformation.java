package net.ripe.db.nrtm4.persist;

import java.util.UUID;
import net.ripe.db.whois.common.domain.Timestamp;

public class VersionInformation {

    private Long id;
    private NrtmSource source;
    private Long version;
    private UUID sessionID;
    private Timestamp timestamp;

    VersionInformation(
            final Long id,
            final NrtmSource source,
            final Long version,
            final UUID sessionID,
            final Timestamp timestamp
    ) {
        this.id = id;
        this.source = source;
        this.version = version;
        this.sessionID = sessionID;
        this.timestamp = timestamp;
    }

    public VersionInformation incrementVersion() {
        return new VersionInformation(0L, this.source, this.version + 1, this.sessionID, Timestamp.fromMilliseconds(System.currentTimeMillis()));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NrtmSource getSource() {
        return source;
    }

    public void setSource(final NrtmSource source) {
        this.source = source;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public void setSessionID(final UUID sessionID) {
        this.sessionID = sessionID;
    }

}
