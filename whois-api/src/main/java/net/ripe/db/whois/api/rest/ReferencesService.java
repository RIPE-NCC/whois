package net.ripe.db.whois.api.rest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Path("/references")
public class ReferencesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferencesService.class);

    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final SourceContext sourceContext;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    private final WhoisService whoisService;
    private final LoggerContext loggerContext;
    private final WhoisObjectMapper whoisObjectMapper;
    private final String dummyRole;

    @Autowired
    public ReferencesService(
            final RpslObjectDao rpslObjectDao,
            final RpslObjectUpdateDao rpslObjectUpdateDao,
            final SourceContext sourceContext,
            final InternalUpdatePerformer updatePerformer,
            final SsoTranslator ssoTranslator,
            final WhoisService whoisService,
            final LoggerContext loggerContext,
            final WhoisObjectMapper whoisObjectMapper,
            @Value("${whois.dummy_role.nichdl}") final String dummyRole) {

        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.sourceContext = sourceContext;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
        this.whoisService = whoisService;
        this.loggerContext = loggerContext;
        this.whoisObjectMapper = whoisObjectMapper;
        this.dummyRole = dummyRole;
    }

    /**
     * Return all incoming references for a given object type and primary key
     *
     * @param sourceParam
     * @param objectTypeParam
     * @param keyParam
     */
    @GET
    @Path("/{source}/{objectType}/{key:.*}")
    public Response lookup(
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam) {

        return ok("ok");
    }

    /**
     * Delete an object, and also any incoming referencing objects (which must be a closed group).
     *
     * @param sourceParam
     * @param objectTypeParam
     * @param keyParam
     *
     */
    @DELETE
    @Path("/{source}/{objectType}/{key:.*}")
    public Response delete(
            @Context final HttpServletRequest request,
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam,
            @QueryParam("reason") @DefaultValue("--") final String reason,
            @QueryParam("password") final List<String> passwords,
            @CookieParam("crowd.token_key") final String crowdTokenKey) {

        checkForMainSource(request, sourceParam);

        final RpslObject primaryObject = rpslObjectDao.getByKey(ObjectType.getByName(objectTypeParam), keyParam);

        final Map<RpslObjectInfo, RpslObject> references = findReferences(primaryObject);

        validate(primaryObject, references);

        final Set<RpslObject> allObjects = Sets.newHashSet(primaryObject);
        allObjects.addAll(references.values());

        try {

            if (references.isEmpty()) {
                // delete the primary object directly. same as existing whois API
                performUpdate(request, primaryObject, reason, passwords, crowdTokenKey);

                return createResponse(request, primaryObject, Response.Status.OK);
            }

            // update the maintainer to point to a dummy person / role

            performUpdate(request, updateMaintainer(allObjects), null, passwords, crowdTokenKey);

            // delete the person / role objects

            for (final RpslObject rpslObject : allObjects) {
                if (!rpslObject.getType().equals(ObjectType.MNTNER)) {
                    performUpdate(request, rpslObject, reason, passwords, crowdTokenKey);
                }
            }

            // delete the maintainer

            performUpdate(request, updateMaintainer(allObjects), reason, passwords, crowdTokenKey);

            return createResponse(request, primaryObject, Response.Status.OK);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NotAuthorizedException(createResponse(request, primaryObject, Response.Status.UNAUTHORIZED));

                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    throw new InternalServerErrorException(createResponse(request, primaryObject, Response.Status.INTERNAL_SERVER_ERROR));

                default:
                    throw new BadRequestException(createResponse(request, primaryObject, Response.Status.BAD_REQUEST));
            }

        } catch (Exception e) {
            return createResponse(request, primaryObject, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Response performUpdate(
                final HttpServletRequest request,
                final RpslObject rpslObject,
                final String deleteReason,
                final List<String> passwords,
                final String crowdTokenKey) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);

            auditlogRequest(request);

            ssoTranslator.populateCacheAuthToUsername(updateContext, rpslObject);
            final RpslObject updatedRpslObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, rpslObject);

            final Update update = updatePerformer.createUpdate(updateContext, updatedRpslObject, passwords, deleteReason, null);

            final Response response = updatePerformer.performUpdate(
                    updateContext,
                    origin,
                    update,
                    updatePerformer.createContent(updatedRpslObject, passwords, deleteReason, null),
                    Keyword.NONE,
                    request);

            if (response.getStatus() != 200) {

                // TODO: add error message "error updating/deleting <specific object>"

                final WhoisResources whoisResources = ((InternalUpdatePerformer.StreamingResponse) response.getEntity()).getWhoisResources();
                for (ErrorMessage errorMessage : whoisResources.getErrorMessages()) {
                    LOGGER.warn("Error Message: {}", errorMessage.toString());
                }

                throw new WebApplicationException(response);
            }

            return response;

        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            updatePerformer.logWarning(String.format("Caught %s for %s: %s", e.getClass().toString(), rpslObject.getKey(), e.getMessage()));
            throw e;
        } finally {
            updatePerformer.closeContext();
        }
    }

    // helper methods

    private void validate(final RpslObject primaryObject, final Map<RpslObjectInfo, RpslObject> references) {

        // make sure that primary object, and all references, are of a valid type

        if (primaryObject.getType().equals(ObjectType.MNTNER)) {

            // references must be of person / role only

            for (Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {
                final RpslObjectInfo reference = entry.getKey();

                if (!reference.getObjectType().equals(ObjectType.PERSON) && !reference.getObjectType().equals(ObjectType.ROLE)) {
                    throw new IllegalArgumentException("Referencing object " + entry.getKey().getKey() + " type " + entry.getKey().getObjectType() + " is not supported.");
                }
            }

        } else {

            if (primaryObject.getType().equals(ObjectType.PERSON) || primaryObject.getType().equals(ObjectType.ROLE)) {

                // references must be of mntner only

                for (Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {
                    final RpslObjectInfo reference = entry.getKey();

                    if (!reference.getObjectType().equals(ObjectType.MNTNER)) {
                        throw new IllegalArgumentException("Referencing object " + entry.getKey().getKey() + " type " + entry.getKey().getObjectType() + " is not supported.");
                    }
                }
            } else {
                throw new IllegalArgumentException("Object type " + primaryObject.getType() + " is not supported.");
            }
        }

        // validate references - ensure a closed group

        for (Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {

            final RpslObject reference = entry.getValue();

            for (RpslObjectInfo referenceToReference : rpslObjectUpdateDao.getReferences(reference)) {

                if (!referenceMatches(referenceToReference, primaryObject) && !references.keySet().contains(referenceToReference)) {

                    throw new IllegalArgumentException("Referencing object " + reference.getKey()  + " itself is referenced by " + referenceToReference.getKey());
                }
            }
        }
    }

    private Response createResponse(final HttpServletRequest request, final RpslObject primaryObject, final Response.Status status) {
        final Response.ResponseBuilder responseBuilder = Response.status(status);

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setWhoisObjects(Lists.newArrayList(whoisObjectMapper.map(primaryObject, FormattedServerAttributeMapper.class)));

        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }

    private RpslObject updateMaintainer(final Set<RpslObject> allObjects) {
        for (final RpslObject rpslObject : ImmutableSet.copyOf(allObjects)) {
            if (rpslObject.getType().equals(ObjectType.MNTNER)) {
                return replaceReferences(rpslObject, allObjects);
            }
        }
        throw new IllegalStateException("No maintainer found");
    }

    private RpslObject replaceReferences(final RpslObject mntner, final Set<RpslObject> references) {
        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();

        for (final RpslAttribute rpslAttribute : mntner.getAttributes()) {
            for (final RpslObject reference : references) {
                if (rpslAttribute.getCleanValue().equals(reference.getKey()) &&
                        rpslAttribute.getType().getReferences().contains(ObjectType.PERSON) ||
                        rpslAttribute.getType().getReferences().contains(ObjectType.ROLE)) {
                    replacements.put(rpslAttribute, new RpslAttribute(rpslAttribute.getType(), dummyRole));
                }
            }
        }

        if (replacements.isEmpty()) {
            return mntner;
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(mntner);
        for (Map.Entry<RpslAttribute, RpslAttribute> entry : replacements.entrySet()) {
            builder.replaceAttribute(entry.getKey(), entry.getValue());
        }
        return builder.get();
    }

    private boolean referenceMatches(final RpslObjectInfo reference, final RpslObject rpslObject) {
        return rpslObject.getKey().equals(reference.getKey()) &&
                rpslObject.getType().equals(reference.getObjectType());
    }

    private Map<RpslObjectInfo, RpslObject> findReferences(final RpslObject rpslObject) {
        final Map<RpslObjectInfo, RpslObject> references = Maps.newHashMap();

        for (RpslObjectInfo rpslObjectInfo : rpslObjectUpdateDao.getReferences(rpslObject)) {
            references.put(rpslObjectInfo, rpslObjectDao.getById(rpslObjectInfo.getObjectId()));
        }

        return references;
    }

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
    }

    private void checkForMainSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throwBadRequest(request, RestMessages.invalidSource(source));
        }
    }

    private void throwBadRequest(final HttpServletRequest request, final Message message) {
        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(whoisService.createErrorEntity(request, message))
                .build());
    }

    private Response badRequest(final String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }
}
