package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateNotificationFileResponse {

    @JsonProperty("session_id")
    private final String sessionID;
    private final long version;
    private final NrtmFileLink snapshot;

    public UpdateNotificationFileResponse() {
        this.sessionID = null;
        this.version = 0L;
        this.snapshot = null;
    }

    @Nullable
    public String getSessionID() {
        return sessionID;
    }

    public long getVersion() {
        return version;
    }

    @Nullable
    public NrtmFileLink getSnapshot(){
        return snapshot;
    }

    public static class NrtmFileLink {

        private final long version;
        private final String url;
        private final String hash;

        private NrtmFileLink() {
            version = 0;
            url = null;
            hash = null;
        }

        public NrtmFileLink(final long version, final String url, final String hash) {
            this.version = version;
            this.url = url;
            this.hash = hash;
        }

        public long getVersion() {
            return version;
        }

        public String getUrl() {
            return url;
        }

        public String getHash() {
            return hash;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final NrtmFileLink that = (NrtmFileLink) o;
            return version == that.version && url.equals(that.url) && hash.equals(that.hash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, url, hash);
        }

    }
}
