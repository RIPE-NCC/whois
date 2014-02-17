package net.ripe.db.whois.api.rest;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
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
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryParser;
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
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
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
import java.util.Iterator;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRestService.class);

    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    public static final String SERVICE_SEARCH = "search";

    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();

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

    private final RpslObjectDao rpslObjectDao;
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;
    private final WhoisObjectServerMapper whoisObjectMapper;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;

    @Autowired
    public WhoisRestService(final RpslObjectDao rpslObjectDao,
                            final SourceContext sourceContext,
                            final QueryHandler queryHandler,
                            final WhoisObjectServerMapper whoisObjectMapper,
                            final InternalUpdatePerformer updatePerformer,
                            final SsoTranslator ssoTranslator) {
        this.rpslObjectDao = rpslObjectDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        this.whoisObjectMapper = whoisObjectMapper;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
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
            @CookieParam("crowd.token_key") final String crowdTokenKey,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        final Origin origin = updatePerformer.createOrigin(request);
        final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);
        try {
            // TODO: [AH] add delete by primary key to DAO layer, so there is no race condition from here to SingleUpdateHandler's global lock
            RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);
            originalObject = ssoTranslator.translateAuthToUsername(updateContext, originalObject);

            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, originalObject, passwords, reason, override),
                    updatePerformer.createContent(originalObject, passwords, reason, override),
                    Keyword.NONE,
                    request);
        } finally {
            updatePerformer.closeContext();
        }
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
            @CookieParam("crowd.token_key") final String crowdTokenKey,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        // TODO: [AH] getSubmittedObject() can throw exceptions on mapping
        final RpslObject submittedObject = getSubmittedObject(request, resource);
        validateSubmittedObject(request, submittedObject, objectType, key);

        final Origin origin = updatePerformer.createOrigin(request);
        final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);
        try {
            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override),
                    updatePerformer.createContent(submittedObject, passwords, null, override),
                    Keyword.NONE,
                    request);
        } finally {
            updatePerformer.closeContext();
        }
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
            @PathParam("objectType") final String objectType,               // TODO: [ES] validate object type (REST paradigm suggests specifying resource type on creation)
            @QueryParam("password") final List<String> passwords,
            @CookieParam("crowd.token_key") final String crowdTokenKey,
            @QueryParam("override") final String override) {

        checkForMainSource(request, source);

        // TODO: [AH] getSubmittedObject() can throw exceptions on mapping
        final RpslObject submittedObject = getSubmittedObject(request, resource);

        final Origin origin = updatePerformer.createOrigin(request);
        final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);
        try {
            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override),
                    updatePerformer.createContent(submittedObject, passwords, null, override),
                    Keyword.NEW,
                    request);
        } finally {
            updatePerformer.closeContext();
        }
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
            @QueryParam("password") final List<String> passwords,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {

        if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.invalidSource(source))).build());
        }

        final boolean unfiltered = isQueryParamSet(request.getQueryString(), "unfiltered");

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

        final Query query = Query.parse(queryBuilder.build(key), crowdTokenKey, passwords);

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
                key), Query.Origin.REST);

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
                key), Query.Origin.REST);

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

    private boolean isQueryParamSet(final String queryString, final String key) {
        if (queryString == null) {
            return false;

        }

        for (String next : AMPERSAND_SPLITTER.split(queryString)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext()) {
                // check if query parameter is present, and has no value, or value is true
                if (iterator.next().equals(key) &&
                        (!iterator.hasNext() || iterator.next().equals("true"))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * The search interface resembles a standard Whois client query with the extra features of multi-registry client,
     * multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.
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

        final Query query = Query.parse(queryBuilder.build(searchKey), Query.Origin.REST);

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

        try {
            if (QueryParser.hasFlags(searchKey)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString())).build());
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString())).build());
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

    private List<ErrorMessage> createErrorMessages(List<Message> messages) {
        List<ErrorMessage> errorMessages = Lists.newArrayList();
        for (Message message : messages) {
            errorMessages.add(new ErrorMessage(message));
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
        if (!object.getKey().equals(key) || !object.getType().getName().equalsIgnoreCase(objectType)) {
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
                if (message != null && Messages.Type.INFO != message.getType()) {
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

        return Response.ok(new RpslObjectStreamer(request, query, remoteAddress, parameters, service)).build();
    }

    private class RpslObjectStreamer implements StreamingOutput {
        private final HttpServletRequest request;
        private final Query query;
        private final InetAddress remoteAddress;
        private final Parameters parameters;
        private final Service service;
        private StreamingMarshal streamingMarshal;

        public RpslObjectStreamer(final HttpServletRequest request, final Query query, final InetAddress remoteAddress, final Parameters parameters, final Service service) {
            this.request = request;
            this.query = query;
            this.remoteAddress = remoteAddress;
            this.parameters = parameters;
            this.service = service;
        }

        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
            streamingMarshal = getStreamingMarshal(request, output);

            try {
                SearchResponseHandler responseHandler = new SearchResponseHandler();
                try {
                    final int contextId = System.identityHashCode(Thread.currentThread());
                    queryHandler.streamResults(query, remoteAddress, contextId, responseHandler);

                    if (!responseHandler.rpslObjectFound()) {
                        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(createErrorEntity(request, responseHandler.flushAndGetErrors())).build());
                    }
                    responseHandler.flushAndGetErrors();

                } catch (RuntimeException e) {
                    throw createWebApplicationException(e, responseHandler);
                }
            } catch (StreamingException ignored) {  // only happens on IOException
            }
        }

        private WebApplicationException createWebApplicationException(final RuntimeException exception, final SearchResponseHandler responseHandler) {
            if (exception instanceof WebApplicationException) {
                return (WebApplicationException) exception;
            } else if (exception instanceof QueryException) {
                final Response.ResponseBuilder responseBuilder;
                if (((QueryException) exception).getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                    responseBuilder = Response.status(STATUS_TOO_MANY_REQUESTS);
                } else {
                    responseBuilder = Response.status(Response.Status.BAD_REQUEST);
                }

                final List<Message> messages = responseHandler.flushAndGetErrors();
                messages.addAll(((QueryException) exception).getMessages());

                if (!messages.isEmpty()) {
                    responseBuilder.entity(createErrorEntity(request, messages));
                }

                return new WebApplicationException(responseBuilder.build());

            } else {

                final Response.ResponseBuilder responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                final List<Message> messages = responseHandler.flushAndGetErrors();
                messages.add(QueryMessages.internalErroroccurred());
                responseBuilder.entity(createErrorEntity(request, messages));

                return new WebApplicationException(responseBuilder.build());
            }
        }

        private class SearchResponseHandler extends ApiResponseHandler {
            private boolean rpslObjectFound;

            // tags come separately
            private final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
            private final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();
            private final List<Message> errors = Lists.newArrayList();

            // TODO: [AH] replace this 'if instanceof' mess with an OO approach
            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof TagResponseObject) {
                    tagResponseObjects.add((TagResponseObject) responseObject);
                } else if (responseObject instanceof RpslObject) {
                    streamRpslObject((RpslObject) responseObject);
                } else if (responseObject instanceof MessageObject) {
                    Message message = ((MessageObject) responseObject).getMessage();
                    if (message != null && Messages.Type.INFO != message.getType()) {
                        errors.add(message);
                    }
                }
            }

            private void streamRpslObject(final RpslObject rpslObject) {
                if (!rpslObjectFound) {
                    rpslObjectFound = true;
                    startStreaming();
                }
                streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                rpslObjectQueue.add(rpslObject);
            }

            private void startStreaming() {
                streamingMarshal.open();

                if (service != null) {
                    streamingMarshal.write("service", service);
                }

                if (parameters != null) {
                    streamingMarshal.write("parameters", parameters);
                }

                if (streamingMarshal instanceof StreamingMarshalJson) {
                    ((StreamingMarshalJson) streamingMarshal).startArray("objects");
                } else {
                    streamingMarshal.start("objects");
                }
            }

            private void streamObject(@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
                if (rpslObject == null) {
                    return;
                }

                final WhoisObject whoisObject = whoisObjectMapper.map(rpslObject, tagResponseObjects);

                // TODO: [AH] add method 'writeAsArray' or 'writeObject' to StreamingMarshal interface to get rid of this uglyness
                if (streamingMarshal instanceof StreamingMarshalJson) {
                    ((StreamingMarshalJson) streamingMarshal).writeArray(whoisObject);
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

                if (streamingMarshal instanceof StreamingMarshalJson) {
                    ((StreamingMarshalJson) streamingMarshal).endArray();
                }

                // TODO inside or outside the xml object?
                if (errors.size() > 0) {
                    streamingMarshal.write("errormessages", createErrorMessages(errors));
                    errors.clear();
                }

                // TODO: [AH] ugly; do we need this?
                if (streamingMarshal instanceof StreamingMarshalJson) {
                    streamingMarshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
                    streamingMarshal.end();
                } else {
                    streamingMarshal.end();
                    streamingMarshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
                }

                streamingMarshal.close();
                return errors;
            }
        }
    }

    private static final class QueryBuilder {
        private static final Joiner COMMA_JOINER = Joiner.on(',');
        private final StringBuilder query = new StringBuilder(128);

        public QueryBuilder addFlag(final QueryFlag queryFlag) {
            query.append(queryFlag.getLongFlag()).append(' ');
            return this;
        }

        public QueryBuilder addCommaList(final QueryFlag queryFlag, final String arg) {
            query.append(queryFlag.getLongFlag()).append(' ').append(arg).append(' ');
            return this;
        }

        public QueryBuilder addCommaList(final QueryFlag queryFlag, final Collection<String> args) {
            if (args.size() > 0) {
                query.append(queryFlag.getLongFlag()).append(' ');
                COMMA_JOINER.appendTo(query, args);
                query.append(' ');
            }
            return this;
        }

        public String build(final String searchKey) {
            return query.append(searchKey).toString();
        }
    }

    public static final StreamingMarshal getStreamingMarshal(final HttpServletRequest request, final OutputStream outputStream) {
        final String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader != null) {
            for (final String accept : Splitter.on(',').split(acceptHeader)) {
                try {
                    final MediaType mediaType = MediaType.valueOf(accept);
                    final String subtype = mediaType.getSubtype().toLowerCase();
                    if (subtype.equals("json") || subtype.endsWith("+json")) {
                        return new StreamingMarshalJson(outputStream);
                    } else if (subtype.equals("xml") || subtype.endsWith("+xml")) {
                        return new StreamingMarshalXml(outputStream, "whois-resources");
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        return new StreamingMarshalXml(outputStream, "whois-resources");
    }


}
