package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
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
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.getServerAttributeMapper;
import static net.ripe.db.whois.api.rest.RestServiceHelper.isQueryParamSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
@Path("/")
public class WhoisRestService {

    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectStreamer rpslObjectStreamer;
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;
    private final AccessControlListManager accessControlListManager;
    private final WhoisObjectMapper whoisObjectMapper;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    private final LoggerContext loggerContext;

    @Autowired
    public WhoisRestService(final RpslObjectDao rpslObjectDao,
                            final RpslObjectStreamer rpslObjectStreamer,
                            final SourceContext sourceContext,
                            final QueryHandler queryHandler,
                            final AccessControlListManager accessControlListManager,
                            final WhoisObjectMapper whoisObjectMapper,
                            final WhoisObjectServerMapper whoisObjectServerMapper,
                            final InternalUpdatePerformer updatePerformer,
                            final SsoTranslator ssoTranslator,
                            final LoggerContext loggerContext) {
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectStreamer = rpslObjectStreamer;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        this.accessControlListManager = accessControlListManager;
        this.whoisObjectMapper = whoisObjectMapper;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
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
            @QueryParam("dry-run") final String dryRun) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            setDryRun(updateContext, dryRun);

            RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);

            ssoTranslator.populateCacheAuthToUsername(updateContext, originalObject);
            originalObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, originalObject);

            final Update update = updatePerformer.createUpdate(updateContext, originalObject, passwords, reason, override);

            return updatePerformer.createResponse(
                    updateContext,
                    updatePerformer.performUpdate(
                            updateContext,
                            origin,
                            update,
                            Keyword.NONE,
                            request),
                    update,
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
            @QueryParam("dry-run") final String dryRun,
            @QueryParam("unformatted") final String unformatted) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            setDryRun(updateContext, dryRun);

            final RpslObject submittedObject = getSubmittedObject(request, resource, isQueryParamSet(unformatted));
            validateSubmittedUpdateObject(request, submittedObject, objectType, key);

            final Update update = updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override);

            return updatePerformer.createResponse(
                    updateContext,
                    updatePerformer.performUpdate(
                            updateContext,
                            origin,
                            update,
                            Keyword.NONE,
                            request),
                    update,
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
            @QueryParam("dry-run") final String dryRun,
            @QueryParam("unformatted") final String unformatted) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            checkForMainSource(request, source);
            setDryRun(updateContext, dryRun);

            final RpslObject submittedObject = getSubmittedObject(request, resource, isQueryParamSet(unformatted));
            validateSubmittedCreateObject(request, submittedObject, objectType);

            final Update update = updatePerformer.createUpdate(updateContext, submittedObject, passwords, null, override);

            return updatePerformer.createResponse(
                updateContext,
                updatePerformer.performUpdate(
                        updateContext,
                        origin,
                        update,
                        Keyword.NEW,
                        request),
                update,
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
            @CookieParam("crowd.token_key") final String crowdTokenKey,
            @QueryParam("unformatted") final String unformatted,
            @QueryParam("unfiltered") final String unfiltered) {

        if (!isValidSource(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }

        final QueryBuilder queryBuilder = new QueryBuilder().
                addFlag(QueryFlag.EXACT).
                addFlag(QueryFlag.NO_GROUPING).
                addFlag(QueryFlag.NO_REFERENCED).
                addFlag(QueryFlag.SHOW_TAG_INFO).
                addCommaList(QueryFlag.SOURCES, source).
                addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName());

        if (isQueryParamSet(unfiltered)) {
            queryBuilder.addFlag(QueryFlag.NO_FILTERING);
        }

        try {
            final Query query = Query.parse(queryBuilder.build(key), crowdTokenKey, passwords, isTrusted(request)).setMatchPrimaryKeyOnly(true);
            return rpslObjectStreamer.handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), null, null, isQueryParamSet(unformatted));
        } catch (QueryException e) {
            throw RestServiceHelper.createWebApplicationException(e, request);
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

        final QueryBuilder queryBuilder = new QueryBuilder()
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
                    .entity(RestServiceHelper.createErrorEntity(request, versionsResponseHandler.getErrors()))
                    .build());
        }

        final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, whoisObjectServerMapper.mapVersions(deleted, versions));

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(RestServiceHelper.createErrorMessages(versionsResponseHandler.getErrors()));
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

        final QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addCommaList(QueryFlag.SHOW_VERSION, String.valueOf(version));

        final Query query = Query.parse(queryBuilder.build(key), Query.Origin.REST, isTrusted(request));

        final VersionsResponseHandler versionsResponseHandler = new VersionsResponseHandler();
        final int contextId = System.identityHashCode(Thread.currentThread());
        queryHandler.streamResults(query, InetAddresses.forString(request.getRemoteAddr()), contextId, versionsResponseHandler);

        final VersionWithRpslResponseObject versionWithRpslResponseObject = versionsResponseHandler.getVersionWithRpslResponseObject();

        if (versionWithRpslResponseObject == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(RestServiceHelper.createErrorEntity(request, versionsResponseHandler.getErrors()))
                    .build());
        }

        // TODO: [AH] this should use StreamingMarshal to properly handle newlines in errormessages
        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject whoisObject = whoisObjectMapper.map(versionWithRpslResponseObject.getRpslObject(), FormattedServerAttributeMapper.class);
        whoisObject.setVersion(versionWithRpslResponseObject.getVersion());
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.setErrorMessages(RestServiceHelper.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();

        return Response.ok(whoisResources).build();
    }

    private boolean isTrusted(final HttpServletRequest request) {
        return accessControlListManager.isTrusted(InetAddresses.forString(request.getRemoteAddr()));
    }

    private RpslObject getSubmittedObject(final HttpServletRequest request, final WhoisResources whoisResources, final boolean unformatted) {
        final int size = (whoisResources == null || CollectionUtils.isEmpty(whoisResources.getWhoisObjects())) ? 0 : whoisResources.getWhoisObjects().size();
        if (size != 1) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.singleObjectExpected(size)))
                    .build());
        }

        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0), getServerAttributeMapper(unformatted));
    }

    private void validateSubmittedUpdateObject(final HttpServletRequest request, final RpslObject object, final String objectType, final String key) {
        if (!object.getType().getName().equalsIgnoreCase(objectType)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.uriMismatch(objectType, key)))
                    .build());
        }

        if (!object.getKey().equals(key)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.pkeyMismatch(key)))
                    .build());
        }
    }

    private void validateSubmittedCreateObject(final HttpServletRequest request, final RpslObject object, final String objectType) {
        if (!object.getType().getName().equalsIgnoreCase(objectType)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.uriMismatch(objectType)))
                    .build());
        }
    }

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
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

    private void checkForMainSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }
    }

    private boolean isValidSource(final String source) {
        return sourceContext.getAllSourceNames().contains(ciString(source));
    }

    void setDryRun(final UpdateContext updateContext, final String dryRun) {
        if (isQueryParamSet(dryRun)) {
            updateContext.dryRun();
        }
    }
}
