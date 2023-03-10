package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class PublishableNrtmNotificationFile extends PublishableNrtmFile {

    private final String timestamp;
    @JsonProperty("next_signing_key")
    private final String nextSigningKey;
    private final NrtmFileLink snapshot;
    private final List<NrtmFileLink> deltas;

    public PublishableNrtmNotificationFile(
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

    public static class NrtmFileLink {

        private final long version;
        private final String url;
        private final String hash;

        public NrtmFileLink(final long version, final String url, final String hash) {
            this.version = version;
            this.url = url;
            this.hash = hash;
        }

    }

}
