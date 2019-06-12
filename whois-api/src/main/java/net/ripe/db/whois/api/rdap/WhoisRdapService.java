package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.fulltextsearch.IndexTemplate;
import net.ripe.db.whois.api.rest.ApiResponseHandler;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@Component
@Path("/")
public class WhoisRdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapService.class);
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private static final int SEARCH_MAX_RESULTS = 100;

    private final QueryHandler queryHandler;
    private final RpslObjectDao objectDao;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;
    private final FullTextIndex fullTextIndex;
    private final Source source;
    private final String baseUrl;
    private final AccessControlListManager accessControlListManager;

    @Autowired
    public WhoisRdapService(final QueryHandler queryHandler,
                            final RpslObjectDao objectDao,
                            final AbuseCFinder abuseCFinder,
                            final RdapObjectMapper rdapObjectMapper,
                            final DelegatedStatsService delegatedStatsService,
                            final FullTextIndex fullTextIndex,
                            final SourceContext sourceContext,
                            @Value("${rdap.port43:}") final String port43,
                            @Value("${rdap.public.baseUrl:}") final String baseUrl,
                            final AccessControlListManager accessControlListManager) {
        this.queryHandler = queryHandler;
        this.objectDao = objectDao;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = rdapObjectMapper;
        this.delegatedStatsService = delegatedStatsService;
        this.fullTextIndex = fullTextIndex;
        this.source = sourceContext.getCurrentSource();
        this.baseUrl = baseUrl;
        this.accessControlListManager = accessControlListManager;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") final String objectType,
                           @PathParam("key") final String key) {

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

        switch (objectType.toLowerCase()) {
            case "autnum": {
                String autnumKey = String.format("AS%s", key);
                validateAutnum(autnumKey);
                return lookupResource(request, AUT_NUM, autnumKey);
            }
            case "domain": {
                validateDomain(key);
                final Set<ObjectType> whoisObjectTypes = Sets.newHashSet();
                whoisObjectTypes.add(DOMAIN);
                return lookupObject(request, whoisObjectTypes, key);
            }
            case "ip": {
                validateIp(request.getRequestURI(), key);
                return lookupResource(request, key.contains(":") ? INET6NUM : INETNUM, key);
            }
            case "entity": {
                try {
                    validateEntity(key);
                } catch (IllegalArgumentException e) {
                    throw badRequest(e.getMessage());
                }

                final Set<ObjectType> whoisObjectTypes = Sets.newHashSet();
                if (key.toUpperCase().startsWith("ORG-")) {
                    whoisObjectTypes.add(ORGANISATION);
                } else {
                    whoisObjectTypes.add(PERSON);
                    whoisObjectTypes.add(ROLE);
                }

                return lookupObject(request, whoisObjectTypes, key);
            }
            case "nameserver": {
                throw notFound("nameserver not found");
            }
            default: {
                throw badRequest("unknown type");
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

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

        if (name != null && handle == null) {
            return handleSearch(new String[]{"person", "role", "org-name"}, name, request);
        }

        if (name == null && handle != null) {
            return handleSearch(new String[]{"organisation", "nic-hdl"}, handle, request);
        }

        throw badRequest("bad request");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/nameservers")
    public Response searchNameservers(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

        if (StringUtils.isEmpty(name)) {
            throw badRequest("empty lookup key");
        }

        throw notFound("nameservers not found");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/domains")
    public Response searchDomains(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

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

    private void validateDomain(final String key) {
        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw notFound("RIPE NCC does not support forward domain queries.");
        }
    }

    private void validateIp(final String rawUri, final String key) {
        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw badRequest("Invalid syntax.");
        }

        if (rawUri.contains("//")) {
            throw badRequest("Invalid syntax.");
        }
    }

    private void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw badRequest("Invalid syntax.");
        }
    }

    private void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw new IllegalArgumentException("Invalid syntax.");
            }
        } else {
            if (!AttributeType.NIC_HDL.isValidValue(ObjectType.PERSON, key)) {
                throw new IllegalArgumentException("Invalid syntax.");
            }
        }
    }

    private BadRequestException badRequest(final String errorTitle) {
        return new BadRequestException(createErrorResponse(Response.Status.BAD_REQUEST, errorTitle));
    }

    private NotFoundException notFound(final String errorTitle) {
        return new NotFoundException(createErrorResponse(Response.Status.NOT_FOUND, errorTitle));
    }

    private WebApplicationException tooManyRequests() {
        return new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
    }

    private Response createErrorResponse(final Response.Status status, final String errorTitle) {
        return Response.status(status)
                .entity(rdapObjectMapper.mapError(status.getStatusCode(), errorTitle, emptyList()))
                .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private Response lookupResource(final HttpServletRequest request, final ObjectType objectType, final String key) {
        final Query query = Query.parse(
                String.format("%s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectType.getName(),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));

        if (!delegatedStatsService.isMaintainedInRirSpace(source.getName(), objectType, CIString.ciString(key))) {
            return redirect(getRequestPath(request), query);
        }

        return handleQuery(query, request);
    }

    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        if (StringUtils.isEmpty(key)) {
            throw badRequest("empty lookup term");
        }

        final Query query = Query.parse(
                String.format("%s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesToString(objectTypes),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));

        return handleQuery(query, request);
    }

    private Response handleQuery(final Query query, final HttpServletRequest request) {

        final int contextId = System.identityHashCode(Thread.currentThread());
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());

        final List<RpslObject> result = Lists.newArrayList();

        try {
            queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {
                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof RpslObject) {
                        result.add((RpslObject) responseObject);
                    }
                }
            });

            if (result.isEmpty()) {
                throw notFound("not found");
            }

            if (result.size() > 1) {
                throw new IllegalStateException("Unexpected result size: " + result.size());
            }

            final RpslObject resultObject = result.get(0);

            if (resultObject.getKey().equals(CIString.ciString("0.0.0.0 - 255.255.255.255")) ||
                    resultObject.getKey().equals(CIString.ciString("::/0"))) {
                // TODO: handle root object in RIPE space
                throw notFound("not found");
            }

            return Response.ok(
                    rdapObjectMapper.map(
                            getRequestUrl(request),
                            resultObject,
                            objectDao.getLastUpdated(resultObject.getObjectId()),
                            abuseCFinder.getAbuseContactRole(resultObject)))
                    .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                    .build();

        } catch (final QueryException e) {
            if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw tooManyRequests();
            } else {
                LOGGER.error(e.getMessage(), e);
                throw new IllegalStateException("query error");
            }
        }
    }

    private Response redirect(final String requestPath, final Query query) {
        final URI uri;

        try {
            uri = delegatedStatsService.getUriForRedirect(requestPath, query);
        } catch (WebApplicationException e) {
            throw notFound("not found");
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
        LOGGER.info("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw badRequest("empty search term");
        }

        try {
            final List<RpslObject> objects = fullTextIndex.search(
                    new IndexTemplate.AccountingSearchCallback<List<RpslObject>>(accessControlListManager, request.getRemoteAddr(), source) {

                @Override
                protected List<RpslObject> doSearch(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException {
                    final Stopwatch stopWatch = Stopwatch.createStarted();

                    final List<RpslObject> results = Lists.newArrayList();
                    final int maxResults = Math.max(SEARCH_MAX_RESULTS, indexReader.numDocs());
                    try {
                        final QueryParser queryParser = new MultiFieldQueryParser(fields, new RdapAnalyzer());
                        queryParser.setAllowLeadingWildcard(true);
                        queryParser.setDefaultOperator(QueryParser.Operator.AND);
                        final org.apache.lucene.search.Query query = queryParser.parse(term);
                        final TopDocs topDocs = indexSearcher.search(query, maxResults);
                        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                            final Document document = indexSearcher.doc(scoreDoc.doc);

                            final RpslObject rpslObject = convertToRpslObject(document);
                            account(rpslObject);
                            results.add(rpslObject);
                        }

                        LOGGER.info("Found {} objects in {}", results.size(), stopWatch.stop());
                        return results;

                    } catch (ParseException e) {
                        LOGGER.error("handleSearch", e);
                        throw badRequest("cannot parse query " + term);
                    }
                }
            });

            if (objects.isEmpty()) {
                throw notFound("not found");
            }

            final Iterable<LocalDateTime> lastUpdateds = objects.stream().map(input -> objectDao.getLastUpdated(input.getObjectId())).collect(Collectors.toList());

            return Response.ok(rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    lastUpdateds))
                    .header(CONTENT_TYPE, CONTENT_TYPE_RDAP_JSON)
                    .build();
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException("search failed");
        }
    }

    private class RdapAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
            final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(reader);
            TokenStream tok = new WordDelimiterFilter(
                    tokenizer,
                    WordDelimiterFilter.PRESERVE_ORIGINAL,
                    CharArraySet.EMPTY_SET);
            tok = new LowerCaseFilter(tok);
            return new TokenStreamComponents(tokenizer, tok);
        }
    }
}
