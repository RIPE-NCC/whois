package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateNotificationFileResponse {

    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;

    public UpdateNotificationFileResponse() {
        this.sessionID = null;
        this.version = 0L;
    }

    public String getSessionID() {
        return sessionID;
    }

    public long getVersion() {
        return version;
    }

}
