package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.dao.NrtmDocumentType;
import net.ripe.db.nrtm4.dao.NrtmSource;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;

import static net.ripe.db.nrtm4.NrtmConstants.NRTM_VERSION;


public abstract class PublishableNrtmDocument {

    @JsonIgnore
    private final Long versionId;
    @JsonProperty("nrtm_version")
    private final int nrtmVersion = NRTM_VERSION;
    private final NrtmDocumentType type;
    private final NrtmSource source;
    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    PublishableNrtmDocument(
        final NrtmVersionInfo version
    ) {
        this.versionId = version.getId();
        this.type = version.getType();
        this.source = version.getSource();
        this.sessionID = version.getSessionID();
        this.version = version.getVersion();
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
