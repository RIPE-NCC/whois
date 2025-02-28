package net.ripe.db.whois.api.rest;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.api.oauth.BearerTokenExtractor;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.conversion.PasswordFilter;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
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
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
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
    private final SsoTokenTranslator ssoTokenTranslator;
    private final BearerTokenExtractor bearerTokenExtractor;

    @Autowired
    public SyncUpdatesService(final DateTimeProvider dateTimeProvider,
                              final UpdateRequestHandler updateRequestHandler,
                              final UpdatesParser updatesParser,
                              final LoggerContext loggerContext,
                              final SourceContext sourceContext,
                              final BearerTokenExtractor bearerTokenExtractor,
                              final SsoTokenTranslator ssoTokenTranslator) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.updatesParser = updatesParser;
        this.loggerContext = loggerContext;
        this.sourceContext = sourceContext;
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.bearerTokenExtractor = bearerTokenExtractor;
    }

    @GET
    @Path("/{source}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response doGet(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @Encoded @QueryParam(Command.DATA) final String data,
            @QueryParam(Command.HELP) final String help,
            @QueryParam(Command.NEW) final String nnew,
            @QueryParam(Command.DIFF) final String diff,
            @QueryParam(Command.REDIRECT) final String redirect,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @QueryParam(ApiKeyUtils.APIKEY_KEY_ID_QUERY_PARAM) final String apiKeyId,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(decode(data, getCharset(contentType)))
                .setNew(nnew)
                .setHelp(getHelp(help, data))
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setApiKeyId(apiKeyId)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    @POST
    @Path("/{source}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response doUrlEncodedPost(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @FormParam(Command.DATA) final String data,
            @FormParam(Command.HELP) final String help,
            @FormParam(Command.NEW) final String nnew,
            @FormParam(Command.DIFF) final String diff,
            @FormParam(Command.REDIRECT) final String redirect,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @QueryParam(ApiKeyUtils.APIKEY_KEY_ID_QUERY_PARAM) final String apiKeyId,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(data)
                .setNew(nnew)
                .setHelp(getHelp(help, data))
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setApiKeyId(apiKeyId)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    @POST
    @Path("/{source}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response doMultipartPost(
            @Context final HttpServletRequest httpServletRequest,
            @PathParam(SOURCE) final String source,
            @FormDataParam(Command.DATA) final String data,
            @FormDataParam(Command.HELP) final String help,
            @FormDataParam(Command.NEW) final String nnew,
            @FormDataParam(Command.DIFF) final String diff,
            @FormDataParam(Command.REDIRECT) final String redirect,
            @QueryParam(ApiKeyUtils.APIKEY_KEY_ID_QUERY_PARAM) final String apiKeyId,
            @HeaderParam(HttpHeaders.CONTENT_TYPE) final String contentType,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {
        final Request request = new Request.RequestBuilder()
                .setData(data)
                .setNew(nnew)
                .setHelp(getHelp(help, data))
                .setRedirect(redirect)
                .setDiff(diff)
                .setRemoteAddress(httpServletRequest.getRemoteAddr())
                .setSource(source)
                .setApiKeyId(apiKeyId)
                .setSsoToken(crowdTokenKey)
                .build();
        return doSyncUpdate(httpServletRequest, request, getCharset(contentType));
    }

    @Nullable
    private String getHelp(final String help, final String data) {
        if (StringUtils.isEmpty(data)) {
            // default to help
            return "yes";
        } else {
            return help;
        }
    }

    private Response doSyncUpdate(final HttpServletRequest httpServletRequest, final Request request, final Charset charset) {
        loggerContext.init(getRequestId(request.getRemoteAddress()));

        try {
            loggerContext.log(new HttpRequestMessage(httpServletRequest));

            if (!sourceMatchesContext(request.getSource())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid source specified: " + request.getSource()).build();
            }

            if (request.isParam(Command.DIFF)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("the DIFF method is not actually supported by the Syncupdates interface").build();
            }

            if (!request.hasParam(Command.DATA) && request.isParam(Command.NEW)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("DATA parameter is missing").build();
            }

            if (!request.hasParam(Command.DATA) && !request.isParam(Command.HELP)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
            }

            loggerContext.log("msg-in.txt", new SyncUpdateLogCallback(request.toString()));

            final UpdateContext updateContext = new UpdateContext(loggerContext);

            if(RestServiceHelper.isHttpProtocol(httpServletRequest)){
                updateContext.addGlobalMessage(UpdateMessages.httpSyncupdate());
            }

            setSsoSessionToContext(updateContext, request.getSsoToken());
            setClientCertificates(updateContext, httpServletRequest);
            updateContext.setOAuthSession(bearerTokenExtractor.extractBearerToken(httpServletRequest, request.getApiKeyId()));

            final String content = request.hasParam("DATA") ? request.getParam("DATA") : "";

            final UpdateRequest updateRequest = new UpdateRequest(
                    new SyncUpdate(dateTimeProvider, request.getRemoteAddress()),
                    getKeyword(request),
                    updatesParser.parse(updateContext, Lists.newArrayList(new ContentWithCredentials(content, charset))));

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
            } catch (AuthServiceClientException e) {
                loggerContext.log(new Message(Messages.Type.ERROR, e.getMessage()));
                updateContext.addGlobalMessage(RestMessages.ssoAuthIgnored());
            }
        }
    }

    public void setClientCertificates(final UpdateContext updateContext, final HttpServletRequest request) {
        updateContext.setClientCertificates(ClientCertificateExtractor.getClientCertificates(request));
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

        // application/x-www-form-urlencoded is UTF-8 by default
        return StandardCharsets.UTF_8;
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
        return "syncupdate_" + remoteAddress + "_" + dateTimeProvider.getElapsedTime();
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
            outputStream.write(PasswordFilter.filterPasswordsInContents(new String(message.getBytes(), "UTF-8")).getBytes());
        }
    }

    static class Request {
        private final Map<String, String> params;
        private final String remoteAddress;
        private final String source;
        private final String ssoToken;
        private final String apiKeyId;

        private Request(final RequestBuilder requestBuilder) {
            this.params = requestBuilder.params;
            this.remoteAddress = requestBuilder.remoteAddress;
            this.source = requestBuilder.source;
            this.ssoToken = requestBuilder.ssoToken;
            this.apiKeyId = requestBuilder.apiKeyId;
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

        public String getApiKeyId() {
            return apiKeyId;
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
            private String apiKeyId;

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

            public RequestBuilder setApiKeyId(final String apiKeyId) {
                this.apiKeyId = apiKeyId;
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
