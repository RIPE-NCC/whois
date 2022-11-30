package net.ripe.db.nrtm4.persist;

import java.util.UUID;


public class NrtmVersionInfo {

    private final Long id;
    private final NrtmSource source;
    private final Long version;
    private final UUID sessionID;
    private final NrtmDocumentType type;
    private final Integer lastSerialId;

    // It doesn't make sense to allow construction of these objects with
    // arbitrary parameters, since they are bound to published versions of the
    // NRTM repo. Consider a private constructor and a builder which the DAO
    // can use.
    NrtmVersionInfo(
        final Long id,
        final NrtmSource source,
        final Long version,
        final UUID sessionID,
        final NrtmDocumentType type,
        final Integer lastSerialId
    ) {
        this.id = id;
        this.source = source;
        this.version = version;
        this.sessionID = sessionID;
        this.type = type;
        this.lastSerialId = lastSerialId;
    }

    public Long getId() {
        return id;
    }

    public NrtmSource getSource() {
        return source;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getSessionID() {
        return sessionID;
    }

    public NrtmDocumentType getType() {
        return type;
    }

    public Integer getLastSerialId() {
        return lastSerialId;
    }

}
