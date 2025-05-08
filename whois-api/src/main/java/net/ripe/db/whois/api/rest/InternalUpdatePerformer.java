package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.UpdateCreator;
import net.ripe.db.whois.api.oauth.BearerTokenExtractor;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.rest.marshal.StreamingHelper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.conversion.PasswordFilter;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.Credentials.OAuthCredential;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.common.Credentials.ClientCertificateCredential;
import net.ripe.db.whois.common.Credentials.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.common.Credentials.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.common.Credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.common.Credentials.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.common.x509.X509CertificateWrapper;
import net.ripe.db.whois.update.log.LogCallback;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
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
    private final BearerTokenExtractor bearerTokenExtractor;

    @Autowired
    public InternalUpdatePerformer(final UpdateRequestHandler updateRequestHandler,
                                   final DateTimeProvider dateTimeProvider,
                                   final WhoisObjectMapper whoisObjectMapper,
                                   final LoggerContext loggerContext,
                                   final BearerTokenExtractor bearerTokenExtractor,
                                   final SsoTokenTranslator ssoTokenTranslator) {
        this.updateRequestHandler = updateRequestHandler;
        this.dateTimeProvider = dateTimeProvider;
        this.whoisObjectMapper = whoisObjectMapper;
        this.loggerContext = loggerContext;
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.bearerTokenExtractor = bearerTokenExtractor;
    }

    public UpdateContext initContext(final Origin origin, final String ssoToken, final String apiKeyId, final HttpServletRequest request) {
        loggerContext.init(getRequestId(origin.getFrom()));
        final UpdateContext updateContext = new UpdateContext(loggerContext);
        setSsoSessionToContext(updateContext, ssoToken);
        setClientCertificates(updateContext, request);
        setOAuthSession(updateContext, apiKeyId, request);
        return updateContext;
    }

    public void closeContext() {
        loggerContext.remove();
    }

    public WhoisResources performUpdate(final UpdateContext updateContext, final Origin origin, final Update update, final Keyword keyword, final HttpServletRequest request) {
        return performUpdates(updateContext, origin, Collections.singletonList(update), keyword, request);
    }

    public WhoisResources performUpdates(final UpdateContext updateContext, final Origin origin, final Collection<Update> updates, final Keyword keyword, final HttpServletRequest request) {
        loggerContext.log("msg-in.txt", new UpdateLogCallback(updates));

        // todo: [TK] use response?
        updateRequestHandler.handle(new UpdateRequest(origin, keyword, updates), updateContext);

        return performUpdates(request, updateContext, updates);
    }

    public Response createResponse(final UpdateContext updateContext, final WhoisResources whoisResources, final Update update, final HttpServletRequest request) {
        final Response.ResponseBuilder responseBuilder;
        switch (updateContext.getStatus(update)) {
            case SUCCESS:
                responseBuilder = Response.status(Response.Status.OK);
                break;
            case FAILED_AUTHENTICATION:
                responseBuilder = Response.status(Response.Status.UNAUTHORIZED);
                    break;
            case EXCEPTION:
                responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                break;
            case FAILED:
            default: {
                if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                    responseBuilder = Response.status(Response.Status.CONFLICT);
                } else {
                    if (updateContext.getMessages(update).contains(UpdateMessages.unexpectedError())) {
                        responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                    } else {
                        responseBuilder = Response.status(Response.Status.BAD_REQUEST);
                    }
                }
            }
        }

        return responseBuilder.entity(new StreamingResponse(request, whoisResources)).build();
    }

    private WhoisResources performUpdates(final HttpServletRequest request, final UpdateContext updateContext, final Collection<Update> updates) {
        final WhoisResources whoisResources = new WhoisResources();

        // global messages
        final List<ErrorMessage> errorMessages = Lists.newArrayList();
        for (Message message : updateContext.getGlobalMessages().getAllMessages()) {
            errorMessages.add(new ErrorMessage(message));
        }

        for (Update update : updates) {
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
        }

        final List<WhoisObject> whoisObjects = Lists.newArrayList();
        for (Update update : updates) {
            final PreparedUpdate preparedUpdate = updateContext.getPreparedUpdate(update);

            //Be careful here, we do not want unsuccessful DELETE operations to return the mntner objects from the DB!!!
            if (preparedUpdate == null
                    || (preparedUpdate.getAction() == Action.DELETE
                    && updateContext.getStatus(update) != UpdateStatus.SUCCESS)) {
                continue;
            }

            whoisObjects.add(whoisObjectMapper.map(preparedUpdate.getUpdatedObject(), RestServiceHelper.getRestResponseAttributeMapper(request.getQueryString())));
        }

        if (!whoisObjects.isEmpty()) {
            whoisResources.setWhoisObjects(whoisObjects);
        }

        if (!errorMessages.isEmpty()) {
            whoisResources.setErrorMessages(errorMessages);
        }

        whoisResources.setLink(Link.create(RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    public Update createUpdate(final UpdateContext updateContext, final RpslObject rpslObject, final List<String> passwords, final String deleteReason, final String override) {
        return UpdateCreator.createUpdate(
                createParagraph(updateContext, rpslObject, passwords, override),
                deleteReason != null ? Operation.DELETE : Operation.UNSPECIFIED,
                deleteReason != null ? Lists.newArrayList(deleteReason) : null,
                rpslObject.toString(),
                updateContext
        );
    }

    private static Paragraph createParagraph(final UpdateContext updateContext, final RpslObject rpslObject, final List<String> passwords, final String override) {
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

        if (updateContext.getClientCertificates() != null) {
            for (X509CertificateWrapper clientCertificate : updateContext.getClientCertificates()) {
                credentials.add(ClientCertificateCredential.createOfferedCredential(clientCertificate));
            }
        }

        if (updateContext.getOAuthSession() != null) {
            credentials.add(OAuthCredential.createOfferedCredential(updateContext.getOAuthSession()));
        }

        return new Paragraph(rpslObject.toString(), new Credentials(credentials));
    }

    public Origin createOrigin(final HttpServletRequest request) {
        return new WhoisRestApi(dateTimeProvider, request.getRemoteAddr());
    }

    private String getRequestId(final String remoteAddress) {
        return String.format("rest_%s_%s", remoteAddress, dateTimeProvider.getElapsedTime());
    }

    public void setSsoSessionToContext(final UpdateContext updateContext, final String ssoToken) {
        if (!StringUtils.isBlank(ssoToken)) {
            try {
                updateContext.setUserSession(ssoTokenTranslator.translateSsoToken(ssoToken));
            } catch (AuthServiceClientException e) {
                logError(e);
                updateContext.addGlobalMessage(RestMessages.ssoAuthIgnored());
            }
        }
    }

    private void setOAuthSession(final UpdateContext updateContext, final String apiKeyId, final HttpServletRequest request) {
        updateContext.setOAuthSession(bearerTokenExtractor.extractBearerToken(request, apiKeyId));
    }

    public void setClientCertificates(final UpdateContext updateContext, final HttpServletRequest request) {
        updateContext.setClientCertificates(ClientCertificateExtractor.getClientCertificates(request));
    }

    public void logInfo(final String message) {
        loggerContext.log(new Message(Messages.Type.INFO, message));
    }

    public void logWarning(final String message) {
        loggerContext.log(new Message(Messages.Type.WARNING, message));
    }

    public void logError(final Throwable t) {
        loggerContext.log(new Message(Messages.Type.ERROR, t.getMessage()), t);
    }

    class UpdateLogCallback implements LogCallback {
        private final Collection<Update> updates;

        public UpdateLogCallback(final Collection<Update> updates) {
            this.updates = updates;
        }

        @Override
        public void log(final OutputStream outputStream) throws IOException {
            for (Update update : updates) {
                outputStream.write(PasswordFilter.filterPasswordsInContents(update.toString()).getBytes());
            }
        }
    }

    public static class StreamingResponse implements StreamingOutput {
        final WhoisResources whoisResources;
        final HttpServletRequest request;

        public StreamingResponse(final HttpServletRequest request, final WhoisResources whoisResources) {
            this.request = request;
            this.whoisResources = whoisResources;
        }

        @Override
        public void write(OutputStream output) throws IOException, WebApplicationException {
            StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
        }

        public WhoisResources getWhoisResources() {
            return whoisResources;
        }
    }


}
