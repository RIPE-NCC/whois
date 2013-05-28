package net.ripe.db.whois.api.whois;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.api.whois.domain.WhoisTag;
import net.ripe.db.whois.api.whois.domain.WhoisVersions;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisService {
    protected static final int STATUS_TOO_MANY_REQUESTS = 429;

    protected static final String TEXT_JSON = "text/json";
    protected static final String TEXT_XML = "text/xml";

    protected final DateTimeProvider dateTimeProvider;
    protected final UpdateRequestHandler updateRequestHandler;
    protected final LoggerContext loggerContext;
    protected final RpslObjectDao rpslObjectDao;
    protected final RpslObjectUpdateDao rpslObjectUpdateDao;
    protected final QueryHandler queryHandler;

    @Autowired
    protected SourceContext sourceContext;

    @Autowired
    public WhoisService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.loggerContext = loggerContext;
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
    }

    // TODO: [AH] hierarchical lookups return the encompassing range if no direct hit
    protected Response lookupObject(
            final HttpServletRequest request,
            final String source,
            final String objectTypeString,
            final String key,
            final boolean isGrs) {
        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s",
                QueryFlag.NO_GROUPING.getLongFlag(),
                QueryFlag.NO_REFERENCED.getLongFlag(),
                QueryFlag.SOURCES.getLongFlag(),
                source,
                QueryFlag.SELECT_TYPES.getLongFlag(),
                objectTypeString,
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                key));

        checkForInvalidSource(source, isGrs);

        return handleQuery(query, source, key, request, null);
    }

    protected Response handleQuery(final Query query, final String source, final String key, final HttpServletRequest request, @Nullable final Parameters parameters) {
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final int contextId = System.identityHashCode(Thread.currentThread());

        if (query.isVersionList() || query.isObjectVersion()) {
            return handleVersionQuery(query, source, key, remoteAddress, contextId);
        }

        return handleQueryAndStreamResponse(query, request, remoteAddress, contextId, parameters);
    }

    // TODO: [AH] refactor this spaghetti
    private Response handleVersionQuery(final Query query, final String source, final String key, final InetAddress remoteAddress, final int contextId) {
        final ApiResponseHandlerVersions apiResponseHandlerVersions = new ApiResponseHandlerVersions();
        queryHandler.streamResults(query, remoteAddress, contextId, apiResponseHandlerVersions);

        final VersionWithRpslResponseObject versionResponseObject = apiResponseHandlerVersions.getVersionWithRpslResponseObject();
        final List<DeletedVersionResponseObject> deleted = apiResponseHandlerVersions.getDeletedObjects();
        final List<VersionResponseObject> versions = apiResponseHandlerVersions.getVersionObjects();

        if (versionResponseObject == null && versions.isEmpty()) {
            if (deleted.isEmpty() || query.isObjectVersion()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }

        final WhoisResources whoisResources = new WhoisResources();

        if (versionResponseObject != null) {
            final WhoisObject whoisObject = WhoisObjectMapper.map(versionResponseObject.getRpslObject());
            whoisObject.setVersion(versionResponseObject.getVersion());
            whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        } else {
            final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
            final WhoisVersions whoisVersions = new WhoisVersions(source, type, key, WhoisObjectMapper.mapVersions(deleted, versions));
            whoisResources.setVersions(whoisVersions);
        }

        return Response.ok(whoisResources).build();
    }

    private Response handleQueryAndStreamResponse(final Query query, final HttpServletRequest request, final InetAddress remoteAddress, final int contextId, @Nullable final Parameters parameters) {
        final StreamingMarshal streamingMarshal = getStreamingMarshal(request);

        return Response.ok(new StreamingOutput() {
            private boolean found;

            @Override
            public void write(final OutputStream output) throws IOException {
                streamingMarshal.open(output);
                streamingMarshal.start("whois-resources");

                if (parameters != null) {
                    streamingMarshal.write("parameters", parameters);
                }

                streamingMarshal.start("objects");

                // TODO [AK] Crude way to handle tags, but working
                final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
                final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();

                try {
                    queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                        @Override
                        public void handle(final ResponseObject responseObject) {
                            if (responseObject instanceof TagResponseObject) {
                                tagResponseObjects.add((TagResponseObject) responseObject);
                            } else if (responseObject instanceof RpslObject) {
                                found = true;
                                streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                                rpslObjectQueue.add((RpslObject) responseObject);
                            }

                            // TODO [AK] Handle related messages
                        }
                    });

                    streamObject(rpslObjectQueue.poll(), tagResponseObjects);

                    if (!found) {
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }
                } catch (QueryException e) {
                    if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                        throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
                    } else {
                        throw e;
                    }
                }

                streamingMarshal.close();
            }

            private void streamObject(@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
                if (rpslObject == null) {
                    return;
                }

                final WhoisObject whoisObject = WhoisObjectMapper.map(rpslObject);

                // TODO [AK] Fix mapper API
                final List<WhoisTag> tags = WhoisObjectMapper.mapTags(tagResponseObjects).getTags();
                whoisObject.setTags(tags);

                streamingMarshal.write("object", whoisObject);
                tagResponseObjects.clear();
            }
        }).build();
    }

    private StreamingMarshal getStreamingMarshal(final HttpServletRequest request) {
        final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        for (final String accept : Splitter.on(',').split(acceptHeader)) {
            try {
                final MediaType mediaType = MediaType.valueOf(accept);
                final String subtype = mediaType.getSubtype().toLowerCase();
                if (subtype.equals("json") || subtype.endsWith("+json")) {
                    return new StreamingMarshalJson();
                } else if (subtype.equals("xml") || subtype.endsWith("+xml")) {
                    return new StreamingMarshalXml();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new StreamingMarshalXml();
    }

}
