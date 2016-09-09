package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.update.domain.Credential;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.SsoCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
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
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Path("/domainobject")
public class DomainObjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainObjectService.class);

    private final InternalUpdatePerformer updatePerformer;
    private final WhoisObjectMapper whoisObjectMapper;

    @Autowired
    public DomainObjectService(
            final WhoisObjectMapper whoisObjectMapper,
            final InternalUpdatePerformer updatePerformer) {

        this.whoisObjectMapper = whoisObjectMapper;
        this.updatePerformer = updatePerformer;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(
            final WhoisResources resources,
            @Context final HttpServletRequest request,
            @QueryParam("password") final List<String> passwords,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {

        if (resources == null) {
            return badRequest("WhoisResources is mandatory");
        }
        try {
            final Origin origin = updatePerformer.createOrigin(request);

            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            final Credentials credentials = createCredentials(updateContext.getUserSession(), passwords, /* override= */null);

            final List<Update> updates = extractUpdates(resources, credentials);

            final WhoisResources updatedResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NONE, request);

            validateUpdates(updateContext, updates, resources);

            return createResponse(request, updatedResources, Response.Status.OK);

        } catch (WebApplicationException e) {
            throw specializeWebApplicationException(e, request, resources); // note: response is attached to exception

        } catch (UpdateFailedException e) {
            return createResponse(request, e.whoisResources, e.status);

        } catch (Exception e) {
            updatePerformer.logError(e);
            LOGGER.error("Unexpected", e);
            return createResponse(request, resources, Response.Status.INTERNAL_SERVER_ERROR);

        } finally {
            updatePerformer.closeContext();
        }
    }

    private WebApplicationException specializeWebApplicationException(
            final WebApplicationException e,
            final HttpServletRequest request,
            final WhoisResources resources) {

        final Response response = e.getResponse();

        switch (response.getStatus()) {
            case HttpStatus.UNAUTHORIZED_401:
                return new NotAuthorizedException(createResponse(request, resources, Response.Status.UNAUTHORIZED));

            case HttpStatus.INTERNAL_SERVER_ERROR_500:
                return new InternalServerErrorException(createResponse(request, resources, Response.Status.INTERNAL_SERVER_ERROR));

            default:
                return new BadRequestException(createResponse(request, resources, BAD_REQUEST));
        }
    }

    private void validateUpdates(UpdateContext updateContext, List<Update> updates, WhoisResources resources) {

        for (Update update : updates) {
            final UpdateStatus status = updateContext.getStatus(update);

            switch (status) {
                case SUCCESS: // nothing to do
                    break;

                case FAILED_AUTHENTICATION:
                    throw new UpdateFailedException(Response.Status.UNAUTHORIZED, resources);

                case EXCEPTION:
                    throw new UpdateFailedException(Response.Status.INTERNAL_SERVER_ERROR, resources);

                default:
                    if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                        throw new UpdateFailedException(Response.Status.CONFLICT, resources);
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

    private Credentials createCredentials(final UserSession userSession, final List<String> passwords, final String override) {

        final Set<Credential> credentials = Sets.newHashSet();

        for (String password : passwords) {
            credentials.add(new PasswordCredential(password));
        }

        if (override != null) {
            credentials.add(OverrideCredential.parse(override));
        }

        if (userSession != null) {
            credentials.add(SsoCredential.createOfferedCredential(userSession));
        }
        return new Credentials(credentials);
    }

    private Response createResponse(final HttpServletRequest request, final WhoisResources whoisResources, final Response.Status status) {
        final Response.ResponseBuilder responseBuilder = Response.status(status);
        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }

    private Response badRequest(final String message) {
        return Response.status(BAD_REQUEST).entity(message).build();
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
