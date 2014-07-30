package net.ripe.db.whois.internal.api.rnd;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.api.rest.WhoisService;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisObjects;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.BasicSourceContext;
import net.ripe.db.whois.internal.api.rnd.dao.ObjectReferenceDao;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectReference;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

@Component
@Path("/rnd")
public class VersionsRestService {
    private final WhoisService whoisService;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final VersionObjectMapper versionObjectMapper;
    private final BasicSourceContext sourceContext;
    private final IpRanges ipRanges;
    private final ObjectReferenceDao referenceDao;

    @Autowired
    public VersionsRestService(final WhoisService whoisService, final QueryHandler queryHandler, final WhoisObjectServerMapper whoisObjectServerMapper, final VersionObjectMapper versionObjectMapper, final BasicSourceContext basicSourceContext, final IpRanges ipRanges, final ObjectReferenceDao referenceDao) {
        this.whoisService = whoisService;
        this.queryHandler = queryHandler;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.versionObjectMapper = versionObjectMapper;
        this.sourceContext = basicSourceContext;
        this.ipRanges = ipRanges;
        this.referenceDao = referenceDao;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response versions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        validate(request, source);

        final List<ObjectVersion> versions = referenceDao.getVersions(key, ObjectType.getByName(objectType));

        if (versions.isEmpty()) {
            throw new WebApplicationException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, Collections.singletonList(InternalMessages.noVersions(key))))
                    .build());
        }

        return Response.ok(versionObjectMapper.mapVersions(objectType, key, source, versions)).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions/{revision:.*}")
    public Response version(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("revision") final Integer revision) {


        validate(request, source);

        final QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addCommaList(QueryFlag.SHOW_INTERNAL_VERSION, String.valueOf(revision));

        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.INTERNAL, ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress)));

        final VersionDateTimeResponseHandler versionDateTimeResponseHandler = new VersionDateTimeResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());

        queryHandler.streamResults(query, remoteAddress, contextId, versionDateTimeResponseHandler);

        final RpslObjectWithTimestamp rpslObjectWithTimestamp = versionDateTimeResponseHandler.getRpslObjectWithTimestamp();
        if (rpslObjectWithTimestamp == null) {
            throw new WebApplicationException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, Collections.singletonList(InternalMessages.noVersion(key))))
                    .build());
        }

        final WhoisResources whoisResources = new WhoisResources();

        final WhoisObject whoisObject = whoisObjectServerMapper.map(rpslObjectWithTimestamp.getRpslObject(), null, FormattedClientAttributeMapper.class);
        if (rpslObjectWithTimestamp.getVersionDateTime() != null) {
            whoisObject.setVersionDateTime(rpslObjectWithTimestamp.getVersionDateTime().toString());
        }
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.setOutgoing(mapReferences(rpslObjectWithTimestamp.getOutgoing()));
        whoisResources.setIncoming(mapReferences(rpslObjectWithTimestamp.getIncoming()));
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionDateTimeResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
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

    private WhoisObjects mapReferences(final List<ObjectReference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return null;
        }

        return new WhoisObjects(
                Lists.newArrayList(Iterables.transform(references, new Function<ObjectReference, WhoisObject>() {
                    @Override
                    public WhoisObject apply(final ObjectReference input) {
                        final WhoisObject whoisObject = new WhoisObject();
// TODO: [ES] fix
//                        whoisObject.setPrimaryKey(Lists.newArrayList(new Attribute(input.getRefObjectType().getName(), input.getRefPkey().toString())));
//                        whoisObject.setType(input.getRefObjectType().getName());
                        return whoisObject;
                    }
                })));
    }
}
