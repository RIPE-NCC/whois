package net.ripe.db.whois.api.rest;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LogCallback;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Map;

@Component
@Path("/syncupdates")
public class SyncUpdatesService {

    private static final Splitter SEMICOLON_SPLITTER = Splitter.on(';').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();

    private static final String SOURCE = "source";

    private final DateTimeProvider dateTimeProvider;
    private final UpdateRequestHandler updateRequestHandler;
    private final UpdatesParser updatesParser;
    private final LoggerContext loggerContext;
    private final SourceContext sourceContext;
    private final IpRanges ipRanges;
    private final SsoTokenTranslator ssoTokenTranslator;

    @Autowired
    public SyncUpdatesService(final DateTimeProvider dateTimeProvider,
                              final UpdateRequestHandler updateRequestHandler,
                              final UpdatesParser updatesParser,
                              final LoggerContext loggerContext,
                              final SourceContext sourceContext,
                              final IpRanges ipRanges,
                              final SsoTokenTranslator ssoTokenTranslator) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.updatesParser = updatesParser;
        this.loggerContext = loggerContext;
        this.sourceContext = sourceContext;
        this.ipRanges = ipRanges;
        this.ssoTokenTranslator = ssoTokenTranslator;
    }

    @GET
    @Path("/{source}")
    public Response doGet(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @Encoded @QueryParam(Command.DATA) final String data,
            @QueryParam(Command.HELP) final String help,
            @QueryParam(Command.NEW) final String nnew,
            @QueryParam(Command.DIFF) final String diff,
            @QueryParam(Command.REDIRECT) final String redirect,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(decode(data, getCharset(contentType)))
                .setNew(nnew)
                .setHelp(help)
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    @POST
    @Path("/{source}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response doUrlEncodedPost(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @FormParam(Command.DATA) final String data,
            @FormParam(Command.HELP) final String help,
            @FormParam(Command.NEW) final String nnew,
            @FormParam(Command.DIFF) final String diff,
            @FormParam(Command.REDIRECT) final String redirect,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(data)
                .setNew(nnew)
                .setHelp(help)
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    @POST
    @Path("/{source}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response doMultipartPost(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @FormDataParam(Command.DATA) final String data,
            @FormDataParam(Command.HELP) final String help,
            @FormDataParam(Command.NEW) final String nnew,
            @FormDataParam(Command.DIFF) final String diff,
            @FormDataParam(Command.REDIRECT) final String redirect,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(data)
                .setNew(nnew)
                .setHelp(help)
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    private Response doSyncUpdate(final HttpServletRequest httpServletRequest, final Request request, final Charset charset) {
        loggerContext.init(getRequestId(request.getRemoteAddress()));

        try {
            if (!sourceMatchesContext(request.getSource())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid source specified: " + request.getSource()).build();
            }

            if (request.isParam(Command.DIFF)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("the DIFF method is not actually supported by the Syncupdates interface").build();
            }

            boolean notificationsEnabled = true;
            if (request.isParam(Command.REDIRECT)) {
                if (!ipRanges.isTrusted(IpInterval.parse(request.getRemoteAddress()))) {
                    return Response.status(Response.Status.FORBIDDEN).entity("Not allowed to disable notifications: " + request.getRemoteAddress()).build();
                }

                notificationsEnabled = false;
            }

            if (!request.hasParam(Command.DATA) && request.isParam(Command.NEW)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("DATA parameter is missing").build();
            }

            if (!request.hasParam(Command.DATA) && !request.isParam(Command.HELP)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }

            InternalUpdatePerformer.logHttpHeaders(loggerContext, httpServletRequest);
            InternalUpdatePerformer.logHttpUri(loggerContext, httpServletRequest);

            loggerContext.log("msg-in.txt", new SyncUpdateLogCallback(request.toString()));

            final UpdateContext updateContext = new UpdateContext(loggerContext);

            setSsoSessionToContext(updateContext, request.getSsoToken());

            final String content = request.hasParam("DATA") ? request.getParam("DATA") : "";

            final UpdateRequest updateRequest = new UpdateRequest(
                    new SyncUpdate(dateTimeProvider, request.getRemoteAddress()),
                    getKeyword(request),
                    content,
                    updatesParser.parse(updateContext, Lists.newArrayList(new ContentWithCredentials(content, charset))),
                    notificationsEnabled);

            final UpdateResponse updateResponse = updateRequestHandler.handle(updateRequest, updateContext);
            loggerContext.log("msg-out.txt", new SyncUpdateLogCallback(updateResponse.getResponse()));
            return getResponse(updateResponse);

        } finally {
            loggerContext.remove();
        }
    }

    private void setSsoSessionToContext(final UpdateContext updateContext, final String ssoToken) {
        if (!StringUtils.isBlank(ssoToken)) {
            try {
                updateContext.setUserSession(ssoTokenTranslator.translateSsoToken(ssoToken));
            } catch (CrowdClientException e) {
                loggerContext.log(new Message(Messages.Type.ERROR, e.getMessage()));
                updateContext.addGlobalMessage(RestMessages.ssoAuthIgnored());
            }
        }
    }

    private Response getResponse(final UpdateResponse updateResponse) {
        final Response.Status status = getResponseStatus(updateResponse);
        if (status != Response.Status.OK) {
            return Response
                    .status(status)
                    .entity(updateResponse.getResponse())
                    .build();
        }

        return Response
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_LENGTH, updateResponse.getResponse().length())
                .entity(updateResponse.getResponse())
                .build();
    }

    private Response.Status getResponseStatus(final UpdateResponse updateResponse) {
        switch (updateResponse.getStatus()) {
            case EXCEPTION:
                return Response.Status.INTERNAL_SERVER_ERROR;
            case FAILED_AUTHENTICATION:
                return Response.Status.UNAUTHORIZED;
            default:
                return Response.Status.OK;
        }
    }

    private Keyword getKeyword(final Request request) {
        if (request.isParam(Command.HELP)) {
            return Keyword.HELP;
        }

        if (request.isParam(Command.NEW)) {
            return Keyword.NEW;
        }

        if (request.isParam(Command.DIFF)) {
            return Keyword.DIFF;
        }

        return Keyword.NONE;
    }

    private Charset getCharset(final String contentType) {
        if (contentType != null && contentType.length() > 0) {
            final String charset = getHeaderParameter(contentType, "charset");
            if (charset != null && charset.length() > 0) {
                try {
                    return Charset.forName(charset.toUpperCase());
                } catch (UnsupportedCharsetException e) {
                    loggerContext.log(new Message(Messages.Type.INFO, "Unsupported charset: %s, using default.", charset));
                } catch (IllegalCharsetNameException e) {
                    loggerContext.log(new Message(Messages.Type.INFO, "Illegal charset name: %s, using default.", charset));
                }
            }
        }
        return Charsets.ISO_8859_1;
    }

    @Nullable
    private String getHeaderParameter(final String header, final String parameter) {
        if (header != null) {
            for (final String next : SEMICOLON_SPLITTER.split(header)) {
                final Iterator<String> pair = EQUALS_SPLITTER.split(next).iterator();
                if (pair.hasNext()) {
                    final String key = pair.next().trim();
                    if (key.equalsIgnoreCase(parameter) && pair.hasNext()) {
                        return pair.next().replace('\"', ' ').trim();
                    }
                }
            }
        }

        return null;
    }

    // Characters in query params in GET requests are not decoded properly, so use @Encoded and decode ourselves
    @Nullable
    private String decode(final String data, final Charset charset) {
        if (data == null) {
            return null;
        }
        try {
            return URLDecoder.decode(data, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private String getRequestId(final String remoteAddress) {
        return "syncupdate_" + remoteAddress + "_" + dateTimeProvider.getNanoTime();
    }

    private boolean sourceMatchesContext(final String source) {
        return (source != null) && sourceContext.getCurrentSource().getName().equals(CIString.ciString(source));
    }

    class SyncUpdateLogCallback implements LogCallback {
        private final String message;

        public SyncUpdateLogCallback(final String message) {
            this.message = message;
        }

        @Override
        public void log(final OutputStream outputStream) throws IOException {
            outputStream.write(message.getBytes());
        }
    }

    static class Request {
        private final Map<String, String> params;
        private final String remoteAddress;
        private final String source;
        private final String ssoToken;

        private Request(final RequestBuilder requestBuilder) {
            this.params = requestBuilder.params;
            this.remoteAddress = requestBuilder.remoteAddress;
            this.source = requestBuilder.source;
            this.ssoToken = requestBuilder.ssoToken;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public String getSource() {
            return source;
        }

        public String getSsoToken() {
            return ssoToken;
        }

        public boolean hasParam(final String key) {
            final String value = params.get(key);
            return value != null && value.length() > 0;
        }

        public boolean isParam(final String key) {
            return hasParam(key) && !"NO".equalsIgnoreCase(params.get(key));
        }

        public String getParam(final String key) {
            return params.get(key);
        }

        public int size() {
            return params.size();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("REQUEST FROM:");
            builder.append(remoteAddress);
            builder.append("\nPARAMS:\n");
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                final String key = next.getKey();
                builder.append(key);
                builder.append('=');
                if (key.equalsIgnoreCase(Command.DATA)) {
                    builder.append("\n\n");
                }
                builder.append(next.getValue());
                if (iterator.hasNext()) {
                    builder.append('\n');
                }
            }

            return builder.toString();
        }

        static class RequestBuilder {
            private final Map<String, String> params = Maps.newHashMap();
            private String remoteAddress;
            private String source;
            private String ssoToken;

            public RequestBuilder setData(final String data) {
                params.put(Command.DATA, data);
                return this;
            }

            public RequestBuilder setNew(final String flag) {
                params.put(Command.NEW, flag);
                return this;
            }

            public RequestBuilder setHelp(final String help) {
                params.put(Command.HELP, help);
                return this;
            }

            public RequestBuilder setRedirect(final String redirect) {
                params.put(Command.REDIRECT, redirect);
                return this;
            }

            public RequestBuilder setDiff(final String diff) {
                params.put(Command.DIFF, diff);
                return this;
            }

            public RequestBuilder setRemoteAddress(final String remoteAddress) {
                this.remoteAddress = remoteAddress;
                return this;
            }

            public RequestBuilder setSource(final String source) {
                this.source = source;
                return this;
            }

            public RequestBuilder setSsoToken(final String ssoToken) {
                this.ssoToken = ssoToken;
                return this;
            }

            public Request build() {
                return new Request(this);
            }
        }
    }

    private class Command {
        private Command(){}

        static final String DATA = "DATA";
        static final String HELP = "HELP";
        static final String NEW = "NEW";
        static final String DIFF = "DIFF";
        static final String REDIRECT = "REDIRECT";
    }
}
