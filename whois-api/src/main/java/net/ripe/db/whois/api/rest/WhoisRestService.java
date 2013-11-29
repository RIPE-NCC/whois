package net.ripe.db.whois.api.rest;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.ErrorMessages;
import net.ripe.db.whois.api.rest.domain.Flags;
import net.ripe.db.whois.api.rest.domain.InverseAttributes;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.QueryString;
import net.ripe.db.whois.api.rest.domain.QueryStrings;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.Sources;
import net.ripe.db.whois.api.rest.domain.TypeFilters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.log.LoggerContext;
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
import java.util.Arrays;
import java.util.Collection;
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

    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&');

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

    private final LoggerContext loggerContext;
    private final RpslObjectDao rpslObjectDao;
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectMapper;
    private final InternalUpdatePerformer updatePerformer;

    @Autowired
    public WhoisRestService(final LoggerContext loggerContext,
                            final RpslObjectDao rpslObjectDao,
                            final SourceContext sourceContext,
                            final QueryHandler queryHandler,
                            final WhoisObjectServerMapper whoisObjectMapper,
                            final InternalUpdatePerformer updatePerformer) {
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
    public Response delete(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam("reason") @DefaultValue("--") final String reason,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        final RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);

        return updatePerformer.performUpdate(
                updatePerformer.createOrigin(request),
                updatePerformer.createUpdate(originalObject, passwords, reason, override),
                updatePerformer.createContent(originalObject, passwords, reason, override),
                Keyword.NONE,
                loggerContext,
                request);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Response update(
            final WhoisResources resource,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        final RpslObject submittedObject = getSubmittedObject(request, resource);
        validateSubmittedObject(request, submittedObject, objectType, key);

        return updatePerformer.performUpdate(
                updatePerformer.createOrigin(request),
                updatePerformer.createUpdate(submittedObject, passwords, null, override),
                updatePerformer.createContent(submittedObject, passwords, null, override),
                Keyword.NONE,
                loggerContext,
                request);

    }

    // TODO: deprecate mod_proxy for 'POST /ripe' and add check for objectType == submitted object type here
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}")
    public Response create(
            final WhoisResources resource,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @QueryParam("password") final List<String> passwords,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        final RpslObject submittedObject = getSubmittedObject(request, resource);

        return updatePerformer.performUpdate(
                updatePerformer.createOrigin(request),
                updatePerformer.createUpdate(submittedObject, passwords, null, override),
                updatePerformer.createContent(submittedObject, passwords, null, override),
                Keyword.NEW,
                loggerContext,
                request);
    }

    private void checkForMainSource(HttpServletRequest request, String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.invalidSource(source))).build());
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam("password") final List<String> passwords) {

        if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.invalidSource(source))).build());
        }

        final boolean unfiltered = Iterables.contains(getQueryParamNames(request.getQueryString()), "unfiltered");

        QueryBuilder queryBuilder = new QueryBuilder().
                addFlag(QueryFlag.EXACT).
                addFlag(QueryFlag.NO_GROUPING).
                addFlag(QueryFlag.NO_REFERENCED).
                addFlag(QueryFlag.SHOW_TAG_INFO).
                addCommaList(QueryFlag.SOURCES, source).
                addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName());

        if (unfiltered) {
            queryBuilder.addFlag(QueryFlag.NO_FILTERING);
        }

        final Query query = Query.parse(queryBuilder.build(key), passwords);

        return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), null, null);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions")
    public Response versions(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {

        checkForMainSource(request, source);

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
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(createErrorEntity(request, versionsResponseHandler.getErrors())).build());
        }

        final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, whoisObjectMapper.mapVersions(deleted, versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}/versions/{version}")
    public Response version(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("version") final Integer version) {

        checkForMainSource(request, source);

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
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(createErrorEntity(request, versionsResponseHandler.getErrors())).build());
        }

        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject whoisObject = whoisObjectMapper.map(versionWithRpslResponseObject.getRpslObject());
        whoisObject.setVersion(versionWithRpslResponseObject.getVersion());
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.setErrorMessages(createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
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

        return Iterables.transform(AMPERSAND_SPLITTER.split(queryString), new Function<String, String>() {
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
     * The search interface resembles a standard Whois client query with the extra features of multi-registry client,
     * multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.
     *
     * @param sources           Mandatory. It's possible to specify multiple sources.
     * @param searchKey         Mandatory.
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

        validateSources(request, sources);
        validateSearchKey(request, searchKey);

        final Set<QueryFlag> separateFlags = splitInputFlags(request, flags);
        checkForInvalidFlags(request, separateFlags);

        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addFlag(QueryFlag.SHOW_TAG_INFO);
        queryBuilder.addCommaList(QueryFlag.SOURCES, sources);
        queryBuilder.addCommaList(QueryFlag.SELECT_TYPES, types);
        queryBuilder.addCommaList(QueryFlag.INVERSE, inverseAttributes);
        queryBuilder.addCommaList(QueryFlag.FILTER_TAG_INCLUDE, includeTags);
        queryBuilder.addCommaList(QueryFlag.FILTER_TAG_EXCLUDE, excludeTags);
        for (QueryFlag separateFlag : separateFlags) {
            queryBuilder.addFlag(separateFlag);
        }

        final Query query = Query.parse(queryBuilder.build(searchKey));

        final Parameters parameters = new Parameters(
                new InverseAttributes(inverseAttributes),
                new TypeFilters(types),
                new Flags(separateFlags),
                new QueryStrings(new QueryString(searchKey)),
                new Sources(sources),
                null);

        Service service = new Service(SERVICE_SEARCH);

        return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), parameters, service);
    }

    private void validateSearchKey(HttpServletRequest request, String searchKey) {
        if (StringUtils.isBlank(searchKey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.queryStringEmpty())).build());
        }
        for (String word : SPACE_SPLITTER.split(searchKey)) {
            // FIXME: this marks search key '10.0.0.0 -10.1.1.1' as invalid, even though it's not!
            if (word.startsWith("-")) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString(word))).build());
            }
        }
    }

    private void validateSources(HttpServletRequest request, final Set<String> sources) {
        for (final String source : sources) {
            if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.invalidSource(source))).build());
            }
        }
    }

    private Set<QueryFlag> splitInputFlags(HttpServletRequest request, final Set<String> inputFlags) {
        final Set<QueryFlag> separateFlags = Sets.newLinkedHashSet();  // reporting errors should happen in the same order
        for (final String flagParameter : inputFlags) {
            QueryFlag forLongFlag = QueryFlag.getForLongFlag(flagParameter);
            if (forLongFlag != null) {
                separateFlags.add(forLongFlag);
            } else {
                final CharacterIterator charIterator = new StringCharacterIterator(flagParameter);
                for (char flag = charIterator.first(); flag != CharacterIterator.DONE; flag = charIterator.next()) {
                    final String flagString = String.valueOf(flag);
                    QueryFlag forShortFlag = QueryFlag.getForShortFlag(flagString);
                    if (forShortFlag != null) {
                        separateFlags.add(forShortFlag);
                    } else {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.invalidSearchFlag(flagParameter, flagString))).build());
                    }
                }
            }
        }
        return separateFlags;
    }

    private void checkForInvalidFlags(HttpServletRequest request, final Set<QueryFlag> flags) {
        for (final QueryFlag flag : flags) {
            if (NOT_ALLOWED_SEARCH_QUERY_FLAGS.contains(flag)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.disallowedSeachFlag(flag))).build());
            }
        }
    }

    private ErrorMessages createErrorMessages(List<Message> messages) {
        ErrorMessages errorMessages = new ErrorMessages();
        for (Message message : messages) {
            errorMessages.addErrorMessage(new ErrorMessage(message));
        }
        return errorMessages;
    }

    private WhoisResources createErrorEntity(final HttpServletRequest request, Message... errorMessage) {
        return createErrorEntity(request, Arrays.asList(errorMessage));
    }

    private WhoisResources createErrorEntity(final HttpServletRequest request, List<Message> errorMessages) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setErrorMessages(createErrorMessages(errorMessages));
        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request).replaceFirst("/whois", "")));
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

    private RpslObject getSubmittedObject(HttpServletRequest request, final WhoisResources whoisResources) {
        if (whoisResources.getWhoisObjects().isEmpty() || whoisResources.getWhoisObjects().size() > 1) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.singleObjectExpected(whoisResources.getWhoisObjects().size()))).build());
        }
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    private void validateSubmittedObject(HttpServletRequest request, final RpslObject object, final String objectType, final String key) {
        if (!object.getKey().equals(CIString.ciString(key)) || !object.getType().getName().equalsIgnoreCase(objectType)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.uriMismatch(objectType, key))).build());
        }
    }

    private class VersionsResponseHandler extends ApiResponseHandler {
        final List<VersionResponseObject> versionObjects = Lists.newArrayList();
        final List<DeletedVersionResponseObject> deletedObjects = Lists.newArrayList();
        VersionWithRpslResponseObject versionWithRpslResponseObject;
        private final List<Message> errors = Lists.newArrayList();

        public List<VersionResponseObject> getVersionObjects() {
            return versionObjects;
        }

        public List<DeletedVersionResponseObject> getDeletedObjects() {
            return deletedObjects;
        }

        public VersionWithRpslResponseObject getVersionWithRpslResponseObject() {
            return versionWithRpslResponseObject;
        }

        public List<Message> getErrors() {
            return errors;
        }

        @Override
        public void handle(final ResponseObject responseObject) {
            if (responseObject instanceof VersionWithRpslResponseObject) {
                versionWithRpslResponseObject = (VersionWithRpslResponseObject) responseObject;
            } else if (responseObject instanceof VersionResponseObject) {
                versionObjects.add((VersionResponseObject) responseObject);
            } else if (responseObject instanceof DeletedVersionResponseObject) {
                deletedObjects.add((DeletedVersionResponseObject) responseObject);
            } else if (responseObject instanceof MessageObject) {
                Message message = ((MessageObject) responseObject).getMessage();
                if (message != null && Messages.Type.INFO.compareTo(message.getType()) < 0) {
                    errors.add(message);
                }
            }
        }
    }

    private Response handleQueryAndStreamResponse(final Query query,
                                                  final HttpServletRequest request,
                                                  final InetAddress remoteAddress,
                                                  @Nullable final Parameters parameters,
                                                  @Nullable final Service service) {

        final StreamingMarshal streamingMarshal = getStreamingMarshal(request);

        return Response.ok(new RpslObjectStreamer(request, query, remoteAddress, parameters, service, streamingMarshal)).build();
    }

    private class RpslObjectStreamer implements StreamingOutput {
        private final HttpServletRequest request;
        private final Query query;
        private final InetAddress remoteAddress;
        private final Parameters parameters;
        private final Service service;
        private final StreamingMarshal streamingMarshal;

        public RpslObjectStreamer(HttpServletRequest request, Query query, InetAddress remoteAddress, Parameters parameters, Service service, StreamingMarshal streamingMarshal) {
            this.request = request;
            this.query = query;
            this.remoteAddress = remoteAddress;
            this.parameters = parameters;
            this.service = service;
            this.streamingMarshal = streamingMarshal;
        }

        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
            try {
                SearchResponseHandler responseHandler = new SearchResponseHandler(output);
                try {
                    final int contextId = System.identityHashCode(Thread.currentThread());
                    queryHandler.streamResults(query, remoteAddress, contextId, responseHandler);

                    if (!responseHandler.rpslObjectFound()) {
                        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(createErrorEntity(request, responseHandler.flushAndGetErrors())).build());
                    }

                    responseHandler.flushAndGetErrors();

                } catch (QueryException e) {
                    Response.ResponseBuilder responseBuilder;
                    if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                        responseBuilder = Response.status(STATUS_TOO_MANY_REQUESTS);
                    } else {
                        responseBuilder = Response.status(Response.Status.BAD_REQUEST);
                    }

                    List<Message> messages = responseHandler.flushAndGetErrors();
                    messages.addAll(e.getMessages());

                    if (!messages.isEmpty()) {
                        responseBuilder.entity(createErrorEntity(request, messages));
                    }

                    throw new WebApplicationException(responseBuilder.build());
                }

            } catch (StreamingException ignored) {  // only happens on IOException
            }
        }

        private class SearchResponseHandler extends ApiResponseHandler {
            private final OutputStream output;

            private boolean rpslObjectFound;

            // tags come separately
            private final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
            private final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();
            private final List<Message> errors = Lists.newArrayList();

            public SearchResponseHandler(OutputStream output) {
                this.output = output;
            }

            // TODO: [AH] replace this 'if instanceof' mess with an OO approach
            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof TagResponseObject) {
                    tagResponseObjects.add((TagResponseObject) responseObject);
                } else if (responseObject instanceof RpslObject) {
                    streamRpslObject((RpslObject) responseObject);
                } else if (responseObject instanceof MessageObject) {
                    Message message = ((MessageObject) responseObject).getMessage();
                    if (message != null && Messages.Type.INFO.compareTo(message.getType()) < 0) {
                        errors.add(message);
                    }
                }
            }

            private void streamRpslObject(final RpslObject rpslObject) {
                if (!rpslObjectFound) {
                    rpslObjectFound = true;
                    startStreaming(output);
                }
                streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                rpslObjectQueue.add(rpslObject);
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

            public boolean rpslObjectFound() {
                return rpslObjectFound;
            }

            public List<Message> flushAndGetErrors() {
                if (!rpslObjectFound) {
                    return errors;
                }
                streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                streamingMarshal.end();
                if (errors.size() > 0) {
                    streamingMarshal.write("errormessages", createErrorMessages(errors));
                    errors.clear();
                }
                streamingMarshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
                streamingMarshal.close();
                return errors;
            }
        }
    }

    private static final class QueryBuilder {
        private static final Joiner COMMA_JOINER = Joiner.on(',');
        private final StringBuilder query = new StringBuilder(128);

        public QueryBuilder addFlag(QueryFlag queryFlag) {
            query.append(queryFlag.getLongFlag()).append(' ');
            return this;
        }

        public QueryBuilder addCommaList(QueryFlag queryFlag, String arg) {
            query.append(queryFlag.getLongFlag()).append(' ').append(arg).append(' ');
            return this;
        }

        public QueryBuilder addCommaList(QueryFlag queryFlag, Collection<String> args) {
            if (args.size() > 0) {
                query.append(queryFlag.getLongFlag()).append(' ');
                COMMA_JOINER.appendTo(query, args);
                query.append(' ');
            }
            return this;
        }

        public String build(String searchKey) {
            return query.append(searchKey).toString();
        }
    }
}
