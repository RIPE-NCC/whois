package net.ripe.db.whois.internal.api.rnd;


import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.StreamingMarshal;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.query.QueryMessages;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class StreamHandler {
    private final Queue<ObjectVersion> queue = new ArrayDeque<>(1);
    private boolean versionFound;
    private StreamingMarshal marshal;
    private String source;
    private VersionObjectMapper objectMapper;

    public StreamHandler(final StreamingMarshal marshal, final String source, final VersionObjectMapper objectMapper) {
        this.marshal = marshal;
        this.source = source;
        this.objectMapper = objectMapper;
    }
    public void handleStreamEvent(final ObjectVersion version) {
        streamVersionObject(version);
    }

    private void streamVersionObject(final ObjectVersion version) {
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

        final WhoisVersionInternal internal = objectMapper.mapVersion(version, source);
//            final WhoisObject whoisObject = whoisObjectServerMapper.map(version, tagResponseObject, attributeMapper);

        marshal.writeArray(internal);
    }

    private void startStreaming() {
        marshal.open();

        marshal.start("versionsInternal");
        marshal.startArray("version");
    }


    public boolean versionFound() {
        return versionFound;
    }

    public void flushAndGetErrors() {
//            if (!versionFound) {
//                return errors;
//            }
        streamObject(queue.poll());

        marshal.endArray();

        marshal.end("versionsInternal");
        if (!versionFound) {
            final List<ErrorMessage> errorMessages = Lists.newArrayList(new ErrorMessage(QueryMessages.noResults(source)));
            marshal.write("errormessages", errorMessages);
        }

        marshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
        marshal.end("whois-resources");
        marshal.close();
    }
}
