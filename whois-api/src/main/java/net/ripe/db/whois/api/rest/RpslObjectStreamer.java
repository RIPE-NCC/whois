package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import net.ripe.db.whois.api.rest.client.StreamingException;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.Version;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.api.rest.marshal.StreamingHelper;
import net.ripe.db.whois.api.rest.marshal.StreamingMarshal;
import net.ripe.db.whois.api.rest.marshal.StreamingMarshalTextPlain;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.IllegalArgumentExceptionMessage;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;


@Component
public class RpslObjectStreamer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpslObjectStreamer.class);

    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final Version version;

    @Autowired
    public RpslObjectStreamer(
            final QueryHandler queryHandler,
            final WhoisObjectServerMapper whoisObjectServerMapper,
            final ApplicationVersion applicationVersion) {
        this.queryHandler = queryHandler;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.version = new Version(
            applicationVersion.getVersion(),
            applicationVersion.getTimestamp(),
            applicationVersion.getCommitId());
    }

    public Response handleQueryAndStreamResponse(final Query query,
                                                  final HttpServletRequest request,
                                                  final InetAddress remoteAddress,
                                                  final Parameters parameters,
                                                  @Nullable final Service service) {
        return Response.ok(new Streamer(request, query, remoteAddress, parameters, service)).build();
    }

    private class Streamer implements StreamingOutput {

        private final HttpServletRequest request;
        private final Query query;
        private final InetAddress remoteAddress;
        private final Parameters parameters;
        private final Service service;
        private StreamingMarshal streamingMarshal;

        public Streamer(
                final HttpServletRequest request,
                final Query query,
                final InetAddress remoteAddress,
                final Parameters parameters,
                final Service service) {
            this.request = request;
            this.query = query;
            this.remoteAddress = remoteAddress;
            this.parameters = parameters;
            this.service = service;
        }

        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
            streamingMarshal = StreamingHelper.getStreamingMarshal(request, output);
            final SearchResponseHandler responseHandler = new SearchResponseHandler();
            try {
                final int contextId = System.identityHashCode(Thread.currentThread());
                queryHandler.streamResults(query, remoteAddress, contextId, responseHandler);

                if (!responseHandler.rpslObjectFound()) {
                    streamingMarshal.throwNotFoundError(request, responseHandler.flushAndGetErrors());
                }
                responseHandler.flushAndGetErrors();
            } catch (StreamingException ignored) {
                LOGGER.debug("{}: {}", ignored.getClass().getName(), ignored.getMessage());
            } catch (QueryException queryException) {
                switch (queryException.getCompletionInfo()) {
                    case DISCONNECTED:
                        responseHandler.flushAndGetErrors();
                        break;
                    default:
                        throw createWebApplicationException(queryException, responseHandler);
                }
            } catch (IllegalArgumentExceptionMessage e) {
                throw new QueryException(QueryCompletionInfo.PARAMETER_ERROR, e.getExceptionMessage());
            } catch (RuntimeException e) {
                throw createWebApplicationException(e, responseHandler);
            }
        }

        private WebApplicationException createWebApplicationException(final RuntimeException exception, final SearchResponseHandler responseHandler) {
            if (exception instanceof WebApplicationException) {
                return (WebApplicationException) exception;
            } else {
                return RestServiceHelper.createWebApplicationException(exception, request, responseHandler.flushAndGetErrors());
            }
        }

        private class SearchResponseHandler extends ApiResponseHandler {
            private boolean rpslObjectFound;

            private final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
            private final List<Message> errors = Lists.newArrayList();
            private final int offset = parameters.getOffset() != null ? parameters.getOffset() : 0;
            private final int limit = parameters.getLimit() != null ? parameters.getLimit() : Integer.MAX_VALUE;
            private int count = 0;

            // TODO: [AH] replace this 'if instanceof' mess with an OO approach
            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof RpslObject) {
                    streamRpslObject((RpslObject) responseObject);
                } else if (responseObject instanceof MessageObject) {
                    final Message message = ((MessageObject) responseObject).getMessage();
                    if (message != null && Messages.Type.INFO != message.getType()) {
                        errors.add(message);
                    }
                }
            }

            private void streamRpslObject(final RpslObject rpslObject) {
                if (!rpslObjectFound) {
                    rpslObjectFound = true;
                    startStreaming();
                }
                streamObject(rpslObjectQueue.poll());
                rpslObjectQueue.add(rpslObject);
            }

            private void startStreaming() {
                streamingMarshal.open();

                if (service != null) {
                    streamingMarshal.write("service", service);

                    // only return parameters for search service
                    streamingMarshal.write("parameters", parameters);
                }

                streamingMarshal.start("objects");
                streamingMarshal.startArray("object");
            }

            private void streamObject(@Nullable final RpslObject rpslObject) {
                if (rpslObject == null) {
                    return;
                }

                if (!withinOffset(count++, offset)) {
                    return;
                }

                if (!withinLimit(count, limit, offset)) {
                    // stop returning objects once limit is reached
                    throw new QueryException(QueryCompletionInfo.DISCONNECTED);
                }

                final WhoisObject whoisObject = whoisObjectServerMapper.map(rpslObject, parameters);
                whoisObjectServerMapper.mapAbuseContact(whoisObject, parameters, rpslObject);
                whoisObjectServerMapper.mapManagedAttributes(whoisObject, parameters, rpslObject);
                whoisObjectServerMapper.mapResourceHolder(whoisObject, parameters, rpslObject);
                whoisObjectServerMapper.mapObjectMessages(whoisObject, parameters, rpslObject);

                if (streamingMarshal instanceof StreamingMarshalTextPlain) {
                    streamingMarshal.writeArray(rpslObject);
                } else {
                    streamingMarshal.writeArray(whoisObject);
                }
            }

            private boolean withinOffset(final int count, final int offset) {
                return count >= offset;
            }

            private boolean withinLimit(final int count, final int limit, final int offset) {
                return limit == Integer.MAX_VALUE || count <= (limit + offset);
            }

            public boolean rpslObjectFound() {
                return rpslObjectFound;
            }

            public List<Message> flushAndGetErrors() {
                if (!rpslObjectFound) {
                    return errors;
                }
                streamObject(rpslObjectQueue.poll());

                streamingMarshal.endArray();

                streamingMarshal.end("objects");
                if (errors.size() > 0) {
                    streamingMarshal.write("errormessages", RestServiceHelper.createErrorMessages(errors));
                    errors.clear();
                }

                streamingMarshal.write("terms-and-conditions", Link.create(WhoisResources.TERMS_AND_CONDITIONS));
                streamingMarshal.write("version", version);
                streamingMarshal.end("whois-resources");
                streamingMarshal.close();
                return errors;
            }
        }

    }

}
