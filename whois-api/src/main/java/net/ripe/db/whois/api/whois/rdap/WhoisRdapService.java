package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.WhoisService;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRdapService extends WhoisService {

    @Autowired
    public WhoisRdapService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        super(dateTimeProvider, updateRequestHandler, loggerContext, rpslObjectDao, sourceContext, queryHandler);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{objectType}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request, @PathParam("objectType") final String objectType, @PathParam("key") final String key) {
        /* RDAP object types do not map directly to whois object
         * types, so translate accordingly here for the remaining
         * object types as they are implemented. */

        String whoisObjectType = objectType;
        String whoisKey = key;

        switch (objectType) {
            case "autnum":
                whoisObjectType = (RdapUtilities.fetchObject(queryHandler, "aut-num", "AS" + key, source()) != null) ? "aut-num" : "as-block";
                whoisKey = "AS" + key;
                break;

            case "entity":
                whoisObjectType = "person,role,organisation,irt";
                break;

            case "domain":
                whoisObjectType = "domain";
                break;

            case "ip":
                if (key.contains(":")) {
                    whoisObjectType = "inet6num";
                } else {
                    whoisObjectType = "inetnum";
                }
                break;

            default:
                return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return lookupObject(request, source(), whoisObjectType, whoisKey, false);
    }

    protected Response handleQueryAndStreamResponse(final Query query, final HttpServletRequest request, final InetAddress remoteAddress, final int contextId, @Nullable final Parameters parameters) {
        final StreamingMarshal streamingMarshal = new RdapStreamingMarshalJson();

        String queryString = request.getQueryString();
        String requestUrl = request.getRequestURL().toString() + ((queryString != null) ? "?" + queryString : "");

        // TODO: A bit awkward; there should be a better way to determine this.
        String baseUrl = requestUrl;
        int pathIndex = 0;
        int count = 3;
        while ((count--) != 0) {
            pathIndex = baseUrl.indexOf('/', pathIndex + 1);
        }

        baseUrl = baseUrl.substring(0, pathIndex) + request.getContextPath();

        RdapStreamingOutput rso = new RdapStreamingOutput(streamingMarshal, queryHandler, parameters, query, remoteAddress, contextId, sourceContext, baseUrl, requestUrl);

        return Response.ok(rso).build();
    }

    private String source() {
        return this.sourceContext.getWhoisSlaveSource().getName().toString();
    }

    // TODO: [AH] hierarchical lookups return the encompassing range if no direct hit
    protected Response lookupObject(final HttpServletRequest request, final String source, final String objectTypeString, final String key, final boolean isGrs) {
        final Query query = Query.parse(
                String.format("%s %s %s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.SOURCES.getLongFlag(), source,
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypeString,
                        QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));

        checkForInvalidSource(source);

        return handleQuery(query, source, key, request, null);
    }
}
