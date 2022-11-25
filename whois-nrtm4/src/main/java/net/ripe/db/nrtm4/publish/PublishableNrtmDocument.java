package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.persist.NrtmSource;
import net.ripe.db.nrtm4.persist.VersionInformation;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


public abstract class PublishableNrtmDocument {

    @JsonProperty("nrtm_version")
    private final int nrtmVersion = NRTM_VERSION;
    private final String type;
    private final NrtmSource source;
    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    PublishableNrtmDocument(
        final VersionInformation version
    ) {
        this.type = version.getType().nameToLowerCase();
        this.source = version.getSource();
        this.sessionID = version.getSessionID().toString();
        this.version = version.getVersion();
    }

    public int getNrtmVersion() {
        return nrtmVersion;
    }

    public String getType() {
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
