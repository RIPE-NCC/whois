package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.update.domain.ClientCertificateCredential;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.log.LoggerContext;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Component
@Path("/domain-objects")
public class DomainObjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainObjectService.class);

    private final InternalUpdatePerformer updatePerformer;
    private final WhoisObjectMapper whoisObjectMapper;
    private final LoggerContext loggerContext;


    @Autowired
    public DomainObjectService(
            final WhoisObjectMapper whoisObjectMapper,
            final InternalUpdatePerformer updatePerformer,
            final LoggerContext loggerContext) {
        this.whoisObjectMapper = whoisObjectMapper;
        this.updatePerformer = updatePerformer;
        this.loggerContext = loggerContext;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}")
    public Response create(
            final WhoisResources resources,
            @Context final HttpServletRequest request,
            @PathParam("source") final String sourceParam,
            @QueryParam("password") final List<String> passwords,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {

        if (resources == null || resources.getWhoisObjects().size() == 0) {
            return Response.status(BAD_REQUEST).entity("WhoisResources is mandatory").build();
        }

        try {
            final Origin origin = updatePerformer.createOrigin(request);

            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey, request);
            updateContext.setBatchUpdate();

            auditlogRequest(request);

            final Credentials credentials = createCredentials(updateContext, passwords);

            final List<Update> updates = extractUpdates(resources, credentials);
            
            final WhoisResources updatedResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NEW, request);

            validateUpdates(updateContext, updates, updatedResources);

            return createResponse(OK, updatedResources);

        } catch (WebApplicationException e) {
            throw specializeWebApplicationException(e, resources); // note: response is attached to exception

        } catch (UpdateFailedException e) {
            return createResponse(e.status, e.whoisResources);

        } catch (Exception e) {
            updatePerformer.logError(e);
            LOGGER.error("Unexpected", e);
            return createResponse(INTERNAL_SERVER_ERROR, e.getMessage());

        } finally {
            updatePerformer.closeContext();
        }
    }

    private WebApplicationException specializeWebApplicationException(
            final WebApplicationException e,
            final WhoisResources resources) {

        final Response response = e.getResponse();

        switch (response.getStatus()) {
            case HttpStatus.UNAUTHORIZED_401:
                return new NotAuthorizedException(createResponse(UNAUTHORIZED, resources));

            case HttpStatus.INTERNAL_SERVER_ERROR_500:
                return new InternalServerErrorException(createResponse(INTERNAL_SERVER_ERROR, resources));

            default:
                return new BadRequestException(createResponse(BAD_REQUEST, resources));
        }
    }

    private void validateUpdates(UpdateContext updateContext, List<Update> updates, WhoisResources resources) {

        for (Update update : updates) {
            final UpdateStatus status = updateContext.getStatus(update);

            switch (status) {
                case SUCCESS: // nothing to do
                    break;

                case FAILED_AUTHENTICATION:
                    throw new UpdateFailedException(UNAUTHORIZED, resources);

                case EXCEPTION:
                    throw new UpdateFailedException(INTERNAL_SERVER_ERROR, resources);

                default:
                    if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                        throw new UpdateFailedException(CONFLICT, resources);
                    } else {
                        throw new UpdateFailedException(BAD_REQUEST, resources);
                    }
            }
        }
    }

    private List<Update> extractUpdates(final WhoisResources whoisResources, final Credentials credentials) {

        List<Update> result = Lists.newArrayList();

        for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            // TODO [TK]: map whoisObject.getAction() to Operation.DELETE, Operation.CREATE (not there yet), etc.

            if (ObjectType.getByName(whoisObject.getType()) != ObjectType.DOMAIN) {
                throw new IllegalArgumentException("supports 'domain' objects only");
            }
            final RpslObject rpslObject = whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            final Paragraph paragraph = new Paragraph(rpslObject.toString(), credentials);
            final Update update = new Update(paragraph, Operation.UNSPECIFIED, null, rpslObject);

            result.add(update);
        }
        return result;
    }

    private Credentials createCredentials(final UpdateContext updateContext, final List<String> passwords) {

        final Set<Credential> credentials = Sets.newHashSet();

        for (String password : passwords) {
            credentials.add(new PasswordCredential(password));
        }

        if (updateContext.getUserSession() != null) {
            credentials.add(SsoCredential.createOfferedCredential(updateContext.getUserSession()));
        }

        updateContext.getClientCertificate().ifPresent(x509 -> credentials.add(ClientCertificateCredential.createOfferedCredential(x509)));

        return new Credentials(credentials);
    }

    private Response createResponse(Response.Status status, WhoisResources updatedResources) {
        return Response.status(status).entity(updatedResources).build();
    }

    private Response createResponse(Response.Status status, String message) {
        return Response.status(status).entity(message).build();
    }

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
    }

    private class UpdateFailedException extends RuntimeException {
        private final WhoisResources whoisResources;
        private final Response.Status status;

        public UpdateFailedException(Response.Status status, WhoisResources whoisResources) {
            this.status = status;
            this.whoisResources = whoisResources;
        }
    }
}
