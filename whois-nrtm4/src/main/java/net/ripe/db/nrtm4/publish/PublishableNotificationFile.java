package net.ripe.db.nrtm4.publish;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.db.nrtm4.persist.NrtmVersionInfo;

import java.util.List;


public class PublishableNotificationFile extends PublishableNrtmDocument {

    @JsonProperty("next_signing_key")
    private String nextSigningKey;
    private FileRef snapshot;
    private List<FileRef> deltas;

    PublishableNotificationFile(NrtmVersionInfo version) {
        super(version);
    }

    private class FileRef {

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
