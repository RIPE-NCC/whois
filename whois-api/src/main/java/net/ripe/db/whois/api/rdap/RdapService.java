package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Component
@Path("/")
public class RdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapService.class);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private final int maxResultSize;
    private final int maxEntityResultSize;
    private final RdapQueryHandler rdapQueryHandler;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;
    private final RdapFullTextSearch rdapFullTextSearch;
    private final Source source;
    private final String baseUrl;
    private final RdapRequestValidator rdapRequestValidator;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final SourceContext sourceContext;
    private final RdapRelationService rdapRelationService;

    /**
     *
     * @param rdapQueryHandler
     * @param abuseCFinder
     * @param rdapObjectMapper
     * @param delegatedStatsService
     * @param rdapFullTextSearch
     * @param sourceContext
     * @param baseUrl
     * @param rdapRequestValidator
     * @param maxResultSize: If the response is bigger than maxResultSize, we truncate the response and we add a notification
     * @param maxEntityResultSize: used for networks maximum retrieved objects, if we retrieve more objects than
     *                           the maximum value we truncate the response and we add a notification in the response.
     */
    @Autowired
    public RdapService(final RdapQueryHandler rdapQueryHandler,
                       final AbuseCFinder abuseCFinder,
                       final RdapObjectMapper rdapObjectMapper,
                       final DelegatedStatsService delegatedStatsService,
                       final RdapFullTextSearch rdapFullTextSearch,
                       final SourceContext sourceContext,
                       final RpslObjectUpdateDao rpslObjectUpdateDao,
                       @Value("${rdap.public.baseUrl:}") final String baseUrl,
                       final RdapRequestValidator rdapRequestValidator,
                       @Value("${rdap.search.max.results:100}") final int maxResultSize,
                       @Value("${rdap.entity.max.results:100}") final int maxEntityResultSize,
                       final RdapRelationService rdapRelationService) {
        this.sourceContext = sourceContext;
        this.rdapQueryHandler = rdapQueryHandler;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = rdapObjectMapper;
        this.delegatedStatsService = delegatedStatsService;
        this.rdapFullTextSearch = rdapFullTextSearch;
        this.source = sourceContext.getCurrentSource();
        this.baseUrl = baseUrl;
        this.rdapRequestValidator = rdapRequestValidator;
        this.maxResultSize = maxResultSize;
        this.maxEntityResultSize = maxEntityResultSize;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.rdapRelationService = rdapRelationService;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") RdapRequestType requestType,
                           @PathParam("key") final String key) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));
        if (requestType == null) {
            throw new RdapException("400 Bad Request", "requestType parameter is required", HttpStatus.BAD_REQUEST_400);
        }

        final Set<ObjectType> whoisObjectTypes = requestType.getWhoisObjectTypes(key);  // null

        switch (requestType) {
            case AUTNUM -> {
                String autnumKey = String.format("AS%s", key);
                rdapRequestValidator.validateAutnum(autnumKey);
                return lookupForAutNum(request, autnumKey);
            }
            case DOMAIN -> {
                rdapRequestValidator.validateDomain(key);
                return lookupForDomain(request, key);
            }
            case IP -> {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                return lookupWithRedirectUrl(request, whoisObjectTypes, key);
            }
            case ENTITY -> {
                rdapRequestValidator.validateEntity(key);
                return key.toUpperCase().startsWith("ORG-") ? lookupForOrganisation(request, key) :
                        lookupObject(request, whoisObjectTypes, key);
            }
            case NAMESERVER -> {
                throw new RdapException("501 Not Implemented", "Nameserver not supported", HttpStatus.NOT_IMPLEMENTED_501);
            }
            default -> {
                throw new RdapException("400 Bad Request", "unknown type" + requestType, HttpStatus.BAD_REQUEST_400);
            }
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/entities")
    public Response searchEntities(
            @Context final HttpServletRequest request,
            @QueryParam("fn") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        if (name != null && handle == null) {
            return handleSearch(new String[]{"person", "role", "org-name"}, name, request);
        }

        if (name == null && handle != null) {
            return handleSearch(new String[]{"organisation", "nic-hdl", "mntner"}, handle, request);
        }

        throw new RdapException("400 Bad Request", "Either fn or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/ips")
    public Response searchIps(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        if (name != null && handle == null || name == null && handle != null) {
            return handleSearch(new String[]{"netname"}, name != null ? name : handle, request);
        }

        throw new RdapException("400 Bad Request", "Either name or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/autnums")
    public Response searchAutnums(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        if (name != null && handle == null) {
            return handleSearch(new String[]{"as-name"}, name, request);
        }

        if (name == null && handle != null) {
            return handleSearch(new String[]{"aut-num"}, handle, request);
        }

        throw new RdapException("400 Bad Request", "Either name or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/nameservers")
    public Response searchNameservers(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {
        throw new RdapException("501 Not Implemented", "Nameserver not supported", HttpStatus.NOT_IMPLEMENTED_501);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/domains")
    public Response searchDomains(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        return handleSearch(new String[]{"domain"}, name, request);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/help")
    public Response help(@Context final HttpServletRequest request) {
        return Response.ok(rdapObjectMapper.mapHelp(getRequestUrl(request)))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType}/rirSearch1/{relation}/{key:.*}")
    public Response relationSearch(
            @Context final HttpServletRequest request,
            @PathParam("objectType") RdapRequestType requestType,
            @PathParam("relation") RelationType relationType,
            @PathParam("key") final String key,
            @QueryParam("status") String status) {

        //TODO: [MH] Status is being ignored until administrative resources are included in RDAP. If status is not
        // given or status is inactive...include administrative resources in the output. However, if status is active
        // return just non administrative resources, as we are doing now.
        if (!StringUtil.isNullOrEmpty(status) && (relationType.equals(RelationType.DOWN) || relationType.equals(RelationType.BOTTOM))){
            throw new RdapException("501 Not Implemented", "Status is not implement in down and bottom relation", HttpStatus.NOT_IMPLEMENTED_501);
        }

        final Set<ObjectType> objectTypes = requestType.getWhoisObjectTypes(key);
        if (isRedirect(Iterables.getOnlyElement(objectTypes), key)) {
            return redirect(getRequestPath(request), getQueryObject(objectTypes, key));
        }

        final List<RpslObject> rpslObjects = handleRelationQuery(request, requestType, relationType, key);

        return Response.ok(rdapObjectMapper.mapSearch(
                        getRequestUrl(request),
                        rpslObjects,
                        maxResultSize))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private Response lookupWithRedirectUrl(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        if (isRedirect(Iterables.getOnlyElement(objectTypes), key)) {
            return redirect(getRequestPath(request), getQueryObject(objectTypes, key));
        }
        return lookupObject(request, objectTypes, key);
    }

    private Response lookupForAutNum(final HttpServletRequest request, final String key) {
        try {
            if (isRedirect(AUT_NUM, key) && !rdapRequestValidator.isReservedAsNumber(key)) {
                return redirect(getRequestPath(request), AUT_NUM, key);
            }

            final Query query = getQueryObject(ImmutableSet.of(AUT_NUM), key);
            List<RpslObject> result = rdapQueryHandler.handleAutNumQuery(query, request);

            return getResponse(request, result);
        } catch (RdapException ex){
            throw new AutnumException(ex.getErrorTitle(), ex.getErrorDescription(), ex.getErrorCode());
        }
    }

    private Boolean isRedirect(ObjectType objectType, final String key) {
        return !delegatedStatsService.isMaintainedInRirSpace(source.getName(), objectType, CIString.ciString(key));
    }

    private Boolean isRedirectDomain(final Domain domain) {
        return isRedirect(getReverseObjectType(domain), domain.getReverseIp().toString());
    }

    private ObjectType getReverseObjectType(final Domain domain) {
        final IpInterval<?> reverseIp = domain.getReverseIp();
        if (reverseIp instanceof Ipv4Resource) {
            return INETNUM;
        } else {
            if (reverseIp instanceof Ipv6Resource) {
                return INET6NUM;
            } else {
                throw new IllegalStateException("Unexpected type " + reverseIp.getClass().getName());
            }
        }
    }

    protected Response lookupForDomain(final HttpServletRequest request, final String key) {
        final Domain domain = Domain.parse(key);

        if (isRedirectDomain(domain)) {
            return redirectDomain(getRequestPath(request), domain);
        }

        final Stream<RpslObject> domainResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), key), request);
        final Stream<RpslObject> inetnumResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), domain.getReverseIp().toString()), request);

        return Response.ok(
                getDomainEntity(request, domainResult, inetnumResult))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        final List<RpslObject> result = rdapQueryHandler.handleQueryStream(getQueryObject(objectTypes, key), request).toList();
        return getResponse(request, result);
    }

    protected Response lookupForOrganisation(final HttpServletRequest request, final String key) {
        final List<RpslObject> organisationResult = rdapQueryHandler.handleQueryStream(getQueryObject(Set.of(ORGANISATION), key), request).toList();

        final RpslObject organisation = switch (organisationResult.size()) {
            case 0 ->
                    throw new RdapException("404 Not Found", "Requested organisation not found: " + key, HttpStatus.NOT_FOUND_404);
            case 1 -> organisationResult.get(0);
            default ->
                    throw new RdapException("500 Internal Error", "Unexpected result size: " + organisationResult.size(), HttpStatus.INTERNAL_SERVER_ERROR_500);
        };

        final Set<RpslObjectInfo> references = getReferences(organisation);

        final List<RpslObjectInfo> autnumResult = references.stream()
            .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == AUT_NUM)
            .toList();

        final List<RpslObjectInfo> inetnumResult = references.stream()
            .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == INETNUM)
            .toList();

        final List<RpslObjectInfo> inet6numResult = references.stream()
            .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == INET6NUM)
            .toList();

        return getOrganisationResponse(request, organisation, autnumResult, inetnumResult, inet6numResult);
    }

    final Set<RpslObjectInfo> getReferences(final RpslObject organisation) {
        final Source originalSource = sourceContext.getCurrentSource();
        try {
            sourceContext.setCurrent(sourceContext.getSlaveSource());
            return rpslObjectUpdateDao.getReferences(organisation);
        } finally {
            sourceContext.setCurrent(originalSource);
        }
    }

    private Query getQueryObject(final Set<ObjectType> objectTypes, final String key) {
        return Query.parse(
                String.format("%s %s %s %s %s %s",
                            QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesToString(objectTypes),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));
    }

    private Response getOrganisationResponse(final HttpServletRequest request,
                                             final RpslObject organisation,
                                             final List<RpslObjectInfo> autnumResult,
                                             final List<RpslObjectInfo> inetnumResult,
                                             final List<RpslObjectInfo> inet6numResult) {
        return Response.ok(
                    rdapObjectMapper.mapOrganisationEntity(
                        getRequestUrl(request),
                        organisation,
                        autnumResult,
                        inetnumResult,
                        inet6numResult,
                        maxEntityResultSize))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private Object getDomainEntity(final HttpServletRequest request, final Stream<RpslObject> domainResult,
                              final Stream<RpslObject> inetnumResult) {
        final Iterator<RpslObject> domainIterator = domainResult.iterator();
        final Iterator<RpslObject> inetnumIterator = inetnumResult.iterator();
        if (!domainIterator.hasNext()) {
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }
        final RpslObject domainObject = domainIterator.next();
        final RpslObject inetnumObject = inetnumIterator.hasNext() ? inetnumIterator.next() : null;

        if (domainIterator.hasNext() || inetnumIterator.hasNext()) {
            throw new RdapException("500 Internal Error", "Unexpected result size: " + Iterators.size(domainIterator),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return rdapObjectMapper.mapDomainEntity(getRequestUrl(request), domainObject, inetnumObject);
    }

    private Response getResponse(final HttpServletRequest request, final Iterable<RpslObject> result) {
        Iterator<RpslObject> rpslIterator = result.iterator();

        if (!rpslIterator.hasNext()) {
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }

        final RpslObject resultObject = rpslIterator.next();

        if (rpslIterator.hasNext()) {
            throw new RdapException("500 Internal Error", "Unexpected result size: " + Iterators.size(rpslIterator),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        if (RdapObjectMapper.isIANABlock(resultObject)){
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }

        return Response.ok(
                rdapObjectMapper.map(
                        getRequestUrl(request),
                        resultObject,
                        abuseCFinder.getAbuseContact(resultObject).orElse(null)))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private Response redirect(final String requestPath, final Query query) {
        final URI uri;
        try {
            uri = delegatedStatsService.getUriForRedirect(requestPath, query);
        } catch (WebApplicationException e) {
            LOGGER.debug(e.getMessage(), e);
            throw new RdapException("404 Not found", "Redirect URI not found", HttpStatus.NOT_FOUND_404);
        }

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

    private Response redirect(final String requestPath, @Nullable final ObjectType objectType, final String searchValue) {
        final URI uri;
        try {
            uri = delegatedStatsService.getUriForRedirect(requestPath, objectType, searchValue);
        } catch (WebApplicationException e) {
            LOGGER.debug(e.getMessage(), e);
            throw new RdapException("404 Not found", "Redirect URI not found", HttpStatus.NOT_FOUND_404);
        }

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

    private Response redirectDomain(final String requestPath, final Domain domain) {
        final URI uri;
        try {
            uri = delegatedStatsService.getUriForRedirect(
                        requestPath,
                        getReverseObjectType(domain),
                        domain.getReverseIp().toString());
        } catch (WebApplicationException e) {
            LOGGER.debug(e.getMessage(), e);
            throw new RdapException("404 Not found", "Redirect URI not found", HttpStatus.NOT_FOUND_404);
        }

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();
    }

    private String getRequestUrl(final HttpServletRequest request) {
        if (StringUtils.isNotEmpty(baseUrl)) {
            // TODO: don't include local base URL (lookup from request context and replace)
            return String.format("%s%s", baseUrl, getRequestPath(request).replaceFirst("/rdap", ""));
        }
        final StringBuffer buffer = request.getRequestURL();
        if (request.getQueryString() != null) {
            buffer.append('?');
            buffer.append(request.getQueryString());
        }
        return buffer.toString();
    }

    private String getRequestPath(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            builder.append('?');
            builder.append(request.getQueryString());
        }
        return builder.toString();
    }

    private String objectTypesToString(final Collection<ObjectType> objectTypes) {
        return COMMA_JOINER.join(objectTypes.stream().map(ObjectType::getName).toList());
    }

    private List<RpslObject> handleRelationQuery(final HttpServletRequest request, final RdapRequestType requestType,
                                                 final RelationType relationType, final String key) {
        final List<RpslObject> rpslObjects;
        switch (requestType) {
            case AUTNUMS -> throw new RdapException("400 Bad Request", "Relation queries not allowed for autnum", HttpStatus.BAD_REQUEST_400);
            case DOMAINS -> {
                rdapRequestValidator.validateDomain(key);
                final List<String> relatedPkeys = rdapRelationService.getDomainRelationPkeys(key, relationType);

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), relatedPkey), request))
                        .toList();
            }
            case IPS -> {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                final List<String> relatedPkeys = rdapRelationService.getInetnumRelationPkeys(key, relationType);

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), relatedPkey), request))
                        .toList();
            }
            default -> throw new RdapException("400 Bad Request", "Invalid or unknown type " + requestType.toString().toLowerCase(), HttpStatus.BAD_REQUEST_400);
        }
        return rpslObjects;
    }

    private Response handleSearch(final String[] fields, final String term, final HttpServletRequest request) {
        LOGGER.debug("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw new RdapException("400 Bad Request", "Empty search term", HttpStatus.BAD_REQUEST_400);
        }

        try {
            final List<RpslObject> objects = rdapFullTextSearch.performSearch(fields, term, request.getRemoteAddr(), source);

            if (objects.isEmpty()) {
                throw new RdapException("404 Not Found", "Requested object not found: " + term, HttpStatus.NOT_FOUND_404);
            }

            return Response.ok(rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    maxResultSize))
                    .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                    .build();
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RdapException("500 Internal Error", "search failed", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
}
