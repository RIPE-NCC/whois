package net.ripe.db.whois.internal.api.rnd.rest;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.api.rest.WhoisService;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.internal.api.rnd.VersionsService;
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

/**
 * Specially tailored class for RND (internal use). RND is only interested in JSON.
 */
@Component
@Path("/rnd")
public class VersionsRestService {
    private final WhoisService whoisService;
    private final BasicSourceContext sourceContext;
    private final IpRanges ipRanges;
    private final VersionsService versionsService;

    @Autowired
    public VersionsRestService(final WhoisService whoisService, final BasicSourceContext basicSourceContext, final IpRanges ipRanges, final VersionsService versionsService) {
        this.whoisService = whoisService;
        this.sourceContext = basicSourceContext;
        this.ipRanges = ipRanges;
        this.versionsService = versionsService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response versions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        validate(request, source);

        return Response.ok(versionsService.streamVersions(key, ObjectType.getByName(objectType), source, request)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions/{revision:.*}")
    public Response version(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("revision") final Integer revision) {


        validate(request, source);

        return Response.ok(versionsService.streamVersion(ObjectType.getByName(objectType), key, source, revision, request)).build();
    }

    private void validate(final HttpServletRequest request, final String source) {
        if (!sourceContext.getAllSourceNames().contains(CIString.ciString(source))) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }

        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        if (!ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress))) {
            throw new WebApplicationException(Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidRequestIp()))
                    .build());
        }
    }
}
