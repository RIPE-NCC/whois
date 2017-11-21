package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;

@Component
@Path("/batch")
public class BatchUpdatesService {

    private final static String DELETE_REASON = "Resource transfer";

    private final LoggerContext loggerContext;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    final WhoisObjectMapper whoisObjectMapper;

    @Autowired
    public BatchUpdatesService(final InternalUpdatePerformer updatePerformer,
                               final SsoTranslator ssoTranslator,
                               final LoggerContext loggerContext,
                               final WhoisObjectMapper whoisObjectMapper) {
        this.loggerContext = loggerContext;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
        this.whoisObjectMapper = whoisObjectMapper;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}")
    public Response update(final WhoisResources whoisResources,
                       @Context final HttpServletRequest request,
                       @PathParam("source") final String sourceParam,
                       @CookieParam("crowd.token_key") final String crowdTokenKey) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);
            updateContext.setBatchUpdate();
            auditlogRequest(request);

            final List<ActionRequest> actionRequests = createActionRequests(whoisResources);

            final List<Update> updates = Lists.newArrayList();
            for (final ActionRequest actionRequest : actionRequests) {
                final String deleteReason = Action.DELETE.equals(actionRequest.getAction()) ? DELETE_REASON : null;

                ssoTranslator.populateCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                final RpslObject rpslObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                // TODO should probably use override user?
                updates.add(updatePerformer.createUpdate(updateContext, rpslObject, EMPTY_LIST /* passwords */, deleteReason, "personadmin,secret" /* override */));
            }

            final WhoisResources updatedWhoisResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NONE, request);
            return createCombinedResponse(updates, updateContext, updatedWhoisResources, request);
        } finally {
            updatePerformer.closeContext();
        }
    }

    private Response createCombinedResponse(final List<Update> updates,
                                            final UpdateContext updateContext,
                                            final WhoisResources updatedWhoisResources,
                                            final HttpServletRequest request) {
        Response.StatusType responseStatus = null;
        for (final Update update : updates) {
            Response response = updatePerformer.createResponse(updateContext, updatedWhoisResources, update, request);
            if (responseStatus == null || response.getStatusInfo() != Response.Status.OK) {
                responseStatus = response.getStatusInfo();
            }
        }

        return Response.status(responseStatus).
            entity((StreamingOutput) outputStream -> StreamingHelper.getStreamingMarshal(request, outputStream).singleton(updatedWhoisResources)).
            build();
    }

    private List<ActionRequest> createActionRequests(WhoisResources whoisResources) {
        List<ActionRequest> actionRequests = Lists.newArrayList();
        whoisResources.getWhoisObjects().forEach((whoisObject) -> actionRequests.add(new ActionRequest(whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class), whoisObject.getAction())));
        return actionRequests;
    }

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
    }

}
