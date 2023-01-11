package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.whois.common.domain.Timestamp;

import java.time.LocalDateTime;
import java.util.List;


public class PublishableNotificationFile extends PublishableNrtmDocument {

    private final String timestamp;
    @JsonProperty("next_signing_key")
    private String nextSigningKey;
    private FileRef snapshot;
    private final List<FileRef> deltas;

    PublishableNotificationFile(final NrtmVersionInfo version) {
        super(version);
        deltas = List.of();
        this.timestamp = Timestamp.from(LocalDateTime.now()).toString();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNextSigningKey() {
        return nextSigningKey;
    }

    public FileRef getSnapshot() {
        return snapshot;
    }

    public List<FileRef> getDeltas() {
        return deltas;
    }

    static class FileRef {

        private final int version;
        private final String url;
        private final String hash;

        FileRef(final int version, final String url, final String hash) {
            this.version = version;
            this.url = url;
            this.hash = hash;
        }

        public int getVersion() {
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
