package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
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

import java.util.List;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.CONFLICT;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

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

            final List<Update> updates = extractUpdates(resources, passwords, updateContext);

            final WhoisResources updatedResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NEW, request);

            validateUpdates(updateContext, updates, updatedResources);

            return createResponse(OK, updatedResources);

        } catch (WebApplicationException e) {
            throw specializeWebApplicationException(e, resources); // note: response is attached to exception

        } catch (UpdateFailedException e) {
            return createResponse(e.status, e.whoisResources);

        } catch (IllegalArgumentException e) {
            return createResponse(BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            updatePerformer.logError(e);
            LOGGER.error("Unexpected", e);
            return createResponse(INTERNAL_SERVER_ERROR, e.getMessage());

        } finally {
            updatePerformer.closeContext();
        }
    }

    private List<Update> extractUpdates(final WhoisResources resources, final List<String> passwords, final UpdateContext updateContext) {
        final List<Update> updates = Lists.newArrayList();
        resources.getWhoisObjects().forEach( whoisObject -> {
            if (ObjectType.getByName(whoisObject.getType()) != ObjectType.DOMAIN) {
                throw new IllegalArgumentException("supports 'domain' objects only");
            }
            final RpslObject rpslObject = whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            updates.add(updatePerformer.createUpdate(updateContext, rpslObject, passwords, null, null));
          }
        );
        return updates;
    }

    private WebApplicationException specializeWebApplicationException(
            final WebApplicationException e,
            final WhoisResources resources) {

        final Response response = e.getResponse();

        return switch (response.getStatus()) {
            case HttpStatus.UNAUTHORIZED_401 -> new NotAuthorizedException(createResponse(UNAUTHORIZED, resources));
            case HttpStatus.INTERNAL_SERVER_ERROR_500 -> new InternalServerErrorException(createResponse(INTERNAL_SERVER_ERROR, resources));
            case HttpStatus.UPGRADE_REQUIRED_426 -> e;
            default -> new BadRequestException(createResponse(BAD_REQUEST, resources));
        };
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
