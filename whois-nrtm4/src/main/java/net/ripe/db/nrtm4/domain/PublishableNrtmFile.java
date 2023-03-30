package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


public abstract class PublishableNrtmFile {

    @JsonProperty("nrtm_version")
    private final int nrtmVersion = NRTM_VERSION;
    private final NrtmDocumentType type;
    private final NrtmSource source;
    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    protected PublishableNrtmFile() {
        type = null;
        source = null;
        sessionID = null;
        version = 0L;
    }

    public PublishableNrtmFile(
        final NrtmVersionInfo version
    ) {
        this.type = version.type();
        this.source = version.source();
        this.sessionID = version.sessionID();
        this.version = version.version();
    }

    public int getNrtmVersion() {
        return nrtmVersion;
    }

    public NrtmDocumentType getType() {
        return type;
    }

    public NrtmSource getSource() {
        return source;
    }

    public String getSessionID() {
        return sessionID;
    }

    public long getVersion() {
        return version;
    }

}
