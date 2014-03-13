package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.api.rest.ApiResponseHandler;
import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.api.search.IndexTemplate;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
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
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROLE;

@Component
@Path("/")
public class WhoisRdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapService.class);
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final Set<ObjectType> ABUSE_CONTACT_TYPES = Sets.newHashSet(AUT_NUM, INETNUM, INET6NUM);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";
    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private static final int SEARCH_MAX_RESULTS = 100;
    private static final Set<String> SEARCH_INDEX_FIELDS_NOT_MAPPED_TO_RPSL_OBJECT = Sets.newHashSet("primary-key", "object-type", "lookup-key");

    private final QueryHandler queryHandler;
    private final RpslObjectDao objectDao;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;
    private final FreeTextIndex freeTextIndex;
    private final String source;
    private final String baseUrl;

    @Autowired
    public WhoisRdapService(final QueryHandler queryHandler,
                            final RpslObjectDao objectDao,
                            final AbuseCFinder abuseCFinder,
                            final NoticeFactory noticeFactory,
                            final DelegatedStatsService delegatedStatsService,
                            final FreeTextIndex freeTextIndex,
                            final SourceContext sourceContext,
                            @Value("${rdap.port43:}") final String port43,
                            @Value("${rdap.public.baseUrl:}") final String baseUrl) {
        this.queryHandler = queryHandler;
        this.objectDao = objectDao;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = new RdapObjectMapper(noticeFactory, port43);
        this.delegatedStatsService = delegatedStatsService;
        this.freeTextIndex = freeTextIndex;
        this.source = sourceContext.getCurrentSource().getName().toString();
        this.baseUrl = baseUrl;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType:(autnum|domain|ip|entity|nameserver)}/{key:.*}")
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") final String objectType,
                           @PathParam("key") final String key) {

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

        final Set<ObjectType> whoisObjectTypes = Sets.newHashSet();

        switch (objectType.toLowerCase()) {
            case "autnum":
                whoisObjectTypes.add(AUT_NUM);
                validateAutnum(getKey(whoisObjectTypes, key));
                break;

            case "domain":
                validateDomain(key);
                whoisObjectTypes.add(DOMAIN);
                break;

            case "ip":
                validateIp(request.getRequestURI(), key);
                whoisObjectTypes.add(key.contains(":") ? INET6NUM : INETNUM);
                break;

            case "entity":
                validateEntity(key);
                if (key.toUpperCase().startsWith("ORG-")) {
                    whoisObjectTypes.add(ORGANISATION);
                } else {
                    whoisObjectTypes.add(PERSON);
                    whoisObjectTypes.add(ROLE);
                }
                break;

            case "nameserver":
                return createErrorResponse(Response.Status.NOT_FOUND, "");

            default:
                return createErrorResponse(Response.Status.BAD_REQUEST, "");
        }

        return lookupObject(request, whoisObjectTypes, getKey(whoisObjectTypes, key));
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

        LOGGER.info("Bad request: {}", request.getRequestURI());
        return createErrorResponse(Response.Status.BAD_REQUEST, "");
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/nameservers")
    public Response searchNameservers(
            @Context final HttpServletRequest request,
            @QueryParam("name") final String name) {

        LOGGER.info("Request: {}", RestServiceHelper.getRequestURI(request));

        if (StringUtils.isEmpty(name)) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "");
        }

        return createErrorResponse(Response.Status.NOT_FOUND, "");
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

    private void validateDomain(final String key) {
        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw new WebApplicationException(createErrorResponse(Response.Status.NOT_FOUND, "RIPE NCC does not support forward domain queries."));
        }
    }

    private void validateIp(final String rawUri, final String key) {
        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(createErrorResponse(Response.Status.BAD_REQUEST, "Invalid syntax."));
        }

        if (rawUri.contains("//")) {
            throw new WebApplicationException(createErrorResponse(Response.Status.BAD_REQUEST, "Invalid syntax."));
        }
    }

    private void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw new WebApplicationException(createErrorResponse(Response.Status.BAD_REQUEST, "Invalid syntax."));
        }
    }

    private void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw new WebApplicationException(createErrorResponse(Response.Status.BAD_REQUEST, "Invalid syntax."));
            }
        } else {
            if (!AttributeType.NIC_HDL.isValidValue(ObjectType.PERSON, key)) {
                throw new WebApplicationException(createErrorResponse(Response.Status.BAD_REQUEST, "Invalid syntax."));
            }
        }
    }

    private Response createErrorResponse(final Response.Status status, final String errorTitle) {
        return Response.status(status)
                .entity(rdapObjectMapper.mapError(status.getStatusCode(), errorTitle, Collections.EMPTY_LIST))
                .header("Content-Type", CONTENT_TYPE_RDAP_JSON)
                .build();
    }

    private String getKey(final Set<ObjectType> objectTypes, final String key) {
        if (objectTypes.contains(AUT_NUM)) {
            return String.format("AS%s", key);
        }
        return key;
    }

    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
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

    protected Response handleQuery(final Query query, final HttpServletRequest request) {

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
                return redirect(getRequestPath(request), query);
            }

            if (result.size() > 1) {
                throw new IllegalStateException("Unexpected result size: " + result.size());
            }

            final RpslObject resultObject = result.get(0);

            if (resultObject.getKey().equals(CIString.ciString("0.0.0.0 - 255.255.255.255")) ||
                    resultObject.getKey().equals(CIString.ciString("::/0"))) {
                // TODO: handle root object
                return redirect(getRequestPath(request), query);
            }

            return Response.ok(
                    rdapObjectMapper.map(
                            getRequestUrl(request),
                            resultObject,
                            objectDao.getLastUpdated(resultObject.getObjectId()),
                            getAbuseContacts(resultObject)))
                    .header("Content-Type", CONTENT_TYPE_RDAP_JSON)
                    .build();

        } catch (final QueryException e) {
            if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
            } else {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    private Response redirect(final String requestPath, final Query query) {
        final URI uri = delegatedStatsService.getUriForRedirect(requestPath, query);
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

    private List<RpslObject> getAbuseContacts(final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        if (ABUSE_CONTACT_TYPES.contains(objectType)) {
            return abuseCFinder.getAbuseContactObjects(rpslObject);
        }
        return Collections.emptyList();
    }

    private String objectTypesToString(final Collection<ObjectType> objectTypes) {
        return COMMA_JOINER.join(Iterables.transform(objectTypes, new Function<ObjectType, String>() {
            @Override
            public String apply(final ObjectType input) {
                return input.getName();
            }
        }));
    }

    private Response handleSearch(final String[] fields, final String term, final HttpServletRequest request) {
        LOGGER.info("Search {} for {}", fields, term);
        try {
            final List<RpslObject> objects = freeTextIndex.search(new IndexTemplate.SearchCallback<List<RpslObject>>() {
                @Override
                public List<RpslObject> search(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException {
                    final Stopwatch stopWatch = new Stopwatch().start();
                    final List<RpslObject> results = Lists.newArrayList();
                    final int maxResults = Math.max(SEARCH_MAX_RESULTS, indexReader.numDocs());
                    try {
                        final QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_44, fields, new RdapAnalyzer());
                        queryParser.setAllowLeadingWildcard(true);
                        queryParser.setDefaultOperator(QueryParser.Operator.AND);
                        final org.apache.lucene.search.Query query = queryParser.parse(term);
                        final TopDocs topDocs = indexSearcher.search(query, maxResults);
                        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                            final Document document = indexSearcher.doc(scoreDoc.doc);
                            results.add(convertLuceneDocumentToRpslObject(document));
                        }

                        LOGGER.info("Found {} objects in {}", results.size(), stopWatch.stop());
                        return results;

                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });

            if (objects.isEmpty()) {
                return Response.status(NOT_FOUND).build();
            }

            final Iterable<LocalDateTime> lastUpdateds = Iterables.transform(objects, new Function<RpslObject, LocalDateTime>() {
                @Nullable
                @Override
                public LocalDateTime apply(@Nullable RpslObject input) {
                    return objectDao.getLastUpdated(input.getObjectId());
                }
            });

            return Response.ok(rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    lastUpdateds))
                    .header("Content-Type", CONTENT_TYPE_RDAP_JSON)
                    .build();

        } catch (IOException e) {
            LOGGER.error("Caught IOException", e);
            throw new IllegalStateException(e);
        } catch (Exception e) {
            LOGGER.error("Caught Exception", e);
            throw e;
        }
    }

    private RpslObject convertLuceneDocumentToRpslObject(Document document) {
        final List<RpslAttribute> attributes = Lists.newArrayList();
        int objectId = 0;

        for (final IndexableField field : document.getFields()) {
            if (SEARCH_INDEX_FIELDS_NOT_MAPPED_TO_RPSL_OBJECT.contains(field.name())) {
                if ("primary-key".equals(field.name())) {
                    objectId = Integer.parseInt(field.stringValue());
                }
            } else {
                attributes.add(new RpslAttribute(AttributeType.getByName(field.name()), field.stringValue()));
            }
        }

        attributes.add(new RpslAttribute(AttributeType.SOURCE, source));
        return new RpslObject(objectId, attributes);
    }

    private class RdapAnalyzer extends Analyzer {
        @Override
        protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
            final WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(Version.LUCENE_44, reader);
            TokenStream tok = new WordDelimiterFilter(
                    tokenizer,
                    WordDelimiterFilter.PRESERVE_ORIGINAL,
                    CharArraySet.EMPTY_SET);
            tok = new LowerCaseFilter(Version.LUCENE_44, tok);
            return new TokenStreamComponents(tokenizer, tok);
        }
    }
}
