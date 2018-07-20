package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.Flags;
import net.ripe.db.whois.api.rest.domain.InverseAttributes;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.QueryString;
import net.ripe.db.whois.api.rest.domain.QueryStrings;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.Sources;
import net.ripe.db.whois.api.rest.domain.TypeFilters;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryParser;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Set;

import static net.ripe.db.whois.api.rest.RestServiceHelper.isQueryParamSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.query.QueryFlag.ABUSE_CONTACT;
import static net.ripe.db.whois.query.QueryFlag.ALL_SOURCES;
import static net.ripe.db.whois.query.QueryFlag.BRIEF;
import static net.ripe.db.whois.query.QueryFlag.CLIENT;
import static net.ripe.db.whois.query.QueryFlag.DIFF_VERSIONS;
import static net.ripe.db.whois.query.QueryFlag.FILTER_TAG_EXCLUDE;
import static net.ripe.db.whois.query.QueryFlag.FILTER_TAG_INCLUDE;
import static net.ripe.db.whois.query.QueryFlag.LIST_SOURCES;
import static net.ripe.db.whois.query.QueryFlag.LIST_SOURCES_OR_VERSION;
import static net.ripe.db.whois.query.QueryFlag.LIST_VERSIONS;
import static net.ripe.db.whois.query.QueryFlag.NO_GROUPING;
import static net.ripe.db.whois.query.QueryFlag.NO_TAG_INFO;
import static net.ripe.db.whois.query.QueryFlag.PERSISTENT_CONNECTION;
import static net.ripe.db.whois.query.QueryFlag.PRIMARY_KEYS;
import static net.ripe.db.whois.query.QueryFlag.SELECT_TYPES;
import static net.ripe.db.whois.query.QueryFlag.SHOW_TAG_INFO;
import static net.ripe.db.whois.query.QueryFlag.SHOW_VERSION;
import static net.ripe.db.whois.query.QueryFlag.SOURCES;
import static net.ripe.db.whois.query.QueryFlag.TEMPLATE;
import static net.ripe.db.whois.query.QueryFlag.VERBOSE;
import static net.ripe.db.whois.query.QueryFlag.VERSION;


@Component
@Path("/")
public class WhoisSearchService {

    private static final Set<QueryFlag> NOT_ALLOWED_SEARCH_QUERY_FLAGS = ImmutableSet.of(
            // flags for port43 only
            VERSION,
            PERSISTENT_CONNECTION,

            // port43 filter flags that make no sense in xml/json
            NO_GROUPING,
            BRIEF,
            ABUSE_CONTACT,
            PRIMARY_KEYS,

            // flags that are covered by path/query params or other rest calls
            TEMPLATE,
            VERBOSE,
            CLIENT,
            LIST_SOURCES,
            LIST_SOURCES_OR_VERSION,
            SOURCES,
            ALL_SOURCES,
            SELECT_TYPES,

            // tags are handled from queryparam
            NO_TAG_INFO,
            SHOW_TAG_INFO,
            FILTER_TAG_EXCLUDE,
            FILTER_TAG_INCLUDE,

            // versions are accessible via REST URL /versions/
            DIFF_VERSIONS,
            LIST_VERSIONS,
            SHOW_VERSION
    );

    private static final Service SEARCH_SERVICE = new Service("search");

    private final AccessControlListManager accessControlListManager;
    private final RpslObjectStreamer rpslObjectStreamer;
    private final SourceContext sourceContext;

    @Autowired
    public WhoisSearchService(
            final AccessControlListManager accessControlListManager,
            final RpslObjectStreamer rpslObjectStreamer,
            final SourceContext sourceContext) {
        this.accessControlListManager = accessControlListManager;
        this.rpslObjectStreamer = rpslObjectStreamer;
        this.sourceContext = sourceContext;
    }

    /**
     * The search interface resembles a standard Whois client query with the extra features of multi-registry client,
     * multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.
     *
     * @param sources source database(s) to search, defaults to RIPE
     * @param searchKey (Mandatory) query search key
     * @param inverseAttributes perform an inverse query using the specified attribute(s)
     * @param includeTags return only objects with the specified tag(s)
     * @param excludeTags do not return objects with the specified tag(s)
     * @param types Filter results by object type(s)
     * @param flags Whois Query search flags
     * @param unformatted return attribute values without formatting
     * @param managedAttributes annotate attributes which are managed by the RIPE NCC
     * @param resourceHolder annotate resource object(s) with the associated responsible organisation (if any)
     * @param abuseContact annotate resource and organisation object(s) with associated abuse contact (if any)
     * @param limit maximum number of objects to return
     * @param offset starting offset in results to return objects from
     *
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/search")
    public Response search(
            @Context final HttpServletRequest request,
            @QueryParam("source") final Set<String> sources,
            @QueryParam("query-string") final String searchKey,
            @QueryParam("inverse-attribute") final Set<String> inverseAttributes,
            @QueryParam("include-tag") final Set<String> includeTags,
            @QueryParam("exclude-tag") final Set<String> excludeTags,
            @QueryParam("type-filter") final Set<String> types,
            @QueryParam("flags") final Set<String> flags,
            @QueryParam("unformatted") final String unformatted,
            @QueryParam("managed-attributes") final String managedAttributes,
            @QueryParam("resource-holder") final String resourceHolder,
            @QueryParam("abuse-contact") final String abuseContact,
            @QueryParam("limit") final Integer limit,
            @QueryParam("offset") final Integer offset) {

        validateSources(request, sources);
        validateSearchKey(request, searchKey);

        final Set<QueryFlag> separateFlags = splitInputFlags(request, flags);
        checkForInvalidFlags(request, separateFlags);

        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addFlag(QueryFlag.SHOW_TAG_INFO);
        queryBuilder.addCommaList(QueryFlag.SOURCES, sources);
        queryBuilder.addCommaList(QueryFlag.SELECT_TYPES, types);
        queryBuilder.addCommaList(QueryFlag.INVERSE, inverseAttributes);
        queryBuilder.addCommaList(QueryFlag.FILTER_TAG_INCLUDE, includeTags);
        queryBuilder.addCommaList(QueryFlag.FILTER_TAG_EXCLUDE, excludeTags);

        for (QueryFlag separateFlag : separateFlags) {
            queryBuilder.addFlag(separateFlag);
        }

        final Query query = Query.parse(queryBuilder.build(searchKey), Query.Origin.REST, isTrusted(request));

        final Parameters parameters = new Parameters.Builder()
                .inverseAttributes(new InverseAttributes(inverseAttributes))
                .typeFilters(new TypeFilters(types))
                .flags(new Flags(separateFlags))
                .queryStrings(new QueryStrings(new QueryString(searchKey)))
                .sources(new Sources(sources))
                .managedAttributes(isQueryParamSet(managedAttributes))
                .resourceHolder(isQueryParamSet(resourceHolder))
                .abuseContact(isQueryParamSet(abuseContact))
                .limit(limit)
                .offset(offset)
                .unformatted(isQueryParamSet(unformatted))
                .build();

        return rpslObjectStreamer.handleQueryAndStreamResponse(
                query,
                request,
                InetAddresses.forString(request.getRemoteAddr()),
                parameters,
                SEARCH_SERVICE);
    }

    private void validateSearchKey(final HttpServletRequest request, final String searchKey) {
        if (StringUtils.isBlank(searchKey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.queryStringEmpty()))
                    .build());
        }

        try {
            if (QueryParser.hasFlags(searchKey)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(RestServiceHelper.createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString()))
                        .build());
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString()))
                    .build());
        }
    }

    private void validateSources(final HttpServletRequest request, final Set<String> sources) {
        for (final String source : sources) {
            if (!sourceContext.isOutOfRegion(source) && !sourceContext.getAllSourceNames().contains(ciString(source))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                        .build());
            }
        }
    }

    private void checkForInvalidFlags(final HttpServletRequest request, final Set<QueryFlag> flags) {
        for (final QueryFlag flag : flags) {
            if (NOT_ALLOWED_SEARCH_QUERY_FLAGS.contains(flag)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(RestServiceHelper.createErrorEntity(request, RestMessages.disallowedSearchFlag(flag)))
                        .build());
            }
        }
    }

    private Set<QueryFlag> splitInputFlags(final HttpServletRequest request, final Set<String> inputFlags) {
        final Set<QueryFlag> separateFlags = Sets.newLinkedHashSet();  // reporting errors should happen in the same order
        for (final String flagParameter : inputFlags) {
            final QueryFlag forLongFlag = QueryFlag.getForLongFlag(flagParameter);
            if (forLongFlag != null) {
                separateFlags.add(forLongFlag);
            } else {
                final CharacterIterator charIterator = new StringCharacterIterator(flagParameter);
                for (char flag = charIterator.first(); flag != CharacterIterator.DONE; flag = charIterator.next()) {
                    final String flagString = String.valueOf(flag);
                    final QueryFlag forShortFlag = QueryFlag.getForShortFlag(flagString);
                    if (forShortFlag != null) {
                        separateFlags.add(forShortFlag);
                    } else {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSearchFlag(flagParameter, flagString)))
                                .build());
                    }
                }
            }
        }
        return separateFlags;
    }

    private boolean isTrusted(final HttpServletRequest request) {
        return accessControlListManager.isTrusted(InetAddresses.forString(request.getRemoteAddr()));
    }
}
