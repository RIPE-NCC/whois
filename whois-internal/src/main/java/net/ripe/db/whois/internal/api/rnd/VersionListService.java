package net.ripe.db.whois.internal.api.rnd;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.ApiResponseHandler;
import net.ripe.db.whois.api.rest.WhoisRestService;
import net.ripe.db.whois.api.rest.WhoisService;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Component
@Path("/rnd")
public class VersionListService {

    private final WhoisService whoisService;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectServerMapper;

    @Autowired
    public VersionListService(final WhoisService whoisService, final QueryHandler queryHandler, final WhoisObjectServerMapper whoisObjectServerMapper) {
        this.whoisService = whoisService;
        this.queryHandler = queryHandler;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response versions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {


        validSource(source);

        WhoisRestService.QueryBuilder queryBuilder = new WhoisRestService.QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addFlag(QueryFlag.LIST_VERSIONS);

        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.REST, whoisService.isTrusted(request));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final List<VersionResponseObject> versions = versionsResponseHandler.getVersionObjects();

        if (versions.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(whoisService.createErrorEntity(request, versionsResponseHandler.getErrors())).build());
        }

        final WhoisVersions whoisVersions = new WhoisVersions("TYPE", key, whoisObjectServerMapper.mapVersionsIncludingDeleted(versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    private void validSource(final String source) {
        //TODO sourcecontext won't work here, need to validate source in other ways.
    }

    private class VersionsResponseHandler extends ApiResponseHandler {
        private List<Message> errors;

        @Override
        public void handle(ResponseObject responseObject) {

        }

        public List<VersionResponseObject> getVersionObjects() {
            return Collections.EMPTY_LIST; //all (incl deleted)
        }

        public List<Message> getErrors() {
            return errors;
        }
    }

}
