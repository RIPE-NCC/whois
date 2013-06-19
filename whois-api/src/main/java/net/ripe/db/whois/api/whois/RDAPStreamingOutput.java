package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.RdapObject;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.api.whois.domain.WhoisTag;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;

import javax.annotation.Nullable;
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

        // TODO [AK] Crude way to handle tags, but working
        final Queue<WhoisObject> whoisObjectQueue = new ArrayDeque<WhoisObject>(1);
        final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();

        try {
            queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof TagResponseObject) {
                        tagResponseObjects.add((TagResponseObject) responseObject);
                    } else if (responseObject instanceof RpslObject) {
                        found = true;
                        whoisObjectQueue.add(getWhoisObject((RpslObject) responseObject, tagResponseObjects));
                        tagResponseObjects.clear();
                    }

                    // TODO [AK] Handle related messages
                }
            });

            /*RpslObject ro;

            while (!rpslObjectQueue.isEmpty()) {
                ro = rpslObjectQueue.poll();
                WhoisObject wo = getWhoisObject(ro, tagResponseObjects);
                System.out.println(wo.getType());
            }*/
            RdapObjectMapper rdapObjectMapper = new RdapObjectMapper(whoisObjectQueue);
            RdapObject rdapObject;

            try {
                rdapObject = rdapObjectMapper.build();
                streamObject(rdapObject);
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

    protected WhoisObject getWhoisObject (@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
        final WhoisObject whoisObject = WhoisObjectMapper.map(rpslObject);
        final List<WhoisTag> tags = WhoisObjectMapper.mapTags(tagResponseObjects).getTags();
        whoisObject.setTags(tags);
        return whoisObject;
    }

    protected void streamObject(Object rdapObject) {
        if (rdapObject == null) {
            return;
        }

        streamingMarshal.write("", rdapObject);
    }
}
