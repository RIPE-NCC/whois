package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SnapshotFileResponse {

    private final List<MirrorRpslObject> objects;

    private final int version;

    @JsonProperty("session_id")
    private final String sessionID;

    private final String hash;

    private SnapshotFileResponse() {
        objects = Lists.newArrayList();
        version = 0;
        sessionID = null;
        hash = null;
    }

    public SnapshotFileResponse(final List<MirrorRpslObject> rpslObject, final int version, final String sessionID,
                                final String hash) {
        this.objects = rpslObject;
        this.version = version;
        this.sessionID = sessionID;
        this.hash = hash;
    }

    public List<MirrorRpslObject> getObjects() {
        return objects;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getVersion() {
        return version;
    }

    public String getHash() {
        return hash;
    }
}
