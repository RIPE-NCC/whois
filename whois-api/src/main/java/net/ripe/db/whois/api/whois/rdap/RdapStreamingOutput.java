package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.ApiResponseHandler;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.WhoisStreamingOutput;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

        streamingMarshal.start("");

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
            Object rdapResponse;

            rdapResponse = rdapObjectMapper.build();
            streamObject(rdapResponse);

        } catch (QueryException e) {
            if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
            } else {
                throw new RuntimeException("Unexpected result", e);
            }
        } catch (Exception e) {
            throw new WebApplicationException(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity(e.toString())
                        .build()
            );
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
