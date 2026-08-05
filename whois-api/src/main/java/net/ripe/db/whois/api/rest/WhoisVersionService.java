package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import jakarta.ws.rs.core.HttpHeaders;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersions;
import net.ripe.db.whois.api.rest.domain.Version;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectServerMapper;
import net.ripe.db.whois.common.ApplicationVersion;
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
import net.ripe.db.whois.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.isQueryParamSet;

@Component
@Path("/")
public class WhoisVersionService {

    private final AccessControlListManager accessControlListManager;
    private final QueryHandler queryHandler;
    private final SourceContext sourceContext;
    private final WhoisObjectServerMapper whoisObjectServerMapper;
    private final VersionQueryExecutor versionQueryExecutor;
    private final Version version;

    @Autowired
    public WhoisVersionService(
            final AccessControlListManager accessControlListManager,
            final QueryHandler queryHandler,
            final SourceContext sourceContext,
            final WhoisObjectServerMapper whoisObjectServerMapper,
            final VersionQueryExecutor versionQueryExecutor,
            final ApplicationVersion applicationVersion) {
        this.accessControlListManager = accessControlListManager;
        this.queryHandler = queryHandler;
        this.sourceContext = sourceContext;
        this.whoisObjectServerMapper = whoisObjectServerMapper;
        this.versionQueryExecutor = versionQueryExecutor;
        this.version = new Version(
                applicationVersion.getVersion(),
                applicationVersion.getTimestamp(),
                applicationVersion.getCommitId());
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
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

        final String type = !versions.isEmpty() ? versions.getFirst().getType().getName() : deleted.getFirst().getType().getName();
        final List<WhoisVersion> mappedVersions = whoisObjectServerMapper.mapVersions(deleted, versions);
        // if an object existed and was later deleted, the 'delete' will show up as the first version in the list --
        // filter it out.
        final String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_PLAIN)) {
            return Response.ok(versionQueryExecutor.buildVersionsTextResponse(objectType, key, mappedVersions)).build();
        }
        while (!mappedVersions.isEmpty() && mappedVersions.getFirst().getDeletedDate() != null) {
            mappedVersions.removeFirst();
        }
        final WhoisVersions whoisVersions = new WhoisVersions(type, key, mappedVersions);

        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setVersions(whoisVersions);
        whoisResources.setErrorMessages(RestServiceHelper.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();
        whoisResources.setVersion(version);

        return Response.ok(whoisResources).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Path("/{source}/{objectType}/{key:.*}/versions/{version}")
    public Response version(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @PathParam("version") final Integer objectVersion,
            @QueryParam("unformatted") final String unformatted) {

        checkForMainSource(request, source);

        final QueryBuilder queryBuilder = new QueryBuilder()
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.getByName(objectType).getName())
                .addCommaList(QueryFlag.SHOW_VERSION, String.valueOf(objectVersion));

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

        final String accept = request.getHeader(HttpHeaders.ACCEPT);

        if (StringUtils.isNotEmpty(request.getHeader(HttpHeaders.ACCEPT)) && accept.contains(MediaType.TEXT_PLAIN)) {
            return Response.ok(versionWithRpslResponseObject.getRpslObject().toString()).build();
        }

        // TODO: [AH] this should use StreamingMarshal to properly handle newlines in errormessages
        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject whoisObject = whoisObjectServerMapper.map(versionWithRpslResponseObject.getRpslObject(), isQueryParamSet(unformatted));
        whoisObject.setVersion(versionWithRpslResponseObject.getVersion());
        whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        whoisResources.setErrorMessages(RestServiceHelper.createErrorMessages(versionsResponseHandler.getErrors()));
        whoisResources.includeTermsAndConditions();
        whoisResources.setVersion(version);

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
