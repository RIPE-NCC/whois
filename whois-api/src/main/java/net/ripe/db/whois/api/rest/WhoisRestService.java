package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.client.StreamingException;
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
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
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
import net.ripe.db.whois.query.acl.AccessControlListManager;
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
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.apache.commons.collections.CollectionUtils;
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

import static net.ripe.db.whois.api.rest.RestServiceHelper.getServerAttributeMapper;
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
public class WhoisRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRestService.class);

    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    public static final String SERVICE_SEARCH = "search";

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
    private final AccessControlListManager accessControlListManager;
    private final WhoisObjectMapper whoisObjectMapper;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    private final WhoisService whoisService;
    private final LoggerContext loggerContext;

    @Autowired
    public WhoisRestService(final RpslObjectDao rpslObjectDao,
                            final SourceContext sourceContext,
                            final QueryHandler queryHandler,
                            final AccessControlListManager accessControlListManager,
                            final WhoisObjectMapper whoisObjectMapper,
                            final WhoisObjectServerMapper whoisObjectServerMapper,
                            final InternalUpdatePerformer updatePerformer,
                            final SsoTranslator ssoTranslator,
                            final WhoisService whoisService,
                            final LoggerContext loggerContext) {
        this.rpslObjectDao = rpslObjectDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        this.accessControlListManager = accessControlListManager;
        this.whoisObjectMapper = whoisObjectMapper;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
        this.whoisService = whoisService;
        this.loggerContext = loggerContext;
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
            @QueryParam("override") final String override,
            @QueryParam("dryrun") final String dryRun) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            checkDryRun(updateContext, dryRun);

            RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);

            ssoTranslator.populateCacheAuthToUsername(updateContext, originalObject);
            originalObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, originalObject);

            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, originalObject, passwords, reason, override),
                    updatePerformer.createContent(originalObject, passwords, reason, override),
                    Keyword.NONE,
                    request);

        } catch (Exception e) {
            updatePerformer.logWarning(String.format("Caught %s for %s: %s", e.getClass().toString(), key, e.getMessage()));
            throw e;
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
            @QueryParam("override") final String override,
            @QueryParam("dryrun") final String dryRun) {

        final RpslObject submittedObject = getSubmittedObject(request, resource);
        validateSubmittedUpdateObject(request, submittedObject, objectType, key);

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            checkDryRun(updateContext, dryRun);

            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override),
                    updatePerformer.createContent(submittedObject, passwords, null, override),
                    Keyword.NONE,
                    request);
        } catch (Exception e) {
            updatePerformer.logWarning(String.format("Caught %s for %s: %s", e.getClass().toString(), key, e.getMessage()));
            throw e;
        } finally {
            updatePerformer.closeContext();
        }
    }

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
            @CookieParam("crowd.token_key") final String crowdTokenKey,
            @QueryParam("override") final String override,
            @QueryParam("dryrun") final String dryRun) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            checkDryRun(updateContext, dryRun);

            final RpslObject submittedObject = getSubmittedObject(request, resource);
            validateSubmittedCreateObject(request, submittedObject, objectType);

            return updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override),
                    updatePerformer.createContent(submittedObject, passwords, null, override),
                    Keyword.NEW,
                    request);
        } catch (Exception e) {
            updatePerformer.logWarning(String.format("Caught %s: %s", e.getClass().toString(), e.getMessage()));
            throw e;
        } finally {
            updatePerformer.closeContext();
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
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }

        QueryBuilder queryBuilder = new QueryBuilder().
                addFlag(QueryFlag.EXACT).
                addFlag(QueryFlag.NO_GROUPING).
                addFlag(QueryFlag.NO_REFERENCED).
                addFlag(QueryFlag.SHOW_TAG_INFO).
                addCommaList(QueryFlag.SOURCES, source).
                addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName());

        if (isQueryParamSet(request.getQueryString(), "unfiltered")) {
            queryBuilder.addFlag(QueryFlag.NO_FILTERING);
        }

        try {
            final Query query = Query.parse(queryBuilder.build(key), crowdTokenKey, passwords, isTrusted(request)).setMatchPrimaryKeyOnly(true);
            return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), null, null);
        } catch (QueryException e) {
            throw getWebApplicationException(e, request, Lists.<Message>newArrayList());
        }
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

        QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addFlag(QueryFlag.LIST_VERSIONS);

        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.REST, isTrusted(request));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final List<DeletedVersionResponseObject> deleted = versionsResponseHandler.getDeletedObjects();
        final List<VersionResponseObject> versions = versionsResponseHandler.getVersionObjects();

        if (versions.isEmpty() && deleted.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, versionsResponseHandler.getErrors()))
                    .build());
        }

        final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, whoisObjectServerMapper.mapVersions(deleted, versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionsResponseHandler.getErrors()));
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

        QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addCommaList(QueryFlag.SHOW_VERSION, String.valueOf(version));

        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.REST, isTrusted(request));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final VersionWithRpslResponseObject versionWithRpslResponseObject = versionsResponseHandler.getVersionWithRpslResponseObject();

        if (versionWithRpslResponseObject == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(whoisService.createErrorEntity(request, versionsResponseHandler.getErrors()))
                    .build());
        }

        // TODO: [AH] this should use StreamingMarshal to properly handle newlines in errormessages
        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject whoisObject = whoisObjectMapper.map(versionWithRpslResponseObject.getRpslObject(), FormattedServerAttributeMapper.class);
        whoisObject.setVersion(versionWithRpslResponseObject.getVersion());
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.setErrorMessages(whoisService.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    /**
     * The search interface resembles a standard Whois client query with the extra features of multi-registry client,
     * multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/search")
    public Response search(
            @Context final HttpServletRequest request,
            @QueryParam("source") final Set<String> sources,
            @QueryParam("query-string") final String searchKey,   // ouch, but it's too costly to change the API just for this
            @QueryParam("inverse-attribute") final Set<String> inverseAttributes,
            @QueryParam("include-tag") final Set<String> includeTags,
            @QueryParam("exclude-tag") final Set<String> excludeTags,
            @QueryParam("type-filter") final Set<String> types,
            @QueryParam("flags") final Set<String> flags) {

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

        final Parameters parameters = new Parameters(
                new InverseAttributes(inverseAttributes),
                new TypeFilters(types),
                new Flags(separateFlags),
                new QueryStrings(new QueryString(searchKey)),
                new Sources(sources),
                null);

        final Service service = new Service(SERVICE_SEARCH);

        return handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), parameters, service);
    }

    private void validateSearchKey(final HttpServletRequest request, final String searchKey) {
        if (StringUtils.isBlank(searchKey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.queryStringEmpty()))
                    .build());
        }

        try {
            if (QueryParser.hasFlags(searchKey)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(whoisService.createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString()))
                        .build());
            }
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.flagsNotAllowedInQueryString()))
                    .build());
        }
    }

    private void validateSources(final HttpServletRequest request, final Set<String> sources) {
        for (final String source : sources) {
            if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(whoisService.createErrorEntity(request, RestMessages.invalidSource(source)))
                        .build());
            }
        }
    }

    private boolean isTrusted(final HttpServletRequest request) {
        return accessControlListManager.isTrusted(InetAddresses.forString(request.getRemoteAddr()));
    }

    private Set<QueryFlag> splitInputFlags(final HttpServletRequest request, final Set<String> inputFlags) {
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
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity(whoisService.createErrorEntity(request, RestMessages.invalidSearchFlag(flagParameter, flagString)))
                                .build());
                    }
                }
            }
        }
        return separateFlags;
    }

    private void checkForInvalidFlags(final HttpServletRequest request, final Set<QueryFlag> flags) {
        for (final QueryFlag flag : flags) {
            if (NOT_ALLOWED_SEARCH_QUERY_FLAGS.contains(flag)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(whoisService.createErrorEntity(request, RestMessages.disallowedSearchFlag(flag)))
                        .build());
            }
        }
    }

    private RpslObject getSubmittedObject(final HttpServletRequest request, final WhoisResources whoisResources) {
        final int size = (whoisResources == null || CollectionUtils.isEmpty(whoisResources.getWhoisObjects())) ? 0 : whoisResources.getWhoisObjects().size();
        if (size != 1) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.singleObjectExpected(size)))
                    .build());
        }

        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0), getServerAttributeMapper(request.getQueryString()));
    }

    private void validateSubmittedUpdateObject(final HttpServletRequest request, final RpslObject object, final String objectType, final String key) {
        if (!object.getKey().equals(key) || !object.getType().getName().equalsIgnoreCase(objectType)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.uriMismatch(objectType, key)))
                    .build());
        }
    }

    private void validateSubmittedCreateObject(final HttpServletRequest request, final RpslObject object, final String objectType) {
        if (!object.getType().getName().equalsIgnoreCase(objectType)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.uriMismatch(objectType)))
                    .build());
        }
    }

    private void auditlogRequest(final HttpServletRequest request) {
        InternalUpdatePerformer.logHttpHeaders(loggerContext, request);
        InternalUpdatePerformer.logHttpUri(loggerContext, request);
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

    private WebApplicationException getWebApplicationException(final RuntimeException exception, final HttpServletRequest request, final List<Message> messages) {
        final Response.ResponseBuilder responseBuilder;

        if (exception instanceof QueryException) {
            final QueryException queryException = (QueryException) exception;
            if (queryException.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                responseBuilder = Response.status(STATUS_TOO_MANY_REQUESTS);
            } else {
                responseBuilder = Response.status(Response.Status.BAD_REQUEST);
            }
            messages.addAll(queryException.getMessages());

        } else {
            LOGGER.error(exception.getMessage(), exception);
            responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

            messages.add(QueryMessages.internalErroroccurred());
        }

        if (!messages.isEmpty()) {
            responseBuilder.entity(whoisService.createErrorEntity(request, messages));
        }
        return new WebApplicationException(responseBuilder.build());
    }

    private void checkForMainSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(whoisService.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }
    }

    void checkDryRun(final UpdateContext updateContext, final String dryRun) {
        if (dryRun == null || dryRun.equalsIgnoreCase("false")) {
            return;
        }
        updateContext.dryRun();
    }

    private class RpslObjectStreamer implements StreamingOutput {
        private final HttpServletRequest request;
        private final Query query;
        private final InetAddress remoteAddress;
        private final Parameters parameters;
        private final Service service;
        private StreamingMarshal streamingMarshal;
        private Class<? extends AttributeMapper> attributeMapper;

        public RpslObjectStreamer(final HttpServletRequest request, final Query query, final InetAddress remoteAddress, final Parameters parameters, final Service service) {
            this.request = request;
            this.query = query;
            this.remoteAddress = remoteAddress;
            this.parameters = parameters;
            this.service = service;
            this.attributeMapper = RestServiceHelper.getServerAttributeMapper(request.getQueryString());
        }

        @Override
        public void write(final OutputStream output) throws IOException, WebApplicationException {
            streamingMarshal = StreamingHelper.getStreamingMarshal(request, output);

            final SearchResponseHandler responseHandler = new SearchResponseHandler();
            try {
                final int contextId = System.identityHashCode(Thread.currentThread());
                queryHandler.streamResults(query, remoteAddress, contextId, responseHandler);

                if (!responseHandler.rpslObjectFound()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(whoisService.createErrorEntity(request, responseHandler.flushAndGetErrors()))
                            .build());
                }
                responseHandler.flushAndGetErrors();

            } catch (StreamingException ignored) {
            } catch (RuntimeException e) {
                throw createWebApplicationException(e, responseHandler);
            }
        }

        private WebApplicationException createWebApplicationException(final RuntimeException exception, final SearchResponseHandler responseHandler) {
            if (exception instanceof WebApplicationException) {
                return (WebApplicationException) exception;
            } else {
                final List<Message> messages = responseHandler.flushAndGetErrors();
                return getWebApplicationException(exception, request, messages);
            }
        }

        private class SearchResponseHandler extends ApiResponseHandler {
            private boolean rpslObjectFound;

            // tags come separately
            private final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
            private TagResponseObject tagResponseObject = null;
            private final List<Message> errors = Lists.newArrayList();

            // TODO: [AH] replace this 'if instanceof' mess with an OO approach
            @Override
            public void handle(final ResponseObject responseObject) {
                if (responseObject instanceof TagResponseObject) {
                    tagResponseObject = (TagResponseObject) responseObject;
                } else if (responseObject instanceof RpslObject) {
                    streamRpslObject((RpslObject) responseObject);
                } else if (responseObject instanceof MessageObject) {
                    final Message message = ((MessageObject) responseObject).getMessage();
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
                streamObject(rpslObjectQueue.poll());
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

                streamingMarshal.start("objects");
                streamingMarshal.startArray("object");
            }

            private void streamObject(@Nullable final RpslObject rpslObject) {
                if (rpslObject == null) {
                    return;
                }

                final WhoisObject whoisObject = whoisObjectServerMapper.map(rpslObject, tagResponseObject, attributeMapper);

                streamingMarshal.writeArray(whoisObject);
                tagResponseObject = null;
            }

            public boolean rpslObjectFound() {
                return rpslObjectFound;
            }

            public List<Message> flushAndGetErrors() {
                if (!rpslObjectFound) {
                    return errors;
                }
                streamObject(rpslObjectQueue.poll());

                streamingMarshal.endArray();

                streamingMarshal.end("objects");
                if (errors.size() > 0) {
                    streamingMarshal.write("errormessages", whoisService.createErrorMessages(errors));
                    errors.clear();
                }

                streamingMarshal.write("terms-and-conditions", new Link("locator", WhoisResources.TERMS_AND_CONDITIONS));
                streamingMarshal.end("whois-resources");
                streamingMarshal.close();
                return errors;
            }
        }
    }
}
