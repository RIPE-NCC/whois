package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
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
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
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
import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;

@Component
@Path("/")
public class RdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapService.class);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private final RdapLookupService rdapService;
    private final RdapRequestValidator rdapRequestValidator;
    private final DelegatedStatsService delegatedStatsService;
    private final Source source;
    public static final Joiner COMMA_JOINER = Joiner.on(",");
    private final RdapObjectMapper rdapObjectMapper;
    private final RdapFullTextSearch rdapFullTextSearch;
    private final String baseUrl;
    private final int maxResultSize;
    private final RdapRelationService rdapRelationService;


    /**
     *
     * @param rdapObjectMapper
     * @param delegatedStatsService
     * @param rdapFullTextSearch
     * @param sourceContext
     * @param baseUrl
     * @param rdapRequestValidator
     * @param maxResultSize: If the response is bigger than maxResultSize, we truncate the response and we add a notification
     */
    @Autowired
    public RdapService(final RdapLookupService rdapService,
                       final RdapRequestValidator rdapRequestValidator,
                       final DelegatedStatsService delegatedStatsService,
                       final RdapObjectMapper rdapObjectMapper,
                       final RdapFullTextSearch rdapFullTextSearch,
                       @Value("${rdap.public.baseUrl:}") final String baseUrl,
                       @Value("${rdap.search.max.results:100}") final int maxResultSize,
                       final SourceContext sourceContext, RdapRelationService rdapRelationService) {
        this.rdapService = rdapService;
        this.rdapRequestValidator = rdapRequestValidator;
        this.delegatedStatsService = delegatedStatsService;
        this.rdapObjectMapper = rdapObjectMapper;
        this.rdapFullTextSearch = rdapFullTextSearch;
        this.source = sourceContext.getCurrentSource();
        this.baseUrl = baseUrl;
        this.maxResultSize = maxResultSize;
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

        return handleLookupWithRedirections(request, requestType, key, whoisObjectTypes);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/entities")
    public Response searchEntities(
            @Context final HttpServletRequest request,
            @QueryParam("fn") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        Object object = null;
        if (name != null && handle == null) {
            object = handleSearch(new String[]{"person", "role", "org-name"}, name, request);
        }

        if (name == null && handle != null) {
            object = handleSearch(new String[]{"organisation", "nic-hdl", "mntner"}, handle, request);
        }

        if (object == null){
            throw new RdapException("400 Bad Request", "Either fn or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
        }

        return Response.ok(object)
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/ips")
    public Response searchIps(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        Object object = null;
        if (name != null && handle == null) {
            object = handleSearch(new String[]{"netname"}, name, request);
        }

        if (name == null && handle != null) {
            final Ipv4Resource ipv4Resource = Ipv4Resource.parseIPv4Resource(handle);
            object = handleSearch(new String[]{"inetnum", "inet6num"}, ipv4Resource != null ? ipv4Resource.toRangeString() : handle, request);
        }

        if (object == null) {
            throw new RdapException("400 Bad Request", "Either name or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
        }

        return Response.ok(object)
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/autnums")
    public Response searchAutnums(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name,
            @QueryParam("handle") final String handle) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));

        Object object = null;
        if (name != null && handle == null) {
            object = handleSearch(new String[]{"as-name"}, name, request);
        }

        if (name == null && handle != null) {
            object = handleSearch(new String[]{"aut-num"}, handle, request);
        }

        if (object == null){
            throw new RdapException("400 Bad Request", "Either name or handle is a required parameter, but never both", HttpStatus.BAD_REQUEST_400);
        }

        return Response.ok(object)
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();

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

        return Response.ok(handleSearch(new String[]{"domain"}, name, request))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
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
            @PathParam("relation") String relationType,
            @PathParam("key") final String key,
            @QueryParam("status") String status) {

        final RelationType relation = RelationType.fromName(relationType);
        //TODO: [MH] Status is being ignored until administrative resources are included in RDAP. If status is not
        // given or status is inactive...include administrative resources in the output. However, if status is active
        // return just non administrative resources, as we are doing now.
        if ("inactive".equalsIgnoreCase(status)) {
            throw new RdapException("501 Not Implemented", "Inactive status is not implemented", HttpStatus.NOT_IMPLEMENTED_501);
        }

        if (!StringUtil.isNullOrEmpty(status) && (relation.equals(RelationType.DOWN) || relation.equals(RelationType.BOTTOM))){
            throw new RdapException("501 Not Implemented", "Status is not implement in down and bottom relation", HttpStatus.NOT_IMPLEMENTED_501);
        }

        final Set<ObjectType> objectTypes = requestType.getWhoisObjectTypes(key);
        if (isRedirect(Iterables.getOnlyElement(objectTypes), key)) {
            return redirect(getRequestPath(request), getQueryObject(objectTypes, key));
        }

        return Response.ok(
                rdapRelationService.handleRelationQuery(
                        request, objectTypes,
                        requestType, relation,
                        key, getRequestUrl(request), maxResultSize))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }


    private Response handleLookupWithRedirections(HttpServletRequest request, RdapRequestType requestType, String key,
                                                  Set<ObjectType> whoisObjectTypes) {
        Object object;
        switch (requestType) {
            case AUTNUM -> {
                final String autnumKey = String.format("AS%s", key);
                rdapRequestValidator.validateAutnum(autnumKey);
                if (isRedirect(AUT_NUM, key) && !rdapRequestValidator.isReservedAsNumber(autnumKey)) {
                    return redirect(getRequestPath(request), AUT_NUM, autnumKey);
                }
                object = rdapService.lookupForAutNum(request, autnumKey);
            }
            case DOMAIN -> {
                rdapRequestValidator.validateDomain(key);
                final Domain domain = Domain.parse(key);

                if (isRedirectDomain(domain)) {
                    return redirectDomain(getRequestPath(request), domain);
                }
                object = rdapService.lookupForDomain(request, key, domain.getReverseIp().toString());
            }
            case IP -> {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                if (isRedirect(Iterables.getOnlyElement(whoisObjectTypes), key)) {
                    return redirect(getRequestPath(request), getQueryObject(whoisObjectTypes, key));
                }
                object = rdapService.lookupObject(request, whoisObjectTypes, key);
            }
            case ENTITY -> {
                rdapRequestValidator.validateEntity(key);
                object = key.toUpperCase().startsWith("ORG-") ? rdapService.lookupForOrganisation(request, key) :
                        rdapService.lookupObject(request, whoisObjectTypes, key);
            }
            case NAMESERVER -> {
                throw new RdapException("501 Not Implemented", "Nameserver not supported", HttpStatus.NOT_IMPLEMENTED_501);
            }
            default -> {
                throw new RdapException("400 Bad Request", "unknown type" + requestType, HttpStatus.BAD_REQUEST_400);
            }
        }

        return Response.ok(object)
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
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

    private String objectTypesToString(final Collection<ObjectType> objectTypes) {
        return COMMA_JOINER.join(objectTypes.stream().map(ObjectType::getName).toList());
    }

    private Boolean isRedirectDomain(final Domain domain) {
        return isRedirect(getReverseObjectType(domain), domain.getReverseIp().toString());
    }

    private ObjectType getReverseObjectType(final Domain domain) {
        return domain.getReverseIp() instanceof Ipv4Resource ? INETNUM : INET6NUM;
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

    private Boolean isRedirect(ObjectType objectType, final String key) {
        return !delegatedStatsService.isMaintainedInRirSpace(source.getName(), objectType, CIString.ciString(key));
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


    private Object handleSearch(final String[] fields, final String term, final HttpServletRequest request) {
        LOGGER.debug("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw new RdapException("400 Bad Request", "Empty search term", HttpStatus.BAD_REQUEST_400);
        }

        try {
            final List<RpslObject> objects = rdapFullTextSearch.performSearch(fields, term, request.getRemoteAddr(), source);

            return rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    maxResultSize);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RdapException("500 Internal Error", "search failed", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
}
