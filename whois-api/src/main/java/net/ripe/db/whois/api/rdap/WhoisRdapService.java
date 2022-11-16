package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static net.ripe.db.whois.common.rpsl.ObjectType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;

@Component
@Path("/")
public class WhoisRdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapService.class);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private final int maxResultSize;

    private final RdapQueryHandler rdapQueryHandler;
    private final RpslObjectDao objectDao;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;
    private final RdapFullTextSearch rdapFullTextSearch;
    private final Source source;
    private final String baseUrl;
    private final RdapRequestValidator rdapRequestValidator;

    @Autowired
    public WhoisRdapService(final RdapQueryHandler rdapQueryHandler,
                            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao objectDao,
                            final AbuseCFinder abuseCFinder,
                            final RdapObjectMapper rdapObjectMapper,
                            final DelegatedStatsService delegatedStatsService,
                            final RdapFullTextSearch rdapFullTextSearch,
                            final SourceContext sourceContext,
                            @Value("${rdap.public.baseUrl:}") final String baseUrl,
                            final RdapRequestValidator rdapRequestValidator,
                            @Value("${rdap.search.max.results:100}") final int maxResultSize) {
        this.rdapQueryHandler = rdapQueryHandler;
        this.objectDao = objectDao;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = rdapObjectMapper;
        this.delegatedStatsService = delegatedStatsService;
        this.rdapFullTextSearch = rdapFullTextSearch;
        this.source = sourceContext.getCurrentSource();
        this.baseUrl = baseUrl;
        this.rdapRequestValidator = rdapRequestValidator;
        this.maxResultSize = maxResultSize;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") RdapRequestType requestType,
                           @PathParam("key") final String key) {

        LOGGER.debug("Request: {}", RestServiceHelper.getRequestURI(request));
        if (requestType == null) {
            throw new BadRequestException("unknown objectType");
        }

        final Set<ObjectType> whoisObjectTypes = requestType.getWhoisObjectTypes(key);  // null

        switch (requestType) {
            case AUTNUM: {
                String autnumKey = String.format("AS%s", key);
                rdapRequestValidator.validateAutnum(autnumKey);
                return lookupForAutNum(request, autnumKey);
            }
            case DOMAIN: {
                rdapRequestValidator.validateDomain(key);
                return lookupObject(request, whoisObjectTypes, key);
            }
            case IP: {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                return lookupWithRedirectUrl(request, whoisObjectTypes, key);
            }
            case ENTITY: {
                rdapRequestValidator.validateEntity(key);
                return lookupObject(request, whoisObjectTypes, key);
            }
            case NAMESERVER: {
                throw new ServerErrorException("Nameserver not supported", Response.Status.NOT_IMPLEMENTED);
            }
            default: {
                throw new BadRequestException("unknown type");
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
            return handleSearch(new String[]{"organisation", "nic-hdl"}, handle, request);
        }

        throw new BadRequestException("bad request");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/nameservers")
    public Response searchNameservers(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {
        throw new ServerErrorException("Nameserver not supported", Response.Status.NOT_IMPLEMENTED);
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

    private Response lookupWithRedirectUrl(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        if (isRedirect(Iterables.getOnlyElement(objectTypes), key)) {
            return redirect(getRequestPath(request), getQueryObject(objectTypes, key));
        }
        return lookupObject(request, objectTypes, key);
    }

    private Response lookupForAutNum(final HttpServletRequest request, final String key) {
        if (isRedirect(AUT_NUM, key) && !rdapRequestValidator.isReservedAsNumber(key)) {
            return redirect(getRequestPath(request), getQueryObject(ImmutableSet.of(AUT_NUM), key));
        }

        //if no autnum is found, as-block should be returned
        final Query query = getQueryObject(ImmutableSet.of(AUT_NUM, AS_BLOCK), key);
        List<RpslObject> result = rdapQueryHandler.handleAutNumQuery(query, request);

        return getResponse(request, result);
    }

    private Boolean isRedirect(ObjectType objectType, final String key) {
        return !delegatedStatsService.isMaintainedInRirSpace(source.getName(), objectType, CIString.ciString(key));
    }

    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        List<RpslObject> result =  rdapQueryHandler.handleQuery(getQueryObject(objectTypes, key), request);
        return getResponse(request, result);
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

    private Response getResponse(HttpServletRequest request, List<RpslObject> result) {
        if (result.isEmpty()) {
            throw new NotFoundException("not found");
        }

        if (result.size() > 1) {
            throw new IllegalStateException("Unexpected result size: " + result.size());
        }


        final RpslObject resultObject = result.get(0);


        return Response.ok(
                rdapObjectMapper.map(
                        getRequestUrl(request),
                        resultObject,
                        abuseCFinder.getAbuseContact(resultObject)))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private Response redirect(final String requestPath, final Query query) {
        final URI uri;

        try {
            uri = delegatedStatsService.getUriForRedirect(requestPath, query);
        } catch (WebApplicationException e) {
            throw new NotFoundException("not found");
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
        return COMMA_JOINER.join(objectTypes.stream().map(ObjectType::getName).collect(Collectors.toList()));
    }

    private Response handleSearch(final String[] fields, final String term, final HttpServletRequest request) {
        LOGGER.debug("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw new BadRequestException("empty search term");
        }

        try {
            final List<RpslObject> objects = rdapFullTextSearch.performSearch(fields, term, request.getRemoteAddr(), source);

            if (objects.isEmpty()) {
                throw new NotFoundException("not found");
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
            throw new IllegalStateException("search failed");
        }
    }
}
