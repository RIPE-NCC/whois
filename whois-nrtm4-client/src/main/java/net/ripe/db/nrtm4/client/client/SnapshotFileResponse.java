package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SnapshotFileResponse {

    @JsonDeserialize(using = RpslObjectDeserializer.class)
    private final List<SnapshotClientFileRecord> objects;

    private final int version;

    @JsonProperty("session_id")
    private final String sessionID;

    private SnapshotFileResponse() {
        objects = Lists.newArrayList();
        version = 0;
        sessionID = null;
    }

    public SnapshotFileResponse(final List<SnapshotClientFileRecord> rpslObject, final int version, final String sessionID) {
        this.objects = rpslObject;
        this.version = version;
        this.sessionID = sessionID;
    }

    public List<SnapshotClientFileRecord> getObjects() {
        return objects;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getVersion() {
        return version;
    }
}
