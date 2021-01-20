package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.fulltextsearch.IndexTemplate;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static net.ripe.db.whois.api.fulltextsearch.FullTextIndex.LOOKUP_KEY_FIELD_NAME;
import static net.ripe.db.whois.common.rpsl.ObjectType.AS_BLOCK;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;

@Component
@Path("/")
public class WhoisRdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapService.class);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    //sort for consistent search results
    private static final Sort SORT_BY_OBJECT_TYPE =
            new Sort(new SortField(FullTextIndex.OBJECT_TYPE_FIELD_NAME, SortField.Type.STRING), new SortField(LOOKUP_KEY_FIELD_NAME, SortField.Type.STRING));

    private final int maxResultSize;

    private final RdapQueryHandler rdapQueryHandler;
    private final RpslObjectDao objectDao;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;
    private final FullTextIndex fullTextIndex;
    private final Source source;
    private final String baseUrl;
    private final AccessControlListManager accessControlListManager;
    private final RdapRequestValidator rdapRequestValidator;

    @Autowired
    public WhoisRdapService(final RdapQueryHandler rdapQueryHandler,
                            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao objectDao,
                            final AbuseCFinder abuseCFinder,
                            final RdapObjectMapper rdapObjectMapper,
                            final DelegatedStatsService delegatedStatsService,
                            final FullTextIndex fullTextIndex,
                            final SourceContext sourceContext,
                            @Value("${rdap.public.baseUrl:}") final String baseUrl,
                            final AccessControlListManager accessControlListManager,
                            final RdapRequestValidator rdapRequestValidator,
                            @Value("${rdap.search.max.results:100}") final int maxResultSize) {
        this.rdapQueryHandler = rdapQueryHandler;
        this.objectDao = objectDao;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = rdapObjectMapper;
        this.delegatedStatsService = delegatedStatsService;
        this.fullTextIndex = fullTextIndex;
        this.source = sourceContext.getCurrentSource();
        this.baseUrl = baseUrl;
        this.accessControlListManager = accessControlListManager;
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

        if (resultObject.getKey().equals(CIString.ciString("0.0.0.0 - 255.255.255.255")) ||
                resultObject.getKey().equals(CIString.ciString("::/0"))) {
            // TODO: handle root object in RIPE space
            throw new NotFoundException("not found");
        }

        return Response.ok(
                rdapObjectMapper.map(
                        getRequestUrl(request),
                        resultObject,
                        objectDao.getLastUpdated(resultObject.getObjectId()),
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
            final List<RpslObject> objects = fullTextIndex.search(
                    new IndexTemplate.AccountingSearchCallback<List<RpslObject>>(accessControlListManager, request.getRemoteAddr(), source) {

                        @Override
                        protected List<RpslObject> doSearch(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException {
                            final Stopwatch stopWatch = Stopwatch.createStarted();

                            final List<RpslObject> results = Lists.newArrayList();
                            try {
                                final QueryParser queryParser = new MultiFieldQueryParser(fields, new RdapAnalyzer());
                                queryParser.setAllowLeadingWildcard(true);
                                queryParser.setDefaultOperator(QueryParser.Operator.AND);

                                // TODO SB: Yuck, query is case insensitive by default
                                // but case sensitivity also depends on field type
                                final org.apache.lucene.search.Query query = queryParser.parse(term.toLowerCase());

                                final TopDocs topDocs = indexSearcher.search(query, maxResultSize, SORT_BY_OBJECT_TYPE);
                                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                                    final Document document = indexSearcher.doc(scoreDoc.doc);

                                    final RpslObject rpslObject;
                                    try {
                                        rpslObject = objectDao.getById(getObjectId(document));
                                    } catch (EmptyResultDataAccessException e) {
                                        // object was deleted from the database but index was not updated yet
                                        continue;
                                    }
                                    account(rpslObject);
                                    results.add(rpslObject);
                                }

                                LOGGER.debug("Found {} objects in {}", results.size(), stopWatch.stop());
                                return results;

                            } catch (ParseException e) {
                                LOGGER.error("handleSearch", e);
                                throw new BadRequestException("cannot parse query " + term);
                            }
                        }
                    });

            if (objects.isEmpty()) {
                throw new NotFoundException("not found");
            }

            final Iterable<LocalDateTime> lastUpdateds = objects.stream().map(input -> objectDao.getLastUpdated(input.getObjectId())).collect(Collectors.toList());

            return Response.ok(rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    lastUpdateds,
                    maxResultSize))
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
        protected TokenStreamComponents createComponents(final String fieldName) {
            final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
            TokenStream tok = new WordDelimiterGraphFilter(
                    tokenizer,
                    WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
                    CharArraySet.EMPTY_SET);
            tok = new LowerCaseFilter(tok);
            return new TokenStreamComponents(tokenizer, tok);
        }
    }
}
