package net.ripe.db.whois.api.rest;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import java.net.URI;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.getServerAttributeMapper;
import static net.ripe.db.whois.api.rest.RestServiceHelper.isQueryParamSet;
import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE;
import static net.ripe.db.whois.common.rpsl.ObjectType.ROUTE6;

@Component
@Path("/")
public class WhoisRestService {

    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectStreamer rpslObjectStreamer;
    private final SourceContext sourceContext;
    private final AccessControlListManager accessControlListManager;
    private final WhoisObjectMapper whoisObjectMapper;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    private final LoggerContext loggerContext;
    private final AuthoritativeResourceData authoritativeResourceData;

    @Autowired
    public WhoisRestService(final RpslObjectDao rpslObjectDao,
                            final RpslObjectStreamer rpslObjectStreamer,
                            final SourceContext sourceContext,
                            final AccessControlListManager accessControlListManager,
                            final WhoisObjectMapper whoisObjectMapper,
                            final InternalUpdatePerformer updatePerformer,
                            final SsoTranslator ssoTranslator,
                            final LoggerContext loggerContext,
                            final AuthoritativeResourceData authoritativeResourceData) {
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectStreamer = rpslObjectStreamer;
        this.sourceContext = sourceContext;
        this.accessControlListManager = accessControlListManager;
        this.whoisObjectMapper = whoisObjectMapper;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
        this.loggerContext = loggerContext;
        this.authoritativeResourceData = authoritativeResourceData;
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

            if(requiresNonauthRedirect(source, objectType, key)) {
                return redirect(request.getServletPath(), sourceContext.getNonauthSource().getName().toString(), objectType, key, request.getQueryString());
            }

            if(requiresRipeRedirect(source, objectType, key)) {
                return redirect(request.getServletPath(), sourceContext.getWhoisMasterSource().getName().toString(), objectType, key, request.getQueryString());
            }

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

            if(requiresNonauthRedirect(source, objectType, key)) {
                return redirect(request.getServletPath(), sourceContext.getNonauthSource().getName().toString(), objectType, key, request.getQueryString());
            }

            if(requiresRipeRedirect(source, objectType, key)) {
                return redirect(request.getServletPath(), sourceContext.getWhoisMasterSource().getName().toString(), objectType, key, request.getQueryString());
            }

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

    /**
     * (Read) Lookup a single Whois RPSL object
     *
     * @param request request context
     * @param source (Mandatory) source database to search
     * @param objectType (Mandatory) object type
     * @param key (Mandatory) object primary key
     * @param passwords password(s) for authentication
     * @param crowdTokenKey crowd token for authentication
     * @param unformatted return attribute values without formatting
     * @param unfiltered do not filter the object
     * @param managedAttributes annotate attributes which are managed by the RIPE NCC
     * @param resourceHolder annotate resource object(s) with the associated responsible organisation (if any)
     * @param abuseContact annotate resource and organisation object(s) with associated abuse contact (if any)
     * @return
     */
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
            @QueryParam("unfiltered") final String unfiltered,
            @QueryParam("managed-attributes") final String managedAttributes,
            @QueryParam("resource-holder") final String resourceHolder,
            @QueryParam("abuse-contact") final String abuseContact) {

        if (!isValidSource(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }

        if(requiresNonauthRedirect(source, objectType, key)) {
            return redirect(request.getServletPath(), sourceContext.getNonauthSource().getName().toString(), objectType, key, request.getQueryString());
        }

        if(requiresRipeRedirect(source, objectType, key)) {
            return redirect(request.getServletPath(), sourceContext.getWhoisMasterSource().getName().toString(), objectType, key, request.getQueryString());
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
            final Parameters parameters = new Parameters.Builder()
                                    .unformatted(isQueryParamSet(unformatted))
                                    .managedAttributes(isQueryParamSet(managedAttributes))
                                    .resourceHolder(isQueryParamSet(resourceHolder))
                                    .abuseContact(isQueryParamSet(abuseContact))
                                    .build();
            return rpslObjectStreamer.handleQueryAndStreamResponse(query, request, InetAddresses.forString(request.getRemoteAddr()), parameters, null);
        } catch (QueryException e) {
            throw RestServiceHelper.createWebApplicationException(e, request);
        }
    }

    private boolean requiresNonauthRedirect(final String source, final String objectType, final String key) {
        if (sourceContext.getWhoisMasterSource().getName().equals(source)) {
            switch (ObjectType.getByName(objectType)) {
                case AUT_NUM:
                    return !authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(AUT_NUM, ciString(key));
                case ROUTE:
                    return !authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(ROUTE, ciString(key));
                case ROUTE6:
                    return !authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(ROUTE6, ciString(key));
                default:
                    return false;
            }
        }

        return false;
    }

    private boolean requiresRipeRedirect(final String source, final String objectType, final String key) {
        if(sourceContext.getNonauthSource().getName().equals(source)) {
            switch (ObjectType.getByName(objectType)) {
                case AUT_NUM:
                    return authoritativeResourceData.getAuthoritativeResource().isMaintainedInRirSpace(AUT_NUM, ciString(key));
                case ROUTE:
                    return authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(ROUTE, ciString(key));
                case ROUTE6:
                    return authoritativeResourceData.getAuthoritativeResource().isRouteMaintainedInRirSpace(ROUTE6, ciString(key));
                default:
                    return false;
            }
        }

        return false;
    }

    private Response redirect(final String context, final String source, final String objectType, final String pkey, String queryString) {
        final URI uri = StringUtils.isBlank(queryString)?
            URI.create(String.format("%s/%s/%s/%s", context, source, objectType, pkey)) :
            URI.create(String.format("%s/%s/%s/%s", context, source, objectType, pkey) + "?" + queryString);

        return Response.status(Response.Status.MOVED_PERMANENTLY).location(uri).build();

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

    private void checkForMainSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            if(!sourceContext.getNonauthSource().getName().toString().equalsIgnoreCase(source)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
            }
        }
    }

    private boolean isValidSource(final String source) {
        return sourceContext.isOutOfRegion(source) || sourceContext.getAllSourceNames().contains(ciString(source));
    }

    void setDryRun(final UpdateContext updateContext, final String dryRun) {
        if (isQueryParamSet(dryRun)) {
            updateContext.dryRun();
        }
    }
}
