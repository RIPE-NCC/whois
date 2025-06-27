package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.rest.marshal.StreamingHelper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.oauth.OAuthUtils;
import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.sso.AuthServiceClient;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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
    private final ReferencesDao referencesDao;
    private final SourceContext sourceContext;
    private final InternalUpdatePerformer updatePerformer;
    private final SsoTranslator ssoTranslator;
    private final LoggerContext loggerContext;
    private final WhoisObjectMapper whoisObjectMapper;
    private final Map dummyMap;
    private final String dummyRole;
    @Autowired
    public ReferencesService(
            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
            final ReferencesDao referencesDao,
            final SourceContext sourceContext,
            final InternalUpdatePerformer updatePerformer,
            final SsoTranslator ssoTranslator,
            final LoggerContext loggerContext,
            final WhoisObjectMapper whoisObjectMapper,
            final @Value("#{${whois.dummy}}") Map<String, String> dummyMap) {
        this.rpslObjectDao = rpslObjectDao;
        this.referencesDao = referencesDao;
        this.sourceContext = sourceContext;
        this.updatePerformer = updatePerformer;
        this.ssoTranslator = ssoTranslator;
        this.loggerContext = loggerContext;
        this.whoisObjectMapper = whoisObjectMapper;
        this.dummyMap = dummyMap;
        this.dummyRole = dummyMap.get(AttributeType.ADMIN_C.toString());
    }

    /**
     * Return all incoming references for a given object type and primary key
     *
     * @param sourceParam
     * @param objectTypeParam
     * @param keyParam
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{source}/{objectType}/{key:.*}")
    public Reference lookup(
            @PathParam("source") final String sourceParam,
            @PathParam("objectType") final String objectTypeParam,
            @PathParam("key") final String keyParam) {
        try {
            sourceContext.setCurrent(sourceContext.getSlaveSource());

            final Reference result = new Reference(keyParam, objectTypeParam);
            lookupIncomingReferences(result);

            for (final Reference reference : result.getIncoming()) {
                lookupIncomingReferences(reference);
            }

            return result;
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    private void lookupIncomingReferences(final Reference reference) {
        final RpslObject primaryObject = lookupObjectByKey(reference.getPrimaryKey(), reference.getObjectType());

        for (final Map.Entry<RpslObjectInfo, RpslObject> entry : findReferences(primaryObject).entrySet()) {
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
                final WhoisResources whoisResources,
                @PathParam("source") final String sourceParam,
                @Context final HttpServletRequest request,
                @QueryParam("password") final List<String> passwords,
                @QueryParam(OAuthUtils.APIKEY_KEY_ID_QUERY_PARAM) final String apiKeyId,
                @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {

        validateWhoisResources(whoisResources);
        validateSource(sourceParam);

        try {
            final List<ActionRequest> actionRequests = Lists.newArrayList();

            final RpslObject mntner = createMntnerWithDummyAdminC(whoisResources);
            actionRequests.add(new ActionRequest(mntner, Action.CREATE));

            final RpslObject person = createPerson(whoisResources);
            actionRequests.add(new ActionRequest(person, Action.CREATE));

            final RpslObject updatedMntner = replaceAdminC(mntner, "AUTO-1");
            actionRequests.add(new ActionRequest(updatedMntner, Action.MODIFY));

            validateObjectNotFound(whoisResources, mntner);

            final WhoisResources updatedResources = performUpdates(request, actionRequests, passwords, crowdTokenKey, apiKeyId, null, SsoAuthForm.ACCOUNT, null);
            return createResponse(request, filterWhoisObjects(updatedResources), Response.Status.OK);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();
            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NotAuthorizedException(createResponse(request, whoisResources, Response.Status.UNAUTHORIZED));

                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    throw new InternalServerErrorException(createResponse(request, whoisResources, Response.Status.INTERNAL_SERVER_ERROR));

                default:
                    throw new BadRequestException(createResponse(request, whoisResources, Response.Status.BAD_REQUEST));
            }
        } catch (ReferenceUpdateFailedException e) {
            return createResponse(request, e.whoisResources, e.status);
        } catch (Exception e) {
            LOGGER.error("Unexpected", e);
            return createResponse(request, whoisResources, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private RpslObject createPerson(final WhoisResources whoisResources) {
        return convertToRpslObject(whoisResources, ObjectType.PERSON);
    }

    private RpslObject createMntnerWithDummyAdminC(final WhoisResources whoisResources) {
        final RpslObject mntnerObject = convertToRpslObject(whoisResources, ObjectType.MNTNER);
        return replaceAdminC(mntnerObject, dummyRole);
    }

    private RpslObject replaceAdminC(final RpslObject mntnerObject, final String adminC) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(mntnerObject);
        builder.replaceAttribute(mntnerObject.findAttribute(AttributeType.ADMIN_C), new RpslAttribute(AttributeType.ADMIN_C, adminC));
        return builder.get();
    }

    /**
     * Update multiple objects in the database. Rollback if any update fails.
     * Must be public for Transaction-annotation to have effect
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public WhoisResources performUpdates(
            final HttpServletRequest request,
            final List<ActionRequest> actionRequests,
            final List<String> passwords,
            final String crowdTokenKey,
            final String apiKeyId,
            final String override,
            final SsoAuthForm ssoAuthForm,
            final String reason) {

        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey, apiKeyId, request);
            updateContext.setBatchUpdate();
            auditlogRequest(request);

            final List<Update> updates = Lists.newArrayList();
            for (final ActionRequest actionRequest : actionRequests) {
                final String deleteReason = Action.DELETE.equals(actionRequest.getAction()) ? (reason != null ? reason : "--") : null;

                final RpslObject rpslObject;
                if (ssoAuthForm == SsoAuthForm.UUID) {
                    ssoTranslator.populateCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                    rpslObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, actionRequest.getRpslObject());
                } else {
                    rpslObject = actionRequest.getRpslObject();
                }
                updates.add(updatePerformer.createUpdate(updateContext, rpslObject, passwords, deleteReason, override));
            }

            final WhoisResources whoisResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NONE, request);

            for (final Update update : updates) {
                final UpdateStatus status = updateContext.getStatus(update);

                if (status != UpdateStatus.SUCCESS) {
                    if (status == UpdateStatus.FAILED_AUTHENTICATION) {
                        throw new ReferenceUpdateFailedException(Response.Status.UNAUTHORIZED, whoisResources);
                    } else if (status == UpdateStatus.EXCEPTION) {
                        throw new ReferenceUpdateFailedException(Response.Status.INTERNAL_SERVER_ERROR, whoisResources);
                    } else if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                        throw new ReferenceUpdateFailedException(Response.Status.CONFLICT, whoisResources);
                    } else {
                        throw new ReferenceUpdateFailedException(Response.Status.BAD_REQUEST, whoisResources);
                    }
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
        for (final WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            if (objectType == ObjectType.getByName(whoisObject.getType())) {
                return whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            }
        }

        setErrorMessage(whoisResources, "Unable to find " + objectType + " in WhoisResources");
        throw new ReferenceUpdateFailedException(Response.Status.BAD_REQUEST, whoisResources);
    }

    private List<ActionRequest> convertToActionRequests(final WhoisResources whoisResources) {
        final List<ActionRequest> actionRequests = Lists.newArrayList();

        for (final WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            final RpslObject rpslObject = whoisObjectMapper.map(whoisObject, FormattedServerAttributeMapper.class);
            final Action action = whoisObject.getAction() != null ? whoisObject.getAction() : Action.MODIFY;
            actionRequests.add(new ActionRequest(rpslObject, action));
        }

        return actionRequests;
    }

    // return only the last version of each object
    private WhoisResources filterWhoisObjects(final WhoisResources whoisResources) {
        final Map<List<Attribute>, WhoisObject> result = Maps.newHashMap();

        for (final WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
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
            final WhoisResources whoisResources,
            @PathParam("source") final String sourceParam,
            @Context final HttpServletRequest request,
            @QueryParam("override") final String override) {

        validateWhoisResources(whoisResources);
        validateOverride(override);
        validateSource(sourceParam);

        try {
            final WhoisResources updatedResources = performUpdates(request, convertToActionRequests(whoisResources), Collections.emptyList(), "", null, override, SsoAuthForm.ACCOUNT, null);
            return createResponse(request, updatedResources, Response.Status.OK);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            switch (response.getStatus()) {
                case HttpStatus.UNAUTHORIZED_401:
                    throw new NotAuthorizedException(createResponse(request, whoisResources, Response.Status.UNAUTHORIZED));

                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    throw new InternalServerErrorException(createResponse(request, whoisResources, Response.Status.INTERNAL_SERVER_ERROR));

                default:
                    throw new BadRequestException(createResponse(request, whoisResources, Response.Status.BAD_REQUEST));
            }

        } catch (ReferenceUpdateFailedException e) {
            return createResponse(request, e.whoisResources, e.status);
        }
        catch (Exception e) {
            LOGGER.error("Unexpected", e);
            return createResponse(request, whoisResources, Response.Status.INTERNAL_SERVER_ERROR);
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
            @QueryParam(OAuthUtils.APIKEY_KEY_ID_QUERY_PARAM) final String apiKeyId,
            @QueryParam("override") final String override,
            @CookieParam(AuthServiceClient.TOKEN_KEY) final String crowdTokenKey) {

        validateSource(sourceParam);

        final RpslObject primaryObject = lookupObjectByKey(keyParam, objectTypeParam);
        final Map<RpslObjectInfo, RpslObject> references = findReferences(primaryObject);
        validateReferences(primaryObject, references);

        try {

            if (references.isEmpty()) {
                // delete the primary object directly
                performUpdate(request, primaryObject, reason, passwords, apiKeyId, crowdTokenKey);
                return createResponse(request, primaryObject, Response.Status.OK);
            }

            final List<ActionRequest> actionRequests = Lists.newArrayList();
            final Set<RpslObject> allObjects = Sets.newHashSet(Iterables.concat(references.values(), Lists.newArrayList(primaryObject)));

            // update the maintainer to point to a dummy person / role

            // replace any possible referenes with dummy values
            RpslObjectWithReplacements tmpMntnerWithReplacements = replaceReferencesInMntner(allObjects);

            // check objects are correctly formed, modify rpsl if not
            tmpMntnerWithReplacements.rpslObject = correctAnySyntaxErrors(tmpMntnerWithReplacements.rpslObject);

            // modify maintainer
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
            final WhoisResources whoisResources = performUpdates(request, actionRequests, passwords, crowdTokenKey, apiKeyId, override, SsoAuthForm.UUID, reason);

            removeDuplicatesAndRestoreReplacedReferences(whoisResources, tmpMntnerWithReplacements);

            return createResponse(request, whoisResources, Response.Status.OK);

        } catch (ReferenceUpdateFailedException e) {
            removeWhoisObjects(e.whoisResources);
            return createResponse(request, e.whoisResources, e.status);
        } catch (Exception e) {
            LOGGER.error("Unexpected", e);
            throw e;
        }
    }

    private void removeWhoisObjects(final WhoisResources whoisResources) {
        whoisResources.setWhoisObjects(null);
    }

    private void setErrorMessage(final WhoisResources whoisResources, final String errorMessage) {
        whoisResources.setErrorMessages(Lists.newArrayList(new ErrorMessage(new Message(Messages.Type.ERROR, errorMessage))));
    }

    private void removeDuplicatesAndRestoreReplacedReferences(final WhoisResources whoisResources, final RpslObjectWithReplacements tmpMntnerWithReplacements) {
        final Map<List<Attribute>, WhoisObject> result = Maps.newHashMap();

        for (final WhoisObject whoisObject : whoisResources.getWhoisObjects()) {
            if (whoisObject.getType().equalsIgnoreCase(AttributeType.MNTNER.getName())) {
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
                final String apiKeyId,
                final String crowdTokenKey) {
        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, crowdTokenKey, apiKeyId, request);

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
                for (final ErrorMessage errorMessage : whoisResources.getErrorMessages()) {
                    LOGGER.warn("Error Message: {}", errorMessage);
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

            for (final Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {
                final RpslObjectInfo reference = entry.getKey();

                if (!reference.getObjectType().equals(ObjectType.PERSON) && !reference.getObjectType().equals(ObjectType.ROLE)) {
                    throw new IllegalArgumentException("Referencing object " + entry.getKey().getKey() + " type " + entry.getKey().getObjectType() + " is not supported.");
                }
            }

        } else {

            if (primaryObject.getType().equals(ObjectType.PERSON) || primaryObject.getType().equals(ObjectType.ROLE)) {
                for (final Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {
                    final RpslObjectInfo reference = entry.getKey();
                    if (!reference.getObjectType().equals(ObjectType.MNTNER)) {
                        // references must be of mntner only
                        throw new IllegalArgumentException("Referencing object " + entry.getKey().getKey() + " type " + entry.getKey().getObjectType() + " is not supported.");
                    }
                }
            } else {
                throw new IllegalArgumentException("Object type " + primaryObject.getType() + " is not supported.");
            }
        }

        // validate references - ensure a closed group

        for (final Map.Entry<RpslObjectInfo, RpslObject> entry : references.entrySet()) {
            final RpslObject reference = entry.getValue();
            for (final RpslObjectInfo referenceToReference : referencesDao.getReferences(reference)) {
                if (!referenceMatches(referenceToReference, primaryObject) && !references.keySet().contains(referenceToReference)) {
                    throw new IllegalArgumentException("Referencing object " + reference.getKey()  + " itself is referenced by " + referenceToReference.getKey());
                }
            }
        }
    }

    private void validateObjectNotFound(final WhoisResources whoisResources, final RpslObject rpslObject) {
        try {
            lookupObjectByKey(rpslObject.getKey().toString(), rpslObject.getType());
            setErrorMessage(whoisResources, rpslObject.getType().getName() + " " + rpslObject.getKey() + " already exists");
            throw new ReferenceUpdateFailedException(Response.Status.BAD_REQUEST, whoisResources);
        } catch (EmptyResultDataAccessException e) {
            // object not found (expected)
        }
    }

    private Response createResponse(final HttpServletRequest request, final RpslObject primaryObject, final Response.Status status) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setWhoisObjects(Lists.newArrayList(convertToWhoisObject(primaryObject)));
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

    private RpslObjectWithReplacements replaceReferences(final RpslObject object, final Collection<RpslObject> references) {
        final Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();

        for (final RpslAttribute rpslAttribute : object.getAttributes()) {
            final AttributeType attributeType = rpslAttribute.getType();
            if (attributeType != null) {
                for (final RpslObject reference : references) {
                    if (rpslAttribute.getCleanValue().equals(reference.getKey()) &&
                            (attributeType.getReferences().contains(ObjectType.PERSON) ||
                                    attributeType.getReferences().contains(ObjectType.ROLE))) {
                        replacements.put(rpslAttribute, new RpslAttribute(attributeType, dummyRole));
                    }
                }
            }
        }

        if (replacements.isEmpty()) {
            return new RpslObjectWithReplacements(object, replacements);
        }

        final RpslObjectBuilder builder = new RpslObjectBuilder(object);
        builder.replaceAttributes(replacements);

        return new RpslObjectWithReplacements(builder.get(), replacements);
    }

    public RpslObject correctAnySyntaxErrors(RpslObject rpslObject) {
        final ObjectType rpslObjectType = rpslObject.getType();
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObjectType);

        final Map<AttributeType, Integer> attributeCount = Maps.newEnumMap(AttributeType.class);
        final List<AttributeTemplate> attributeTemplates = objectTemplate.getAttributeTemplates();

        // determine possible attributes for object type
        for (final AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
            attributeCount.put(attributeTemplate.getAttributeType(), 0);
        }

        // count instances of each attribute
        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            final AttributeType attributeType = attribute.getType();

            if (attributeType != null) {
                if (!attributeCount.containsKey(attributeType)) {
                    LOGGER.warn("Ignoring {} attribute not in {} object template for {}", attributeType.getName(), objectTemplate.getObjectType().getName(), rpslObject.getKey());
                    continue;
                }

                attributeCount.put(attributeType, attributeCount.get(attributeType) + 1);
            }
        }

        // iterate through possible object attributes and check against what we have in the submitted object
        for (final AttributeTemplate attributeTemplate : attributeTemplates) {
            final AttributeType attributeType = attributeTemplate.getAttributeType();
            final int attributeTypeCount = attributeCount.get(attributeType);

            // if we are missing any mandatory attributes add a dummy attribute so the object is valid
            if (attributeTemplate.getRequirement() == AttributeTemplate.Requirement.MANDATORY && attributeTypeCount == 0) {
                rpslObject = addDummyAttribute(rpslObject, attributeType);
            }
        }
        return rpslObject;
    }

    public RpslObject addDummyAttribute(final RpslObject rpslObject, final AttributeType attributeType) {
        final RpslObjectBuilder builder = new RpslObjectBuilder(rpslObject);
        final Object dummyValue = dummyMap.get(attributeType.toString());
        if (dummyValue != null)
        {
            final RpslAttribute attr = new RpslAttribute(attributeType, dummyValue.toString());
            return builder.addAttributeSorted(attr).get();
        }
        else {
            return rpslObject;
        }
    }

    private boolean referenceMatches(final RpslObjectInfo reference, final RpslObject rpslObject) {
        return rpslObject.getKey().equals(reference.getKey()) &&
                rpslObject.getType().equals(reference.getObjectType());
    }

    // DAO calls

    private RpslObject lookupObjectByKey(final String primaryKey, final String objectType) {
        return lookupObjectByKey(primaryKey, ObjectType.getByName(objectType));
    }

    private RpslObject lookupObjectByKey(final String primaryKey, final ObjectType objectType) {
        try {
            return rpslObjectDao.getByKey(objectType, primaryKey);
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
            for (final RpslObjectInfo rpslObjectInfo : referencesDao.getReferences(rpslObject)) {
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

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
    }

    private void validateSource(final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new IllegalArgumentException("Invalid source '" + source + "'");
        }
    }

    private void validateWhoisResources(@Nullable final WhoisResources whoisResources) {
        if (whoisResources == null) {
            throw new IllegalArgumentException("WhoisResources is mandatory");
        }
    }

    private void validateOverride(@Nullable final String override) {
        if (Strings.isNullOrEmpty(override)) {
            throw new IllegalArgumentException("override is mandatory");
        }
    }

    // model classes

    static class RpslObjectWithReplacements {
        private RpslObject rpslObject;
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

        public ReferenceUpdateFailedException(final Response.Status status, final WhoisResources whoisResources) {
            this.status = status;
            this.whoisResources = whoisResources;
        }
    }
}
