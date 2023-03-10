package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


public class PublishableNrtmFile {

    @JsonIgnore
    private final Long versionId;
    @JsonProperty("nrtm_version")
    private final int nrtmVersion = NRTM_VERSION;
    private final NrtmDocumentType type;
    private final NrtmSourceModel source;
    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    protected PublishableNrtmFile() {
        versionId = 0L;
        type = null;
        source = null;
        sessionID = null;
        version = 0L;
    }

    public PublishableNrtmFile(
        final NrtmVersionInfo version
    ) {
        this.versionId = version.id();
        this.type = version.type();
        this.source = version.source();
        this.sessionID = version.sessionID();
        this.version = version.version();
    }

    public Long getVersionId() {
        return versionId;
    }

    public int getNrtmVersion() {
        return nrtmVersion;
    }

    public NrtmDocumentType getType() {
        return type;
    }

    public NrtmSourceModel getSource() {
        return source;
    }

    public String getSessionID() {
        return sessionID;
    }

    public long getVersion() {
        return version;
    }

}
