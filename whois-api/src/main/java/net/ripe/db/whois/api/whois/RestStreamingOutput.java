package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
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

public class RestStreamingOutput extends WhoisStreamingOutput {

    public RestStreamingOutput(StreamingMarshal streamingMarshal, QueryHandler queryHandler, Parameters parameters, Query query, InetAddress remoteAddress, int contextId) {
        super(streamingMarshal,queryHandler,parameters,query,remoteAddress,contextId);
    }

    @Override
    public void write(final OutputStream output) throws IOException {
        streamingMarshal.open(output);
        streamingMarshal.start("whois-resources");

        if (parameters != null) {
            streamingMarshal.write("parameters", parameters);
        }

        streamingMarshal.start("objects");

        // TODO [AK] Crude way to handle tags, but working
        final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<RpslObject>(1);
        final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();

        try {
            queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof TagResponseObject) {
                        tagResponseObjects.add((TagResponseObject) responseObject);
                    } else if (responseObject instanceof RpslObject) {
                        found = true;
                        WhoisObject wo = getWhoisObject((RpslObject) responseObject, tagResponseObjects);
                        streamObject(wo);
                        rpslObjectQueue.add((RpslObject) responseObject);
                    }

                    // TODO [AK] Handle related messages
                }
            });

            WhoisObject wo = getWhoisObject(rpslObjectQueue.poll(), tagResponseObjects);
            streamObject(wo);

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
        final WhoisObject whoisObject = WhoisObjectMapper.map(rpslObject, tagResponseObjects);

        tagResponseObjects.clear();

        return whoisObject;
    }

    private void streamObject(Object whoisObject) {
        if (whoisObject == null) {
            return;
        }

        streamingMarshal.write("object", whoisObject);
    }
}
