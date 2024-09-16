package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nrtm_version", "type", "source", "session_id", "version"})
public class NrtmVersionRecord implements NrtmFileRecord {

    @JsonProperty("nrtm_version")
    private final int nrtmVersion = NRTM_VERSION;
    private final NrtmDocumentType type;
    private final NrtmSource source;
    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    public NrtmVersionRecord() {
        this.type = null;
        this.source = null;
        this.sessionID = null;
        this.version = 0L;
    }

    public NrtmVersionRecord(final NrtmVersionInfo version, final NrtmDocumentType type) {
        this.type = type;
        this.source = version.source();
        this.sessionID = version.sessionID();
        this.version = version.version();
    }

    public NrtmVersionRecord(final NrtmSource source, final String sessionId, final Long version, final NrtmDocumentType type) {
        this.type = type;
        this.source = source;
        this.sessionID = sessionId;
        this.version = version;
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
