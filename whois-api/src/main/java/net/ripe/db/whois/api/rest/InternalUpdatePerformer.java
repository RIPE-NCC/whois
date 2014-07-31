package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.CrowdClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LogCallback;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InternalUpdatePerformer {
    private final UpdateRequestHandler updateRequestHandler;
    private final DateTimeProvider dateTimeProvider;
    private final WhoisObjectMapper whoisObjectMapper;
    private final LoggerContext loggerContext;
    private final SsoTokenTranslator ssoTokenTranslator;

    @Autowired
    public InternalUpdatePerformer(final UpdateRequestHandler updateRequestHandler,
                                   final DateTimeProvider dateTimeProvider,
                                   final WhoisObjectMapper whoisObjectMapper,
                                   final LoggerContext loggerContext,
                                   final SsoTokenTranslator ssoTokenTranslator) {
        this.updateRequestHandler = updateRequestHandler;
        this.dateTimeProvider = dateTimeProvider;
        this.whoisObjectMapper = whoisObjectMapper;
        this.loggerContext = loggerContext;
        this.ssoTokenTranslator = ssoTokenTranslator;
    }

    public UpdateContext initContext(final Origin origin, final String ssoToken) {
        loggerContext.init(getRequestId(origin.getFrom()));
        final UpdateContext updateContext = new UpdateContext(loggerContext);
        setSsoSessionToContext(updateContext, ssoToken);
        return updateContext;
    }

    public void closeContext() {
        loggerContext.remove();
    }

    public Response performUpdate(final UpdateContext updateContext, final Origin origin, final Update update,
                                  final String content, final Keyword keyword, final HttpServletRequest request) {

        logHttpHeaders(loggerContext, request);
        logHttpUri(loggerContext, request);

        loggerContext.log("msg-in.txt", new UpdateLogCallback(update));

        final UpdateRequest updateRequest = new UpdateRequest(origin, keyword, content, Collections.singletonList(update), true);
        updateRequestHandler.handle(updateRequest, updateContext);

        final Response.ResponseBuilder responseBuilder;
        UpdateStatus status = updateContext.getStatus(update);
        if (status == UpdateStatus.SUCCESS) {
            responseBuilder = Response.status(Response.Status.OK);
        } else if (status == UpdateStatus.FAILED_AUTHENTICATION) {
            responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
        } else if (status == UpdateStatus.EXCEPTION) {
            responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        } else if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
            responseBuilder = Response.status(Response.Status.CONFLICT);
        } else {
            responseBuilder = Response.status(Response.Status.BAD_REQUEST);
        }

        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                final WhoisResources result = createResponse(request, updateContext, update);
                StreamingHelper.getStreamingMarshal(request, output).singleton(result);
            }
        }).build();
    }

    private WhoisResources createResponse(final HttpServletRequest request, final UpdateContext updateContext, final Update update) {
        final WhoisResources whoisResources = new WhoisResources();
        // global messages
        final List<ErrorMessage> errorMessages = Lists.newArrayList();
        for (Message message : updateContext.getGlobalMessages().getAllMessages()) {
            errorMessages.add(new ErrorMessage(message));
        }

        // object messages
        for (Message message : updateContext.getMessages(update).getMessages().getAllMessages()) {
            errorMessages.add(new ErrorMessage(message));
        }

        // attribute messages
        for (Map.Entry<RpslAttribute, Messages> entry : updateContext.getMessages(update).getAttributeMessages().entrySet()) {
            RpslAttribute rpslAttribute = entry.getKey();
            for (Message message : entry.getValue().getAllMessages()) {
                errorMessages.add(new ErrorMessage(message, rpslAttribute));
            }
        }

        if (!errorMessages.isEmpty()) {
            whoisResources.setErrorMessages(errorMessages);
        }

        final PreparedUpdate preparedUpdate = updateContext.getPreparedUpdate(update);
        if (preparedUpdate != null) {
            final WhoisObject whoisObject = whoisObjectMapper.map(preparedUpdate.getUpdatedObject(), RestServiceHelper.getServerAttributeMapper(request.getQueryString()));
            whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        }

        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }


    public Update createUpdate(final UpdateContext updateContext, final RpslObject rpslObject, final List<String> passwords, final String deleteReason, final String override) {
        return new Update(
                createParagraph(updateContext, rpslObject, passwords, override),
                deleteReason != null ? Operation.DELETE : Operation.UNSPECIFIED,
                deleteReason != null ? Lists.newArrayList(deleteReason) : null,
                rpslObject);
    }

    private Paragraph createParagraph(final UpdateContext updateContext, final RpslObject rpslObject, final List<String> passwords, final String override) {
        final Set<Credential> credentials = Sets.newHashSet();
        for (String password : passwords) {
            credentials.add(new PasswordCredential(password));
        }

        if (override != null) {
            credentials.add(OverrideCredential.parse(override));
        }

        if (updateContext.getUserSession() != null) {
            credentials.add(SsoCredential.createOfferedCredential(updateContext.getUserSession()));
        }

        return new Paragraph(rpslObject.toString(), new Credentials(credentials));
    }

    public Origin createOrigin(final HttpServletRequest request) {
        return new WhoisRestApi(dateTimeProvider, request.getRemoteAddr());
    }

    public String createContent(final RpslObject rpslObject, final List<String> passwords, final String deleteReason, String override) {
        final StringBuilder builder = new StringBuilder();
        builder.append(rpslObject.toString());

        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }

        if (deleteReason != null) {
            builder.append("delete: ");
            builder.append(deleteReason);
            builder.append("\n\n");
        }

        if (passwords != null) {
            for (final String password : passwords) {
                builder.append("password: ");
                builder.append(password);
                builder.append('\n');
            }
        }

        if (override != null) {
            builder.append("override: ");
            builder.append(override);
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private String getRequestId(final String remoteAddress) {
        return String.format("rest_%s_%s", remoteAddress, dateTimeProvider.getNanoTime());
    }

    public void setSsoSessionToContext(final UpdateContext updateContext, final String ssoToken) {
        if (!StringUtils.isBlank(ssoToken)) {
            try {
                updateContext.setUserSession(ssoTokenTranslator.translateSsoToken(ssoToken));
            } catch (CrowdClientException e) {
                loggerContext.log(new Message(Messages.Type.ERROR, e.getMessage()));
                updateContext.addGlobalMessage(RestMessages.ssoAuthIgnored());
            }
        }
    }

    // TODO: [AH] format logging of this properly (e.g. add proper global message support for headers and request url
    public static void logHttpHeaders(final LoggerContext loggerContext, final HttpServletRequest request) {
        final Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                loggerContext.log(new Message(Messages.Type.INFO, String.format("Header: %s=%s", name, values.nextElement())));
            }
        }
    }

    public static void logHttpUri(final LoggerContext loggerContext, final HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getQueryString())) {
            loggerContext.log(new Message(Messages.Type.INFO, request.getRequestURI()));
        } else {
            loggerContext.log(new Message(Messages.Type.INFO, String.format("%s?%s", request.getRequestURI(), request.getQueryString())));
        }
    }

    class UpdateLogCallback implements LogCallback {
        private final Update update;

        public UpdateLogCallback(final Update update) {
            this.update = update;
        }

        @Override
        public void log(final OutputStream outputStream) throws IOException {
            outputStream.write(update.toString().getBytes());
        }
    }

}
