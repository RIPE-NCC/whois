package net.ripe.db.whois.api.rest;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;

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
public class WhoisRestService {
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    public static final String SERVICE_SEARCH = "search";
    private static final Joiner JOINER = Joiner.on(",");
    private static final Splitter AMPERSAND = Splitter.on("&");

    private static final Set<String> NOT_ALLOWED_SEARCH_QUERY_FLAGS = Sets.newHashSet(Iterables.concat(
            // flags for port43 only
            VERSION.getFlags(),
            PERSISTENT_CONNECTION.getFlags(),

            // port43 filter flags that make no sense in xml/json
            NO_GROUPING.getFlags(),
            BRIEF.getFlags(),
            ABUSE_CONTACT.getFlags(),
            PRIMARY_KEYS.getFlags(),

            // flags that are covered by path/query params or other rest calls
            TEMPLATE.getFlags(),
            VERBOSE.getFlags(),
            CLIENT.getFlags(),
            LIST_SOURCES.getFlags(),
            LIST_SOURCES_OR_VERSION.getFlags(),
            SOURCES.getFlags(),
            ALL_SOURCES.getFlags(),
            SELECT_TYPES.getFlags(),

            // tags are handled from queryparam
            NO_TAG_INFO.getFlags(),
            SHOW_TAG_INFO.getFlags(),
            FILTER_TAG_EXCLUDE.getFlags(),
            FILTER_TAG_INCLUDE.getFlags(),

            // versions are accessible via REST URL /versions/
            DIFF_VERSIONS.getFlags(),
            LIST_VERSIONS.getFlags(),
            SHOW_VERSION.getFlags()
    ));

    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;
    private final RpslObjectDao rpslObjectDao;
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectMapper;
    private final InternalUpdatePerformer updatePerformer;

    @Autowired
    public WhoisRestService(final DateTimeProvider dateTimeProvider, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final SourceContext sourceContext, final QueryHandler queryHandler, final WhoisObjectServerMapper whoisObjectMapper, final InternalUpdatePerformer updatePerformer) {
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
        this.rpslObjectDao = rpslObjectDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        this.whoisObjectMapper = whoisObjectMapper;
        this.updatePerformer = updatePerformer;
    }

    @DELETE
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Response restDelete(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam("reason") @DefaultValue("--") final String reason,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(source);

        final RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);

        updatePerformer.performUpdate(
                createOrigin(request),
                updatePerformer.createUpdate(originalObject, passwords, reason, override),
                createContent(originalObject, passwords, reason, override),
                Keyword.NONE,
                loggerContext);

        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Response restUpdate(
            final WhoisResources resource,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(source);

        final RpslObject submittedObject = getSubmittedObject(resource);
        validateSubmittedObject(submittedObject, objectType, key);

        final RpslObject response = updatePerformer.performUpdate(
                createOrigin(request),
                updatePerformer.createUpdate(submittedObject, passwords, null, override),
                createContent(submittedObject, passwords, null, override),
                Keyword.NONE,
                loggerContext);

        WhoisResources whoisResources = createWhoisResources(request, response, false);
        return Response.ok(whoisResources).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}")
    public Response restCreate(
            final WhoisResources resource,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(source);

        final RpslObject submittedObject = getSubmittedObject(resource);

        final RpslObject response = updatePerformer.performUpdate(
                createOrigin(request),
                updatePerformer.createUpdate(submittedObject, passwords, null, override),
                createContent(submittedObject, passwords, null, override),
                Keyword.NEW,
                loggerContext);

        WhoisResources whoisResources = createWhoisResources(request, response, false);
        return Response.ok(whoisResources).build();
    }

    private void checkForMainSource(String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new IllegalArgumentException("Invalid source: " + source);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Response restGet(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        checkForInvalidSource(source);

        final boolean unfiltered = Iterables.contains(getQueryParamNames(request.getQueryString()), "unfiltered");

        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s %s %s",
                QueryFlag.EXACT.getLongFlag(),
                QueryFlag.NO_GROUPING.getLongFlag(),
                QueryFlag.NO_REFERENCED.getLongFlag(),
                QueryFlag.SOURCES.getLongFlag(),
                source,
                QueryFlag.SELECT_TYPES.getLongFlag(),
                ObjectType.getByName(objectType).getName(),
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                unfiltered ? QueryFlag.NO_FILTERING.getLongFlag() : "",
                key));

        return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), null, null);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response restVersions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        checkForMainSource(source);

        final Query query = Query.parse(String.format("%s %s %s %s",
                QueryFlag.SELECT_TYPES.getLongFlag(),
                ObjectType.getByName(objectType).getName(),
                QueryFlag.LIST_VERSIONS.getLongFlag(),
                key));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final List<DeletedVersionResponseObject> deleted = versionsResponseHandler.getDeletedObjects();
        final List<VersionResponseObject> versions = versionsResponseHandler.getVersionObjects();

        if (versions.isEmpty() && deleted.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, whoisObjectMapper.mapVersions(deleted, versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions/{version}")
    public Response restVersion(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("version") final Integer version) {

        checkForMainSource(source);

        final Query query = Query.parse(String.format("%s %s %s %s %s",
                QueryFlag.SELECT_TYPES.getLongFlag(),
                ObjectType.getByName(objectType).getName(),
                QueryFlag.SHOW_VERSION.getLongFlag(),
                version,
                key));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final VersionWithRpslResponseObject versionWithRpslResponseObject = versionsResponseHandler.getVersionWithRpslResponseObject();

        if (versionWithRpslResponseObject == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject whoisObject = whoisObjectMapper.map(versionWithRpslResponseObject.getRpslObject());
        whoisObject.setVersion(versionWithRpslResponseObject.getVersion());
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    private Response handleQueryAndStreamResponse(final Query query, final HttpServletRequest request,
                                                  final InetAddress remoteAddress, @Nullable final Parameters parameters,
                                                  @Nullable final Service service) {

        final StreamingMarshal streamingMarshal = getStreamingMarshal(request);

        return Response.ok(new StreamingOutput() {
            private boolean rpslObjectFound;

            @Override
            public void write(final OutputStream output) throws IOException {

                try {
                    // TODO [AK] Crude way to handle tags, but working
                    final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
                    final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();

                    try {
                        final int contextId = System.identityHashCode(Thread.currentThread());
                        queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                            @Override
                            public void handle(final ResponseObject responseObject) {
                                if (responseObject instanceof TagResponseObject) {
                                    tagResponseObjects.add((TagResponseObject) responseObject);
                                } else if (responseObject instanceof RpslObject) {
                                    if (!rpslObjectFound) {
                                        startStreaming(output);
                                    }
                                    rpslObjectFound = true;
                                    streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                                    rpslObjectQueue.add((RpslObject) responseObject);
                                }

                                // TODO [AK] Handle related messages
                            }
                        });

                        streamObject(rpslObjectQueue.poll(), tagResponseObjects);

                        if (!rpslObjectFound) {
                            throw new WebApplicationException(Response.Status.NOT_FOUND);
                        }
                    } catch (QueryException e) {
                        if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                            throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
                        } else {
                            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
                        }
                    }

                    streamingMarshal.end();
                    streamingMarshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
                    streamingMarshal.close();
                } catch (StreamingException ignored) {  // only happens on IOException
                }
            }

            private void startStreaming(final OutputStream output) {
                streamingMarshal.open(output, "whois-resources");

                if (service != null) {
                    streamingMarshal.write("service", service);
                }

                if (parameters != null) {
                    streamingMarshal.write("parameters", parameters);
                }

                streamingMarshal.start("objects");
            }

            private void streamObject(@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
                if (rpslObject == null) {
                    return;
                }

                final WhoisObject whoisObject = whoisObjectMapper.map(rpslObject, tagResponseObjects);

                if (streamingMarshal instanceof StreamingMarshalJson) {
                    streamingMarshal.write("object", Collections.singletonList(whoisObject));
                } else {
                    streamingMarshal.write("object", whoisObject);
                }

                tagResponseObjects.clear();
            }
        }).build();
    }

    private StreamingMarshal getStreamingMarshal(final HttpServletRequest request) {
        final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        for (final String accept : Splitter.on(',').split(acceptHeader)) {
            try {
                final MediaType mediaType = MediaType.valueOf(accept);
                final String subtype = mediaType.getSubtype().toLowerCase();
                if (subtype.equals("json") || subtype.endsWith("+json")) {
                    return new StreamingMarshalJson();
                } else if (subtype.equals("xml") || subtype.endsWith("+xml")) {
                    return new StreamingMarshalXml();
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new StreamingMarshalXml();
    }

    private Iterable<String> getQueryParamNames(final String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return Collections.emptyList();
        }

        return Iterables.transform(AMPERSAND.split(queryString), new Function<String, String>() {
            @Override
            public String apply(final String input) {
                String result = input.toLowerCase();
                if (result.contains("=")) {
                    return result.substring(0, result.indexOf('='));
                }
                return result;
            }
        });
    }

    /**
     * The search interface resembles a standard Whois client query with the extra features of multi-registry client, multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.
     *
     * @param sources           Mandatory. It's possible to specify multiple sources.
     * @param searchKey       Mandatory.
     * @param inverseAttributes If specified the query is an inverse lookup on the given attribute, if not specified the query is a direct lookup search.
     * @param includeTags       Only show RPSL objects with given tags. Can be multiple.
     * @param excludeTags       Only show RPSL objects that <i>do not</i> have given tags. Can be multiple.
     * @param types             If specified the results will be filtered by object-type, multiple type-filters can be specified.
     * @param flags             Optional query-flags. Use separate flags parameters for each option (see examples above)
     * @return Returns the query result.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/search")
    public Response search(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String searchKey,   // ouch, but it's too costly to change the API just for this
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("include-tag") Set<String> includeTags,
            @QueryParam("exclude-tag") Set<String> excludeTags,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") Set<String> flags) {

        Set<String> validSources;
        if (CollectionUtils.isEmpty(sources)) {
            validSources = Collections.singleton(sourceContext.getCurrentSource().getName().toString());
        } else {
            validSources = sources;
            checkForInvalidSources(validSources);
        }

        final Set<String> separateFlags = splitInputFlags(flags);
        checkForInvalidFlags(separateFlags);

        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s",
                QueryFlag.SOURCES.getLongFlag(),
                JOINER.join(validSources),
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                CollectionUtils.isEmpty(types) ? "" : QueryFlag.SELECT_TYPES.getLongFlag(),
                JOINER.join(types),
                CollectionUtils.isEmpty(inverseAttributes) ? "" : QueryFlag.INVERSE.getLongFlag(),
                JOINER.join(inverseAttributes),
                CollectionUtils.isEmpty(includeTags) ? "" : QueryFlag.FILTER_TAG_INCLUDE.getLongFlag(),
                JOINER.join(includeTags),
                CollectionUtils.isEmpty(excludeTags) ? "" : QueryFlag.FILTER_TAG_EXCLUDE.getLongFlag(),
                JOINER.join(excludeTags),
                Joiner.on(" ").join(Iterables.transform(separateFlags, new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input.length() > 1 ? "--" + input : "-" + input;
                    }
                })),
                (searchKey == null ? "" : searchKey)));

        final Parameters parameters = new Parameters();
        parameters.setSources(validSources);
        parameters.setQueryStrings(searchKey);
        parameters.setInverseLookup(inverseAttributes);
        parameters.setTypeFilters(types);
        parameters.setFlags(separateFlags);

        Service service = new Service(SERVICE_SEARCH);

        return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), parameters, service);
    }

    private void checkForInvalidSources(final Set<String> sources) {
        for (final String source : sources) {
            checkForInvalidSource(source);
        }
    }

    private void checkForInvalidSource(final String source) {
        if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
            throw new IllegalArgumentException(String.format("Invalid source '%s'", source));
        }
    }

    private Set<String> splitInputFlags(final Set<String> inputFlags) {
        final Set<String> separateFlags = Sets.newLinkedHashSet();  // reporting errors should happen in the same order
        for (final String flagParameter : inputFlags) {
            if (QueryFlag.getValidLongFlags().contains(flagParameter)) {
                separateFlags.add(flagParameter);
            } else {
                final CharacterIterator charIterator = new StringCharacterIterator(flagParameter);
                for (char flag = charIterator.first(); flag != CharacterIterator.DONE; flag = charIterator.next()) {
                    final String flagString = String.valueOf(flag);
                    if (!QueryFlag.getValidShortFlags().contains(flagString)) {
                        throw new IllegalArgumentException(String.format("Invalid option '%s'", flag));
                    }
                    separateFlags.add(flagString);
                }
            }
        }
        return separateFlags;
    }

    private void checkForInvalidFlags(final Set<String> flags) {
        for (final String flag : flags) {
            if (NOT_ALLOWED_SEARCH_QUERY_FLAGS.contains(flag)) {
                throw new IllegalArgumentException(String.format("Disallowed option '%s'", flag));
            }
        }
    }

    private String createContent(final RpslObject rpslObject, final List<String> passwords, final String deleteReason, String override) {
        final StringBuilder builder = new StringBuilder();
        builder.append(rpslObject.toString());

        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }

        if (deleteReason != null) {
            builder.append("delete: ");
            builder.append(deleteReason);
            builder.append("\n\n");
        }

        for (final String password : passwords) {
            builder.append("password: ");
            builder.append(password);
            builder.append('\n');
        }

        if (override != null) {
            builder.append("override: ");
            builder.append(override);
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private Origin createOrigin(final HttpServletRequest request) {
        return new WhoisRestApi(dateTimeProvider, request.getRemoteAddr());
    }

    private WhoisResources createWhoisResources(final HttpServletRequest request, final RpslObject rpslObject, boolean filter) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObjectMapper.map(rpslObject, filter)));
        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    private RpslObject getSubmittedObject(final WhoisResources whoisResources) {
        if (whoisResources.getWhoisObjects().isEmpty() || whoisResources.getWhoisObjects().size() > 1) {
            throw new IllegalArgumentException("Expected a single RPSL object");
        }
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    private void validateSubmittedObject(final RpslObject object, final String objectType, final String key) {
        if (!(object.getKey().equals(CIString.ciString(key)) &&
                object.getType().getName().equalsIgnoreCase(objectType))) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    private class VersionsResponseHandler extends ApiResponseHandler {
        final List<VersionResponseObject> versionObjects = Lists.newArrayList();
        final List<DeletedVersionResponseObject> deletedObjects = Lists.newArrayList();
        VersionWithRpslResponseObject versionWithRpslResponseObject;

        public List<VersionResponseObject> getVersionObjects() {
            return versionObjects;
        }

        public List<DeletedVersionResponseObject> getDeletedObjects() {
            return deletedObjects;
        }

        public VersionWithRpslResponseObject getVersionWithRpslResponseObject() {
            return versionWithRpslResponseObject;
        }

        @Override
        public void handle(final ResponseObject responseObject) {
            if (responseObject instanceof VersionWithRpslResponseObject) {
                versionWithRpslResponseObject = (VersionWithRpslResponseObject) responseObject;
            }

            if (responseObject instanceof VersionResponseObject) {
                versionObjects.add((VersionResponseObject) responseObject);
            }

            if (responseObject instanceof DeletedVersionResponseObject) {
                deletedObjects.add((DeletedVersionResponseObject) responseObject);
            }
        }
    }

}
