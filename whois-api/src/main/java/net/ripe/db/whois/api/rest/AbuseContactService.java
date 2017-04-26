package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbusePKey;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.AbuseContactMapper;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.AbuseCFinder;
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
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component
@Path("/abuse-contact")
public class AbuseContactService {

    private final QueryHandler queryHandler;
    private final AccessControlListManager accessControlListManager;
    private final AbuseCFinder abuseCFinder;

    @Autowired
    public AbuseContactService(final QueryHandler queryHandler, final AccessControlListManager accessControlListManager, final AbuseCFinder abuseCFinder) {
        this.queryHandler = queryHandler;
        this.accessControlListManager = accessControlListManager;
        this.abuseCFinder = abuseCFinder;
    }

    //TODO [TP]: in case abuse contact is empty we should return 404 instead of 200 + empty string!
    @GET
    @Path("/{key:.*}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("key") final String key) {

        final QueryBuilder queryBuilder = new QueryBuilder()
                .addFlag(QueryFlag.NO_GROUPING)
                .addFlag(QueryFlag.NO_REFERENCED);

        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.REST, isTrusted(request));

        final List<AbuseResources> abuseResources = Lists.newArrayList();

        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, new ApiResponseHandler() {

            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof RpslObject) {
                    final RpslObject rpslObject = (RpslObject)responseObject;

                    final String abuseContact = abuseCFinder.getAbuseContact(rpslObject);
                    final RpslObject abuseRole = abuseCFinder.getAbuseContactRole(rpslObject);

                    abuseResources.add(
                        new AbuseResources(
                            "abuse-contact",
                            Link.create(String.format("http://rest.db.ripe.net/abuse-contact/%s", key)),
                            new Parameters(null, null, null, null, null, new AbusePKey(rpslObject.getKey().toString())),
                            new AbuseContact(abuseRole != null ? abuseRole.getKey().toString() : "", abuseContact != null ? abuseContact : ""),
                            Link.create(WhoisResources.TERMS_AND_CONDITIONS)));
                }
            }
        });

        if (abuseResources.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(AbuseContactMapper.mapAbuseContactError("No abuse contact found for " + key))
                    .build());
        }

        final AbuseResources result = abuseResources.get(0);

        final String parametersKey = result.getParameters().getPrimaryKey().getValue();
        if (parametersKey.equals("::/0") || parametersKey.equals("0.0.0.0 - 255.255.255.255")) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(AbuseContactMapper.mapAbuseContactError("No abuse contact found for " + key))
                    .build());
        }

        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(result);
            }
        }).build();
    }

    private boolean isTrusted(final HttpServletRequest request) {
        return accessControlListManager.isTrusted(InetAddresses.forString(request.getRemoteAddr()));
    }
}
