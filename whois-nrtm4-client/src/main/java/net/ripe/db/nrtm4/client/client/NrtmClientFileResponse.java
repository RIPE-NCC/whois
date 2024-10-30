package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NrtmClientFileResponse {

    private final int version;
    @JsonProperty("session_id")
    private final String sessionID;
    private final String hash;
    private final List<MirrorObjectInfo> objectMirrorInfo;

    public NrtmClientFileResponse() {
        this.version = 0;
        this.hash = null;
        this.objectMirrorInfo = null;
        this.sessionID = null;
    }

    public NrtmClientFileResponse(final List<MirrorObjectInfo> object,
                                  final int version,
                                  final String sessionID,
                                  final String hash) {
        this.version = version;
        this.sessionID = sessionID;
        this.hash = hash;
        this.objectMirrorInfo = object;
    }

    @Nullable
    public List<MirrorObjectInfo> getObjectMirrorInfo() {
        return objectMirrorInfo;
    }

    public int getVersion() {
        return version;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getHash() {
        return hash;
    }
}
