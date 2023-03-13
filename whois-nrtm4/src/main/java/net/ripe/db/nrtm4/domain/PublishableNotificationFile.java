package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"nrtm_version", "timestamp", "type", "next_signing_key", "source", "session_id", "version", "snapshot", "deltas"})
public class PublishableNotificationFile extends PublishableNrtmFile {

    private final String timestamp;
    @JsonProperty("next_signing_key")
    private final String nextSigningKey;
    private final NrtmFileLink snapshot;
    private final List<NrtmFileLink> deltas;

    private PublishableNotificationFile() {
        timestamp = null;
        nextSigningKey = null;
        snapshot = null;
        deltas = null;
    }

    public PublishableNotificationFile(
        final NrtmVersionInfo version,
        final String timestamp,
        final String nextSigningKey,
        final NrtmFileLink snapshot,
        final List<NrtmFileLink> deltas
    ) {
        super(version);
        this.timestamp = timestamp;
        this.nextSigningKey = nextSigningKey;
        this.snapshot = snapshot;
        this.deltas = deltas;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNextSigningKey() {
        return nextSigningKey;
    }

    public NrtmFileLink getSnapshot() {
        return snapshot;
    }

    public List<NrtmFileLink> getDeltas() {
        return deltas;
    }

    @JsonPropertyOrder({"version", "url", "hash"})
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

    }

}
