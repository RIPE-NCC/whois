package net.ripe.db.nrtm4.persist;

import java.util.UUID;


public class VersionInformation {

    private final Long id;
    private final NrtmSource source;
    private final Long version;
    private final UUID sessionID;
    private final NrtmDocumentType type;

    // It doesn't make sense to allow construction of these objects with
    // arbitrary parameters, since they are bound to published versions of the
    // NRTM repo. Consider a private constructor and a builder which the DAO
    // can use.
    VersionInformation(
        final Long id,
        final NrtmSource source,
        final Long version,
        final UUID sessionID,
        final NrtmDocumentType type
    ) {
        this.id = id;
        this.source = source;
        this.version = version;
        this.sessionID = sessionID;
        this.type = type;
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

}
