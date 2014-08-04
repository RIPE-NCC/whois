package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;

public class ReferenceStreamHandler {
    private final Queue<ObjectVersion> queue = new ArrayDeque<>(1);
    private boolean objectFound;
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
        if (!objectFound) {
            objectFound = true;
            startStreamingObject();
        }

        streamObject(object);
    }

    private void startStreamingObject() {
        marshal.open();
    }

    private void streamObject(@Nullable final RpslObject object) {
        if (object == null) {
            return;
        }

        marshal.write("object", objectMapper.mapObject(object, source));
    }

    private void startStreamingVersions(final String direction) {
        marshal.startArray(direction);
    }

    public void streamReference(final boolean isIncoming, final ObjectVersion version) {
        if (isIncoming && !incomingFound) {
            incomingFound = true;
            startStreamingVersions("incoming");
        } else if (!isIncoming && !outgoingFound) {
            outgoingFound = true;
            startStreamingVersions("outgoing");
        }

        streamObjectVersion(queue.poll());
        queue.add(version);
    }

    private void streamObjectVersion(@Nullable final ObjectVersion version) {
        if (version == null) {
            return;
        }

        marshal.writeArray(objectMapper.mapVersion(version, source));
    }

    public void endStreamingIncoming() {
        if (!incomingFound) {
            return;
        }

        streamObjectVersion(queue.poll());
        marshal.endArray();
    }

    public void flush() {
        if (outgoingFound) {
            streamObjectVersion(queue.poll());
            marshal.endArray();
        }

        marshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
        marshal.end("whois-resources");
        marshal.close();
    }

    public void streamVersion(final ObjectVersion version) {
        marshal.write("version", objectMapper.mapVersion(version, source));
    }

    public void streamErrorMessage(final Message message) {
        marshal.write("errormessages", Lists.newArrayList(new ErrorMessage(message)));
    }
}
