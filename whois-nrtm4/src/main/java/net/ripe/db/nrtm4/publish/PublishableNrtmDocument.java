package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.persist.NrtmDocumentType;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.VersionInformation;


public abstract class PublishableNrtmDocument {

    @JsonProperty("nrtm_version")
    private int nrtmVersion = 4;
    private NrtmDocumentType type;
    private NrtmSource source;
    @JsonProperty("session_id")
    private String sessionID;
    private final long version;

    PublishableNrtmDocument(
            final VersionInformation version
    ) {
        this.type = version.getType();
        this.source = version.getSource();
        this.sessionID = version.getSessionID().toString();
        this.version = version.getVersion();
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
