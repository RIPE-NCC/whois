package net.ripe.db.whois.internal.api.rnd;


import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;

public class VersionsStreamHandler {
    private final Queue<ObjectVersion> queue = new ArrayDeque<>(1);
    private boolean versionFound;
    private StreamingMarshal marshal;
    private String source;
    private VersionObjectMapper objectMapper;

    public VersionsStreamHandler(final StreamingMarshal marshal, final String source, final VersionObjectMapper objectMapper) {
        this.marshal = marshal;
        this.source = source;
        this.objectMapper = objectMapper;
    }

    public void streamObjectVersion(final ObjectVersion version) {
        if (!versionFound) {
            versionFound = true;
            startStreaming();
        }
        streamObject(queue.poll());
        queue.add(version);
    }

    private void streamObject(@Nullable final ObjectVersion version) {
        if (version == null) {
            return;
        }

        marshal.writeArray(objectMapper.mapVersion(version, source));
    }

    private void startStreaming() {
        marshal.open();

        marshal.startArray("versions");
    }

    public boolean flushHasStreamedObjects() {
        if (!versionFound) {
            return versionFound;
        }

        streamObject(queue.poll());

        marshal.endArray();

        marshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
        marshal.end("whois-resources");
        marshal.close();
        return true;
    }
}
