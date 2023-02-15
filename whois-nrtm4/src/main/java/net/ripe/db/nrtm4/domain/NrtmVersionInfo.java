package net.ripe.db.nrtm4.domain;

public class NrtmVersionInfo {

    private final Long id;
    private final NrtmSourceModel source;
    private final Long version;
    private final String sessionID;
    private final NrtmDocumentType type;
    private final Integer lastSerialId;
    private final long created;

    // It doesn't make sense to allow construction of these objects with
    // arbitrary parameters, since they are bound to published versions of the
    // NRTM repo. Consider a private constructor and a builder which the DAO
    // can use.
    public NrtmVersionInfo(
        final Long id,
        final NrtmSourceModel source,
        final Long version,
        final String sessionID,
        final NrtmDocumentType type,
        final Integer lastSerialId,
        final long created
    ) {
        this.id = id;
        this.source = source;
        this.version = version;
        this.sessionID = sessionID;
        this.type = type;
        this.lastSerialId = lastSerialId;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public NrtmSourceModel getSource() {
        return source;
    }

    public Long getVersion() {
        return version;
    }

    public String getSessionID() {
        return sessionID;
    }

    public NrtmDocumentType getType() {
        return type;
    }

    public Integer getLastSerialId() {
        return lastSerialId;
    }

    public long getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "NrtmVersionInfo{" +
            "id=" + id +
            ", source=" + source +
            ", version=" + version +
            ", sessionID='" + sessionID + '\'' +
            ", type=" + type +
            ", lastSerialId=" + lastSerialId +
            ", created=" + created +
            '}';
    }

}
