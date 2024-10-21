package net.ripe.db.nrtm4.client.scheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nrtm_version", "type", "source", "session_id", "version"})
public class NrtmVersionRecord {

    @JsonProperty("nrtm_version")
    private final int nrtmVersion = 4;
    private final NrtmDocumentType type;
    private final String source;
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

    public NrtmVersionRecord(final String source, final String sessionId, final Long version,
                             final NrtmDocumentType type) {
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

    public String getSource() {
        return source;
    }

    public String getSessionID() {
        return sessionID;
    }

    public long getVersion() {
        return version;
    }

}
