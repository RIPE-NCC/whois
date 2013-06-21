package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.RdapResponse;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class RdapStreamingOutput extends WhoisStreamingOutput {

    public RdapStreamingOutput(StreamingMarshal sm, QueryHandler qh, Parameters p, Query q, InetAddress ra, int cid) {
        super(sm,qh,p,q,ra,cid);
    }

    @Override
    public void write(final OutputStream output) throws IOException {
        streamingMarshal.open(output);

        streamingMarshal.start("yep");

        // TODO [AK] Crude way to handle tags, but working
        final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<RpslObject>(1);
        final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();
        final Queue<TaggedRpslObject> taggedRpslObjectQueue = new ArrayDeque<TaggedRpslObject>(1);

        try {
            queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof TagResponseObject) {
                        tagResponseObjects.add((TagResponseObject) responseObject);
                    } else if (responseObject instanceof RpslObject) {
                        found = true;
                        taggedRpslObjectQueue.add(new TaggedRpslObject((RpslObject)responseObject, tagResponseObjects));
                        tagResponseObjects.clear();
                    }

                    // TODO [AK] Handle related messages
                }
            });

            RdapObjectMapper rdapObjectMapper = new RdapObjectMapper(taggedRpslObjectQueue);
            RdapResponse rdapResponse;

            try {
                rdapResponse = rdapObjectMapper.build();
                streamObject(rdapResponse);
            } catch (Exception e) {
                // TODO do something meaningful coz this aint too meaningful tevs
            }

            if (!found) {
                throw new NotFoundException();
            }
        } catch (QueryException e) {
            if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
            } else {
                throw new RuntimeException("Unexpected result", e);
            }
        }

        streamingMarshal.close();
    }

    protected void streamObject(Object rdapObject) {
        if (rdapObject == null) {
            return;
        }

        streamingMarshal.writeObject(rdapObject);
    }
}
