package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.dao.NrtmVersionInfo;
import net.ripe.db.whois.common.FormatHelper;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        this.timestamp = FormatHelper.dateTimeToUtcString(ZonedDateTime.now(ZoneOffset.UTC));
        // TODO: replace with this, but how to inject dateTimeProvider?
        //       FormatHelper.dateTimeToUtcString(dateTimeProvider.getCurrentZonedDateTime())
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
