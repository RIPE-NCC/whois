package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.mapper.FormattedServerAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import net.ripe.db.whois.query.domain.VersionWithRpslResponseObject;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Component
@Path("/")
public class WhoisVersionService {

    private final AccessControlListManager accessControlListManager;
    private final QueryHandler queryHandler;
    private final SourceContext sourceContext;
    private final WhoisObjectMapper whoisObjectMapper;
    private final WhoisObjectServerMapper whoisObjectServerMapper;

    @Autowired
    public WhoisVersionService(
            final AccessControlListManager accessControlListManager,
            final QueryHandler queryHandler,
            final SourceContext sourceContext,
            final WhoisObjectMapper whoisObjectMapper,
            final WhoisObjectServerMapper whoisObjectServerMapper) {
        this.accessControlListManager = accessControlListManager;
        this.queryHandler = queryHandler;
        this.sourceContext = sourceContext;
        this.whoisObjectMapper = whoisObjectMapper;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
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

        final String type = versions.size() > 0 ? versions.get(0).getType().getName() : deleted.get(0).getType().getName();
        final List<WhoisVersion> mappedVersions = whoisObjectServerMapper.mapVersions(deleted, versions);
        // if an object existed and was later deleted, the 'delete' will show up as the first version in the list --
        // filter it out.
        while (!mappedVersions.isEmpty() && mappedVersions.get(0).getDeletedDate() != null) {
            mappedVersions.remove(0);
        }
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, mappedVersions);

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

    private void checkForMainSource(final HttpServletRequest request, final String source) {
        if (!sourceContext.getCurrentSource().getName().toString().equalsIgnoreCase(source)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestServiceHelper.createErrorEntity(request, RestMessages.invalidSource(source)))
                    .build());
        }
    }

    private static class VersionsResponseHandler extends ApiResponseHandler {
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
}
