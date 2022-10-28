package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.persist.NrtmSource;


public abstract class PublishableNrtmDocument {

    @JsonProperty("nrtm_version")
    private int nrtmVersion = 4;
    private NrtmDocumentType type;
    private NrtmSource source;
    @JsonProperty("session_id")
    private String sessionID;
    private final int version;

    PublishableNrtmDocument(
            final NrtmDocumentType type,
            final NrtmSource source,
            final String sessionID,
            final int version
    ) {
        this.type = type;
        this.source = source;
        this.sessionID = sessionID;
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

    public int getVersion() {
        return version;
    }

}
