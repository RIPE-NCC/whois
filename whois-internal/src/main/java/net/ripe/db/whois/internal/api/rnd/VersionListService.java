package net.ripe.db.whois.internal.api.rnd;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.api.rest.WhoisService;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionsInternal;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import java.util.Collections;
import java.util.List;

@Component
@Path("/rnd")
public class VersionListService {
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

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

        final QueryBuilder queryBuilder = new QueryBuilder()
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
            throw new WebApplicationException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, Collections.singletonList(QueryMessages.noResults(source))))
                    .build());
        }

        final WhoisVersionsInternal whoisVersions = new WhoisVersionsInternal(objectType,
                key,
                whoisObjectServerMapper.mapVersionsInternal(versions, source, objectType, key));


        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions/{datetime}")
    public Response version(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("datetime") final String datetime) {


        validSource(request, source);
        long timestamp = validateDateTimeAndConvertToTimestamp(request, datetime);

        final QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addCommaList(QueryFlag.SHOW_VERSION, String.valueOf(timestamp));

        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.INTERNAL, ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress)));

        final SingleVersionResponseHandler singleVersionResponseHandler = new SingleVersionResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());

        queryHandler.streamResults(query, remoteAddress, contextId, singleVersionResponseHandler);

        final RpslObject rpslObject = singleVersionResponseHandler.getRpslObject();
        if (rpslObject == null) {
            throw new WebApplicationException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, Collections.singletonList(QueryMessages.noResults(source))))
                    .build());
        }

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObjectServerMapper.map(rpslObject, null, FormattedServerAttributeMapper.class)));
        whoisResources.setErrorMessages(whoisService.createErrorMessages(singleVersionResponseHandler.getErrors()));
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

    private long validateDateTimeAndConvertToTimestamp(final HttpServletRequest request, final String timestamp) {
        try {
            return DEFAULT_DATE_TIME_FORMATTER.parseDateTime(timestamp).getMillis();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidTimestampFormat(timestamp)))
                    .build());
        }
    }
}
