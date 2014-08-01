package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;

public class ReferenceStreamHandler {
    private final Queue<ObjectVersion> queue = new ArrayDeque<>(1);
    private boolean versionFound;
    private boolean incomingFound;
    private boolean outgoingFound;
    private StreamingMarshal marshal;
    private String source;
    private VersionObjectMapper objectMapper;

    public ReferenceStreamHandler(final StreamingMarshal marshal, final String source, final VersionObjectMapper objectMapper) {
        this.marshal = marshal;
        this.source = source;
        this.objectMapper = objectMapper;
    }

    public void streamWhoisObject(final RpslObject object) {
        if (!versionFound) {
            versionFound = true;
            startStreaming();
        }

        streamObject(object);
    }

    private void startStreaming() {
        marshal.open();

        marshal.start("objects");
        marshal.startArray("object");
    }

    private void streamObject(@Nullable final RpslObject object) {
        if (object == null) {
            return;
        }

        marshal.writeArray(objectMapper.mapObject(object, source));
        marshal.endArray();
        marshal.end("objects");
    }

    private void startStreamingVersions(final String direction) {
        marshal.open();

        marshal.start(direction);
        marshal.startArray("version");
    }

    public void streamReference(final boolean isIncoming, final ObjectVersion version) {
        if (isIncoming && !incomingFound) {
            incomingFound = true;
            startStreamingVersions("incoming");
        } else if (!isIncoming && !outgoingFound) {
            outgoingFound = true;
            startStreamingVersions("outgoing");
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

    public void endStreamingIncoming() {
        if (!incomingFound) {
            return;
        }

        streamObject(queue.poll());
        marshal.endArray();
        marshal.end("incoming");
    }

    public void flush() {
        if (!outgoingFound) {
            return;
        }

        streamObject(queue.poll());

        marshal.endArray();
        marshal.end("outgoing");

        marshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
        marshal.end("whois-resources");
        marshal.close();
    }
}
