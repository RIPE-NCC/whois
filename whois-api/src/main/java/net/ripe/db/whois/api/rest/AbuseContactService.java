package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.mapper.AbuseContactMapper;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.RpslAttributes;
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

import static net.ripe.db.whois.api.rest.WhoisRestService.QueryBuilder;

@Component
@Path("/abuse-contact")
public class AbuseContactService {

    private final QueryHandler queryHandler;

    @Autowired
    public AbuseContactService(final QueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    @GET
    @Path("/{key:.*}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("key") final String key) {

        QueryBuilder queryBuilder = new QueryBuilder()
                .addFlag(QueryFlag.ABUSE_CONTACT);

        final Query query = Query.parse(queryBuilder.build(key));

        final List<AbuseResources> abuseResources = Lists.newArrayList();

        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, new ApiResponseHandler() {

            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof RpslAttributes) {
                    final RpslAttributes responseAttributes = (RpslAttributes)responseObject;
                    abuseResources.add(AbuseContactMapper.mapAbuseContact(key, responseAttributes.getAttributes()));
                }
            }
        });

        if (abuseResources.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No abuse contact found for " + key).build());
        }

        final AbuseResources result = abuseResources.get(0);

        final String parametersKey = result.getParameters().getPrimaryKey().getValue();
        if (parametersKey.equals("::/0") || parametersKey.equals("0.0.0.0 - 255.255.255.255")) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("No abuse contact found for " + key).build());
        }

        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                WhoisRestService.getStreamingMarshal(request, output).singleton(result);
            }
        }).build();
    }
}
