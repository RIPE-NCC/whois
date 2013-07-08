package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static net.ripe.db.whois.common.rpsl.ObjectType.*;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRdapService {

    protected final DateTimeProvider dateTimeProvider;
    protected final UpdateRequestHandler updateRequestHandler;
    protected final LoggerContext loggerContext;
    protected final RpslObjectDao rpslObjectDao;
    protected final SourceContext sourceContext;
    protected final QueryHandler queryHandler;

    @Autowired
    public WhoisRdapService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.loggerContext = loggerContext;
        this.rpslObjectDao = rpslObjectDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
    }

    // TODO: [ES] drop streaming support - only one object returned. but, must implement logging, blocking etc.

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{objectType}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") final String objectType,
                           @PathParam("key") final String key) {

        final Set<ObjectType> whoisObjectTypes = Sets.newHashSet();

        switch (objectType) {
            case "autnum":
                whoisObjectTypes.add(AUT_NUM);
                break;

            case "domain":
                whoisObjectTypes.add(DOMAIN);
                break;

            case "ip":
                whoisObjectTypes.add(key.contains(":") ? INET6NUM : INETNUM);
                break;

            case "entity":
                whoisObjectTypes.add(PERSON);
                whoisObjectTypes.add(ROLE);
                whoisObjectTypes.add(ORGANISATION);
                whoisObjectTypes.add(IRT);
                break;

            default:
                return Response.status(BAD_REQUEST).build();
        }

        return lookupObject(request, whoisObjectTypes, getKey(whoisObjectTypes, key));
    }

    private String getKey(final Set<ObjectType> objectTypes, final String key) {
        if (objectTypes.contains(AUT_NUM)) {
            return String.format("AS%s", key);
        }
        return key;
    }

    // TODO: [AH] hierarchical lookups return the encompassing range if no direct hit
    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        final String source = sourceContext.getWhoisSlaveSource().getName().toString();
        final String objectTypesString = Joiner.on(",").join(Iterables.transform(objectTypes, new Function<ObjectType, String>() {
            @Override
            public String apply(final ObjectType input) {
                return input.getName();
            }
        }));

        final Query query = Query.parse(
                String.format("%s %s %s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.SOURCES.getLongFlag(), source,
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesString,
                        QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));

        return handleQueryAndStreamResponse(query, request);
    }

    protected Response handleQueryAndStreamResponse(final Query query, final HttpServletRequest request) {
        final StreamingMarshal streamingMarshal = new RdapStreamingMarshalJson();

        final String queryString = request.getQueryString();
        final String requestUrl = request.getRequestURL().toString() + ((queryString != null) ? "?" + queryString : "");
        final int contextId = System.identityHashCode(Thread.currentThread());
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());

        // TODO: A bit awkward; there should be a better way to determine this. Also, baseUrl will have to be a configuration option anyway, because the internal and external URL schemes may differ.
        String baseUrl = requestUrl;
        int pathIndex = 0;
        int count = 3;
        while ((count--) != 0) {
            pathIndex = baseUrl.indexOf('/', pathIndex + 1);
        }

        baseUrl = baseUrl.substring(0, pathIndex) + request.getServletPath() + request.getContextPath();

        RdapStreamingOutput rso = new RdapStreamingOutput(streamingMarshal, queryHandler, null, query, remoteAddress, contextId, sourceContext, baseUrl, requestUrl);

        return Response.ok(rso).build();
    }
}
