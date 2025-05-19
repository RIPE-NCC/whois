package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
import net.ripe.db.whois.common.sso.AuthServiceClient;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryParser;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Set;

import static net.ripe.db.whois.api.rest.RestServiceHelper.isQueryParamSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.query.QueryFlag.ABUSE_CONTACT;
import static net.ripe.db.whois.query.QueryFlag.ALL_SOURCES;
import static net.ripe.db.whois.query.QueryFlag.BRIEF;
import static net.ripe.db.whois.query.QueryFlag.CHARSET;
import static net.ripe.db.whois.query.QueryFlag.CLIENT;
import static net.ripe.db.whois.query.QueryFlag.DIFF_VERSIONS;
import static net.ripe.db.whois.query.QueryFlag.LIST_SOURCES;
import static net.ripe.db.whois.query.QueryFlag.LIST_SOURCES_OR_VERSION;
import static net.ripe.db.whois.query.QueryFlag.LIST_VERSIONS;
import static net.ripe.db.whois.query.QueryFlag.PERSISTENT_CONNECTION;
import static net.ripe.db.whois.query.QueryFlag.PRIMARY_KEYS;
import static net.ripe.db.whois.query.QueryFlag.SELECT_TYPES;
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
            CHARSET,

            // port43 filter flags that make no sense in xml/json
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

            // versions are accessible via REST URL /versions/
            DIFF_VERSIONS,
            LIST_VERSIONS,
            SHOW_VERSION
    );

    private static final Service SEARCH_SERVICE = new Service("search");

    private final AccessControlListManager accessControlListManager;
    private final SsoTokenTranslator ssoTokenTranslator;
    private final RpslObjectStreamer rpslObjectStreamer;
    private final SourceContext sourceContext;

    @Autowired
    public WhoisSearchService(
            final AccessControlListManager accessControlListManager,
            final SsoTokenTranslator ssoTokenTranslator,
            final RpslObjectStreamer rpslObjectStreamer,
            final SourceContext sourceContext) {
        this.accessControlListManager = accessControlListManager;
        this.ssoTokenTranslator = ssoTokenTranslator;
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
     * @param client Sends information about the client to the server
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
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("/search")
    public Response search(
            @Context final HttpServletRequest request,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey,
            @QueryParam("source") final Set<String> sources,
            @QueryParam("query-string") final String searchKey,
            @QueryParam("inverse-attribute") final Set<String> inverseAttributes,
            @QueryParam("client") final String client,
            @QueryParam("type-filter") final Set<String> types,
            @QueryParam("flags") final Set<String> flags,
            @QueryParam("unformatted") final String unformatted,
            @QueryParam("managed-attributes") final String managedAttributes,
            @QueryParam("resource-holder") final String resourceHolder,
            @QueryParam("abuse-contact") final String abuseContact,
            @QueryParam("limit") final Integer limit,
            @QueryParam("offset") final Integer offset,
            @QueryParam("override") final String override,
            @QueryParam("roa-check") @DefaultValue("false") final Boolean roaCheck) {

        validateSources(request, sources);
        validateSearchKey(request, searchKey);

        final Set<QueryFlag> separateFlags = splitInputFlags(request, flags);
        checkForInvalidFlags(request, separateFlags);

        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addCommaList(QueryFlag.SOURCES, sources);
        queryBuilder.addCommaList(QueryFlag.SELECT_TYPES, types);
        queryBuilder.addCommaList(QueryFlag.INVERSE, inverseAttributes);

        if (client != null) {
            queryBuilder.addCommaList(QueryFlag.CLIENT, client);
        }

        for (QueryFlag separateFlag : separateFlags) {
            queryBuilder.addFlag(separateFlag);
        }


        final Query query = Query.parse(queryBuilder.build(searchKey), ssoTokenTranslator.translateSsoTokenOrNull(crowdTokenKey), override, Query.Origin.REST,
                isTrusted(request));

        final Parameters parameters = new Parameters.Builder()
                .inverseAttributes(new InverseAttributes(inverseAttributes))
                .client(client)
                .typeFilters(new TypeFilters(types))
                .flags(new Flags(separateFlags))
                .queryStrings(new QueryStrings(new QueryString(searchKey)))
                .sources(new Sources(sources))
                .managedAttributes(isQueryParamSet(managedAttributes))
                .resourceHolder(isQueryParamSet(resourceHolder))
                .abuseContact(isQueryParamSet(abuseContact))
                .roaCheck(roaCheck)
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
