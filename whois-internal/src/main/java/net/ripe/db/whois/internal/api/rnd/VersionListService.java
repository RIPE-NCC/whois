package net.ripe.db.whois.internal.api.rnd;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.api.rest.WhoisRestService;
import net.ripe.db.whois.api.rest.WhoisService;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionsInternal;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.ResponseHandler;
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
import java.net.InetAddress;
import java.util.List;

@Component
@Path("/rnd")
public class VersionListService {

    private final WhoisService whoisService;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final BasicSourceContext sourceContext;
    private final IpRanges ipRanges;

    @Autowired
    public VersionListService(final WhoisService whoisService, final QueryHandler queryHandler, final WhoisObjectServerMapper whoisObjectServerMapper, final BasicSourceContext basicSourceContext, final IpRanges ipRanges) {
        this.whoisService = whoisService;
        this.queryHandler = queryHandler;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.sourceContext = basicSourceContext;
        this.ipRanges = ipRanges;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response versions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {


        validSource(request, source);

        final WhoisRestService.QueryBuilder queryBuilder = new WhoisRestService
                .QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addFlag(QueryFlag.LIST_VERSIONS);

        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.INTERNAL, ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress)));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());

        // TODO [FRV] Is the public queryhandler handling everything correctly? Seems so but it looks like overkill
        queryHandler.streamResults(query, remoteAddress, contextId, versionsResponseHandler);
        final List<VersionResponseObject> versions = versionsResponseHandler.getVersions();

        if (versions.isEmpty()) {
            versionsResponseHandler.errors.add(new MessageObject(QueryMessages.noResults(source)).getMessage());
            throw new WebApplicationException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, versionsResponseHandler.getErrors()))
                    .build());
        }

        final WhoisVersionsInternal whoisVersions = new WhoisVersionsInternal(objectType, key, whoisObjectServerMapper.mapVersionsInternal(versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    private void validSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getAllSourceNames().contains(CIString.ciString(source))) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }
    }

    private class VersionsResponseHandler implements ResponseHandler {
        private List<Message> errors = Lists.newArrayList();
        private List<VersionResponseObject> versions = Lists.newArrayList();

        @Override
        public String getApi() {
            return "INTERNAL_API";
        }

        @Override
        public void handle(final ResponseObject responseObject) {
            if (responseObject instanceof VersionResponseObject) {
                versions.add((VersionResponseObject) responseObject);
            } else if (responseObject instanceof MessageObject) {
                final Message message = ((MessageObject) responseObject).getMessage();
                if (message != null && Messages.Type.INFO != message.getType()) {
                    errors.add(message);
                }
            }
        }

        public List<VersionResponseObject> getVersions() {
            return versions;
        }

        public List<Message> getErrors() {
            return errors;
        }
    }
}
