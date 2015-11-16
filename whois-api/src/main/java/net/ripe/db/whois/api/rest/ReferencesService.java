package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

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
    public Reference lookup(
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam) {

        final Reference result = new Reference(keyParam, objectTypeParam);
        populateIncomingReferences(result);

        for (Reference reference : result.getIncoming()) {
            populateIncomingReferences(reference);
        }

        return result;
    }

    private void populateIncomingReferences(final Reference reference) {
        final RpslObject primaryObject = lookupObjectByKey(reference.getPrimaryKey(), reference.getObjectType());

        for (Map.Entry<RpslObjectInfo, RpslObject> entry : findReferences(primaryObject).entrySet()) {
            final RpslObject referenceObject = entry.getValue();
            final Reference referenceToReference = new Reference(referenceObject.getKey().toString(), referenceObject.getType().getName());
            reference.getIncoming().add(referenceToReference);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}")
    public Response create(
                final WhoisResources resource,
                @PathParam("source") final String sourceParam,
                @Context final HttpServletRequest request,
                @QueryParam("password") final List<String> passwords,
                @CookieParam("crowd.token_key") final String crowdTokenKey) {

        if (resource == null) {
            return badRequest("WhoisResources is mandatory");
        }

        checkForMainSource(request, sourceParam);

        try {
            final List<ActionRequest> actionRequests = Lists.newArrayList();

            final RpslObject mntner = createMntnerWithDummyAdminC(resource);
            actionRequests.add(new ActionRequest(mntner, Action.CREATE));

            final RpslObject person = createPerson(resource);
            actionRequests.add(new ActionRequest(person, Action.CREATE));

            final RpslObject updatedMntner = replaceAdminC(mntner, "AUTO-1");
            actionRequests.add(new ActionRequest(updatedMntner, Action.MODIFY));

            final WhoisResources whoisResources = performUpdates(request, actionRequests, passwords, crowdTokenKey, null, SsoAuthForm.ACCOUNT);
            return createResponse(request, filterWhoisObjects(whoisResources), Response.Status.OK);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NotAuthorizedException(createResponse(request, resource, Response.Status.UNAUTHORIZED));

                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    throw new InternalServerErrorException(createResponse(request, resource, Response.Status.INTERNAL_SERVER_ERROR));

                default:
                    throw new BadRequestException(createResponse(request, resource, Response.Status.BAD_REQUEST));
            }

        } catch (ReferenceUpdateFailedException e) {
            return createResponse(request, e.whoisResources, e.status);
        }
        catch (Exception e) {
            LOGGER.error("Unexpected", e);
            return createResponse(request, resource, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private RpslObject createPerson(final WhoisResources resource) {
        return convertToRpslObject(resource, ObjectType.PERSON);
    }

    private RpslObject createMntnerWithDummyAdminC(final WhoisResources resource) {
        final RpslObject mntnerObject = convertToRpslObject(resource, ObjectType.MNTNER);
        return replaceAdminC(mntnerObject, dummyRole);
    }

    private RpslObject replaceAdminC(final RpslObject mntnerObject, final String adminC) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(mntnerObject);
        builder.replaceAttribute(mntnerObject.findAttribute(AttributeType.ADMIN_C), new RpslAttribute(AttributeType.ADMIN_C, adminC));
        return builder.get();
    }

    /**
     * Update multiple objects in the database. Rollback if any update fails.
     */
    private WhoisResources performUpdates(
            final HttpServletRequest request,
            final List<ActionRequest> actionRequests,
            final List<String> passwords,
            final String crowdTokenKey,
            final String override,
            final SsoAuthForm ssoAuthForm) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey);
            updateContext.batchUpdate();
            auditlogRequest(request);

            final List<Update> updates = Lists.newArrayList();
            for (ActionRequest actionRequest : actionRequests) {
                final String deleteReason = Action.DELETE.equals(actionRequest.getAction()) ? "--" : null;

                final RpslObject rpslObject;
                if (ssoAuthForm == SsoAuthForm.UUID){
                    ssoTranslator.populateCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                    rpslObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                } else {
                    rpslObject = actionRequest.getRpslObject();
                }
                updates.add(updatePerformer.createUpdate(updateContext, rpslObject, passwords, deleteReason, override));
            }

            final WhoisResources whoisResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NONE, request);

            for (Update update : updates) {
                final UpdateStatus status = updateContext.getStatus(update);

                if (status == UpdateStatus.SUCCESS) {
                    // continue
                } else if (status == UpdateStatus.FAILED_AUTHENTICATION) {
                    throw new ReferenceUpdateFailedException(Response.Status.UNAUTHORIZED, whoisResources);
                } else if (status == UpdateStatus.EXCEPTION) {
                    throw new ReferenceUpdateFailedException(Response.Status.INTERNAL_SERVER_ERROR, whoisResources);
                } else if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                    throw new ReferenceUpdateFailedException(Response.Status.CONFLICT, whoisResources);
                } else {
                    throw new ReferenceUpdateFailedException(Response.Status.BAD_REQUEST, whoisResources);
                }
            }

            return whoisResources;

        } catch (ReferenceUpdateFailedException e) {
            throw e;
        } catch (Exception e) {
            updatePerformer.logError(e);
            throw e;
        } finally {
            updatePerformer.closeContext();
        }
    }

    private WhoisObject convertToWhoisObject(final RpslObject rpslObject) {
        return whoisObjectMapper.map(rpslObject, FormattedServerAttributeMapper.class);
    }

    private RpslObject convertToRpslObject(final WhoisResources whoisResources, final ObjectType objectType) {
        for(WhoisObject whoisObject: whoisResources.getWhoisObjects()) {
            if (objectType == ObjectType.getByName(whoisObject.getType())) {
                return whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            }
        }

        throw new IllegalArgumentException("Unable to find " + objectType + " in WhoisResources");
    }

    private List<ActionRequest> convertToActionRequests(final WhoisResources whoisResources) {
        final List<ActionRequest> actionRequests = Lists.newArrayList();

        for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            final RpslObject rpslObject = whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            final Action action = whoisObject.getAction() != null ? whoisObject.getAction() : Action.MODIFY;
            actionRequests.add(new ActionRequest(rpslObject, action));
        }

        return actionRequests;
    }

    // return only the last version of each object
    private WhoisResources filterWhoisObjects(final WhoisResources whoisResources) {
        final Map<List<Attribute>, WhoisObject> result = Maps.newHashMap();

        for (WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            result.put(whoisObject.getPrimaryKey(), whoisObject);
        }

        whoisResources.setWhoisObjects(Lists.newArrayList(result.values()));
        return whoisResources;
    }

    //TODO [TP]: This method has to go to its own class and path.
    /**
     * Update one or more objects together (in the same transaction). If any update fails, then all changes are cancelled (rolled back).
     *
     * If any update fails, the response will contain all (attempted) changes up to, and including, that update.
     * Any error message will refer to the last attempted update.
     *
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}")
    public Response update(
            final WhoisResources resource,
            @PathParam("source") final String sourceParam,
            @Context final HttpServletRequest request,
            @QueryParam("override") final String override) {

        if (resource == null) {
            return badRequest("WhoisResources is mandatory");
        }

        if (Strings.isNullOrEmpty(override)) {
            return badRequest("override is mandatory");
        }

        checkForMainSource(request, sourceParam);

        try {
            final WhoisResources updatedResources = performUpdates(request, convertToActionRequests(resource), Collections.<String>emptyList(), "", override, SsoAuthForm.ACCOUNT);
            return createResponse(request, updatedResources, Response.Status.OK);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NotAuthorizedException(createResponse(request, resource, Response.Status.UNAUTHORIZED));

                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    throw new InternalServerErrorException(createResponse(request, resource, Response.Status.INTERNAL_SERVER_ERROR));

                default:
                    throw new BadRequestException(createResponse(request, resource, Response.Status.BAD_REQUEST));
            }

        } catch (ReferenceUpdateFailedException e) {
            return createResponse(request, e.whoisResources, e.status);
        }
        catch (Exception e) {
            LOGGER.error("Unexpected", e);
            return createResponse(request, resource, Response.Status.INTERNAL_SERVER_ERROR);
        }
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
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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

        final RpslObject primaryObject = lookupObjectByKey(keyParam, objectTypeParam);
        final Map<RpslObjectInfo, RpslObject> references = findReferences(primaryObject);
        validateReferences(primaryObject, references);

        try {

            if (references.isEmpty()) {
                // delete the primary object directly
                performUpdate(request, primaryObject, reason, passwords, crowdTokenKey);
                return createResponse(request, primaryObject, Response.Status.OK);
            }

            final List<ActionRequest> actionRequests = Lists.newArrayList();
            final Set<RpslObject> allObjects = Sets.newHashSet(Iterables.concat(references.values(), Lists.newArrayList(primaryObject)));

            // update the maintainer to point to a dummy person / role

            final RpslObjectWithReplacements tmpMntnerWithReplacements = replaceReferencesInMntner(allObjects);

            actionRequests.add(new ActionRequest(tmpMntnerWithReplacements.rpslObject, Action.MODIFY));

            // delete the person / role objects

            for (final RpslObject rpslObject : allObjects) {
                if (!rpslObject.getType().equals(ObjectType.MNTNER)) {
                    actionRequests.add(new ActionRequest(rpslObject, Action.DELETE));
                }
            }

            // delete the maintainer
            actionRequests.add(new ActionRequest(tmpMntnerWithReplacements.rpslObject, Action.DELETE));

            // batch update
            final WhoisResources whoisResources = performUpdates(request, actionRequests, passwords, crowdTokenKey, null, SsoAuthForm.UUID);

            removeDuplicatesAndRestoreReplacedReferences(whoisResources, tmpMntnerWithReplacements);

            return createResponse(request, whoisResources, Response.Status.OK);

        } catch (ReferenceUpdateFailedException e) {
            return createResponse(request, removeWhoisObjects(e.whoisResources), e.status);
        } catch (Exception e) {
            LOGGER.error("Unexpected", e);
            throw e;
        }
    }

    private WhoisResources removeWhoisObjects(final WhoisResources whoisResources){
        whoisResources.setWhoisObjects(null);
        return whoisResources;
    }

    private void removeDuplicatesAndRestoreReplacedReferences(final WhoisResources whoisResources, final RpslObjectWithReplacements tmpMntnerWithReplacements) {
        final Map<List<Attribute>, WhoisObject> result = Maps.newHashMap();

        for (final WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            if (whoisObject.getType().equalsIgnoreCase(AttributeType.MNTNER.getName())){
                final RpslObject mntnerWithDummyRole = whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);

                final RpslObjectBuilder builder = new RpslObjectBuilder(mntnerWithDummyRole);

                for (final Map.Entry<RpslAttribute, RpslAttribute> entry : tmpMntnerWithReplacements.replacements.entrySet()) {
                    builder.removeAttribute(entry.getValue())
                            .addAttributeSorted(entry.getKey());
                }
                result.put(whoisObject.getPrimaryKey(), whoisObjectMapper.map(builder.get(), FormattedServerAttributeMapper.class));
            } else {
                result.put(whoisObject.getPrimaryKey(), whoisObject);
            }
        }

        whoisResources.setWhoisObjects(Lists.newArrayList(result.values()));
    }

    /**
     * Update a single object in the database
     */
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

            final Response response = updatePerformer.createResponse(
                    updateContext,
                    updatePerformer.performUpdate(
                            updateContext,
                            origin,
                            update,
                            Keyword.NONE,
                            request),
                    update,
                    request);

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {

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
            updatePerformer.logError(e);
            throw e;
        } finally {
            updatePerformer.closeContext();
        }
    }

    private void validateReferences(final RpslObject primaryObject, final Map<RpslObjectInfo, RpslObject> references) {

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
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setWhoisObjects(Lists.newArrayList(convertToWhoisObject(primaryObject)));
        return createResponse(request, whoisResources, status);
    }

    private Response createResponse(final HttpServletRequest request, final Collection<RpslObject> objects, final Response.Status status) {
        final WhoisResources whoisResources = new WhoisResources();

        whoisResources.setWhoisObjects(FluentIterable.from(objects).transform(new Function<RpslObject, WhoisObject>() {
            @Nullable
            @Override
            public WhoisObject apply(final RpslObject input) {
                return convertToWhoisObject(input);
            }
        }).toList());
        return createResponse(request, whoisResources, status);
    }

    private Response createResponse(final HttpServletRequest request, final WhoisResources whoisResources, final Response.Status status) {
        final Response.ResponseBuilder responseBuilder = Response.status(status);
       return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }

    private RpslObjectWithReplacements replaceReferencesInMntner(final Collection<RpslObject> allObjects) {
        for (final RpslObject rpslObject : ImmutableSet.copyOf(allObjects)) {
            if (rpslObject.getType().equals(ObjectType.MNTNER)) {
                return replaceReferences(rpslObject, allObjects);
            }
        }
        throw new IllegalStateException("No maintainer found");
    }

    private RpslObjectWithReplacements replaceReferences(final RpslObject mntner, final Collection<RpslObject> references) {
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
            return new RpslObjectWithReplacements(mntner, replacements);
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(mntner);
        for (Map.Entry<RpslAttribute, RpslAttribute> entry : replacements.entrySet()) {
            builder.replaceAttribute(entry.getKey(), entry.getValue());
        }
        return new RpslObjectWithReplacements(builder.get(), replacements);
    }

    private boolean referenceMatches(final RpslObjectInfo reference, final RpslObject rpslObject) {
        return rpslObject.getKey().equals(reference.getKey()) &&
                rpslObject.getType().equals(reference.getObjectType());
    }

    // DAO calls

    private RpslObject lookupObjectByKey(final String primaryKey, final String objectType) {
        try {
            return rpslObjectDao.getByKey(ObjectType.getByName(objectType), primaryKey);
        } catch (EmptyResultDataAccessException e) {
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("Unexpected", e);
            throw new EmptyResultDataAccessException(1);
        }
    }

    private Map<RpslObjectInfo, RpslObject> findReferences(final RpslObject rpslObject) {
        final Map<RpslObjectInfo, RpslObject> references = Maps.newHashMap();

        try {
            for (RpslObjectInfo rpslObjectInfo : rpslObjectUpdateDao.getReferences(rpslObject)) {
                references.put(rpslObjectInfo, rpslObjectDao.getById(rpslObjectInfo.getObjectId()));
            }
        } catch (EmptyResultDataAccessException e) {
            throw e;
        } catch (DataAccessException e) {
            LOGGER.error("Unexpected", e);
            throw new EmptyResultDataAccessException(1);
        }

        return references;
    }

    // helper methods

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

    private Response internalServerError(final String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
    }

    private Response ok(final Object message) {
        return Response.ok(message).build();
    }

    // model classes

    static class RpslObjectWithReplacements {
        private final RpslObject rpslObject;
        private final Map<RpslAttribute, RpslAttribute> replacements;

        RpslObjectWithReplacements(final RpslObject rpslObject, final Map<RpslAttribute, RpslAttribute> replacements) {
            this.rpslObject = rpslObject;
            this.replacements = replacements;
        }
    }

    enum SsoAuthForm {ACCOUNT, UUID}

    @XmlRootElement(name = "references")
    @JsonInclude(NON_EMPTY)
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Reference {
        @XmlElement
        private String primaryKey;
        @XmlElement
        private String objectType;
        @XmlElementWrapper(name="incoming")
        @XmlElementRef
        @JsonInclude(NON_EMPTY)
        private List<Reference> incoming;
        @XmlElementWrapper(name="outgoing")
        @XmlElementRef
        @JsonInclude(NON_EMPTY)
        private List<Reference> outgoing;

        private Reference() {
            // required no-arg constructor
        }

        public Reference(final String primaryKey, final String objectType) {
            this.primaryKey = primaryKey;
            this.objectType = objectType;
            this.incoming = Lists.newArrayList();
            this.outgoing = Lists.newArrayList();
        }

        public String getPrimaryKey() {
            return primaryKey;
        }

        public String getObjectType() {
            return objectType;
        }

        public List<Reference> getIncoming() {
            return incoming;
        }

        public List<Reference> getOutgoing() {
            return outgoing;
        }
    }

    private class ReferenceUpdateFailedException extends RuntimeException {
        private final WhoisResources whoisResources;
        private final Response.Status status;

        public ReferenceUpdateFailedException(Response.Status status, WhoisResources whoisResources) {
            this.status = status;
            this.whoisResources = whoisResources;
        }
    }
}
