package net.ripe.db.whois.api.whois;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.sun.jersey.api.NotFoundException;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.*;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.apache.commons.lang.StringUtils;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRestService {
    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    private static final String TEXT_JSON = "text/json";
    private static final String TEXT_XML = "text/xml";

    private final DateTimeProvider dateTimeProvider;
    private final UpdateRequestHandler updateRequestHandler;
    private final LoggerContext loggerContext;
    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;

    private static final Pattern UPDATE_RESPONSE_ERRORS = Pattern.compile("(?m)^\\*\\*\\*Error:\\s*((.*)(\\n[ ]+.*)*)$");
    private static final Joiner JOINER = Joiner.on(",");
    private static final Set<Character> NOT_ALLOWED_SEARCH_FLAGS = Sets.newHashSet('k');

    @Autowired
    public WhoisRestService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.loggerContext = loggerContext;
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
    }

    @GET
    @TypeHint(WhoisResources.class)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/lookup/{source}/{objectType}/{key:.*}")
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {
        return lookupObject(request, source, objectType, key, false);
    }

    @GET
    @TypeHint(WhoisResources.class)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/grs-lookup/{source}/{objectType}/{key:.*}")
    public Response grslookup(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {
        return lookupObject(request, source, objectType, key, true);
    }

    private Response lookupObject(final HttpServletRequest request, final String source, final String objectTypeString, final String key, final boolean isGrsExpected) {
        final Query query = Query.parse(String.format("%s %s %s %s %s",
                QueryFlag.SOURCES.getLongFlag(), source,
                QueryFlag.SELECT_TYPES.getLongFlag(), objectTypeString,
                key));

        if (sourceContext.getGrsSourceNames().contains(ciString(source)) != isGrsExpected) {
            throw new IllegalArgumentException(String.format("The given grs source id: '%s' is not valid", source));
        }

        return handleQuery(query, source, key, request, null);
    }

    private Response handleQuery(final Query query, final String source, final String key, final HttpServletRequest request, @Nullable final Parameters parameters) {
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());
        final int contextId = System.identityHashCode(Thread.currentThread());

        if (query.isVersionList() || query.isObjectVersion()) {
            return handleVersionQuery(query, source, key, remoteAddress, contextId);
        }

        return handleQueryAndStreamResponse(query, request, remoteAddress, contextId, parameters);
    }

    private Response handleVersionQuery(final Query query, final String source, final String key, final InetAddress remoteAddress, final int contextId) {
        final ApiResponseHandlerVersions apiResponseHandlerVersions = new ApiResponseHandlerVersions();
        queryHandler.streamResults(query, remoteAddress, contextId, apiResponseHandlerVersions);

        final VersionWithRpslResponseObject versionResponseObject = apiResponseHandlerVersions.getVersionWithRpslResponseObject();
        final List<DeletedVersionResponseObject> deleted = apiResponseHandlerVersions.getDeletedObjects();
        final List<VersionResponseObject> versions = apiResponseHandlerVersions.getVersionObjects();

        if (versionResponseObject == null && deleted.isEmpty() && versions.isEmpty()) {
            throw new NotFoundException();
        }

        final WhoisResources whoisResources = new WhoisResources();

        if (versionResponseObject != null) {
            final WhoisObject whoisObject = WhoisObjectMapper.map(versionResponseObject.getRpslObject());
            whoisObject.setVersion(versionResponseObject.getVersion());
            whoisResources.setWhoisObjects(Collections.singletonList(whoisObject));
        } else {
            final String type = (versions.size() > 0) ? versions.get(0).getType().getName() : deleted.size() > 0 ? deleted.get(0).getType().getName() : null;
            final WhoisVersions whoisVersions = new WhoisVersions(source, type, key, WhoisObjectMapper.mapVersions(deleted, versions));
            whoisResources.setVersions(whoisVersions);
        }

        return Response.ok(whoisResources).build();
    }

    private Response handleQueryAndStreamResponse(final Query query, final HttpServletRequest request, final InetAddress remoteAddress, final int contextId, @Nullable final Parameters parameters) {
        final StreamingMarshal streamingMarshal = getStreamingMarshal(request);

        return Response.ok(new StreamingOutput() {
            private boolean found;

            @Override
            public void write(final OutputStream output) throws IOException {
                streamingMarshal.open(output);
                streamingMarshal.start("whois-resources");

                if (parameters != null) {
                    streamingMarshal.write("parameters", parameters);
                }

                streamingMarshal.start("objects");

                // TODO [AK] Crude way to handle tags, but working
                final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<RpslObject>(1);
                final List<TagResponseObject> tagResponseObjects = Lists.newArrayList();

                try {
                    queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {

                        @Override
                        public void handle(final ResponseObject responseObject) {
                            if (responseObject instanceof TagResponseObject) {
                                tagResponseObjects.add((TagResponseObject) responseObject);
                            } else if (responseObject instanceof RpslObject) {
                                found = true;
                                streamObject(rpslObjectQueue.poll(), tagResponseObjects);
                                rpslObjectQueue.add((RpslObject) responseObject);
                            }

                            // TODO [AK] Handle related messages
                        }
                    });

                    streamObject(rpslObjectQueue.poll(), tagResponseObjects);

                    if (!found) {
                        throw new NotFoundException();
                    }
                } catch (QueryException e) {
                    if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                        throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
                    } else {
                        throw new RuntimeException("Unexpected result", e);
                    }
                }

                streamingMarshal.close();
            }

            private void streamObject(@Nullable final RpslObject rpslObject, final List<TagResponseObject> tagResponseObjects) {
                if (rpslObject == null) {
                    return;
                }

                final WhoisObject whoisObject = WhoisObjectMapper.map(rpslObject);

                // TODO [AK] Fix mapper API
                final List<WhoisTag> tags = WhoisObjectMapper.mapTags(tagResponseObjects).getTags();
                whoisObject.setTags(tags);

                streamingMarshal.write("object", whoisObject);
                tagResponseObjects.clear();
            }
        }).build();
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

    @POST
    // TODO [AK] Does text/json actually result in a json response and text/xml in xml?
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Path("/create/{source}")
    public Response create(
            final WhoisResources resources,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @QueryParam(value = "password") final List<String> passwords) {

        final RpslObject submittedObject = getSubmittedObject(resources);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(submittedObject, null, passwords, null),
                createContent(submittedObject, passwords, null),
                Keyword.NEW,
                source);

        return getResponse(response);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Path("/create")
    public Response create() {
        // Create request without including source in URL is no longer allowed.
        // Source needs to be included to be consistent with the other CRUD operations, and also
        // to allow mod-proxy to redirect requests to the correct instance.
        throw new IllegalArgumentException("Source must be specified in URL");
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @TypeHint(WhoisResources.class)
    @Path("/update/{source}/{objectType}/{key:.*}")
    public Response update(
            final WhoisResources resource,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam(value = "password") final List<String> passwords) {

        final RpslObject submittedObject = getSubmittedObject(resource);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(submittedObject, null, passwords, null),
                createContent(submittedObject, passwords, null),
                Keyword.NONE,
                source);

        if (response.getStatus().equals(UpdateStatus.SUCCESS)) {
            final RpslObject updatedObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);
            final WhoisResources whoisResources = createWhoisResources(request, updatedObject);
            return Response.ok(whoisResources).build();
        }

        return getResponse(response);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Path("/modify/{source}/{objectType}/{key:.*}")
    public Response modify(
            final WhoisModify whoisModify,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam(value = "password") final List<String> passwords) {

        // TODO: [AH] this should be a single dao call
        RpslObjectUpdateInfo objectInfo = rpslObjectUpdateDao.lookupObject(ObjectType.getByName(objectType), key);
        RpslObject originalObject = rpslObjectDao.getById(objectInfo.getObjectId());
        RpslObject updatedObject = modifyRpslObject(originalObject, whoisModify);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(updatedObject, objectInfo, passwords, null),
                createContent(updatedObject, passwords, null),
                Keyword.NONE,
                source);

        if (!response.getStatus().equals(UpdateStatus.SUCCESS)) {
            return getResponse(response);
        }

        final WhoisResources whoisResources = createWhoisResources(request, updatedObject);
        return Response.ok(whoisResources).build();
    }

    private RpslObject modifyRpslObject(final RpslObject rpslObject, final WhoisModify whoisModify) {
        if (whoisModify.getReplace() != null) {
            return modifyReplaceAttributes(rpslObject, whoisModify.getReplace());
        } else if (whoisModify.getAdd() != null) {
            return modifyAddAttributes(rpslObject, whoisModify.getAdd());
        } else if (whoisModify.getRemove() != null) {
            return modifyRemoveAttributes(rpslObject, whoisModify.getRemove());
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    private RpslObject modifyAddAttributes(final RpslObject rpslObject, final WhoisModify.Add add) {
        final List<RpslAttribute> additions = Lists.newArrayList();
        for (Attribute attribute : add.getAttributes()) {
            additions.add(new RpslAttribute(AttributeType.getByName(attribute.getName()), attribute.getValue()));
        }

        final int index = add.getIndex();
        if (index == -1) {
            return new RpslObjectFilter(rpslObject).addAttributes(additions);
        } else {
            return new RpslObjectFilter(rpslObject).addAttributes(additions, index);
        }
    }

    private RpslObject modifyReplaceAttributes(final RpslObject rpslObject, final WhoisModify.Replace replace) {
        final AttributeType type = AttributeType.getByName(replace.getAttributeType());

        final RpslObject updatedObject = RpslObjectFilter.removeAttributeTypes(rpslObject, Lists.newArrayList(type));

        final List<RpslAttribute> replacements = Lists.newArrayList();
        for (Attribute attribute : replace.getAttributes()) {
            replacements.add(new RpslAttribute(AttributeType.getByName(attribute.getName()), attribute.getValue()));
        }

        return new RpslObjectFilter(updatedObject).addAttributes(replacements);
    }

    private RpslObject modifyRemoveAttributes(final RpslObject rpslObject, final WhoisModify.Remove remove) {
        final int index = remove.getIndex();
        if (index != -1) {
            return RpslObjectFilter.removeAttribute(rpslObject, index);
        } else {
            final String type = remove.getAttributeType();
            if (type != null) {
                return RpslObjectFilter.removeAttributeTypes(rpslObject, Lists.newArrayList(AttributeType.getByName(type)));
            }

            throw new IllegalArgumentException("Invalid request");
        }
    }

    @DELETE
    @Path("/delete/{source}/{objectType}/{key:.*}")
    public Response delete(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam(value = "reason") @DefaultValue("--") final String reason,
            @QueryParam(value = "password") final List<String> passwords) {

        final RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(originalObject, null, passwords, reason),
                createContent(originalObject, passwords, reason),
                Keyword.NONE,
                source);

        return getResponse(response);
    }

    /**
     * Lists versions of an RPSL object
     *
     * @param source RIPE or TEST
     * @param key    sought RPSL object
     * @return all updates of given RPSL object
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/versions/{source}/{key:.*}")
    public Response listVersions(
            @Context HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("key") final String key) {
        final Query query = Query.parse(String.format("--list-versions %s", key));
        return handleQuery(query, source, key, request, null);
    }

    /**
     * Show a specific version of an RPSL object
     *
     * @param source  RIPE or TEST
     * @param version sought version
     * @param key     sought RPSL object
     * @return The version of the RPSL object asked for
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/version/{source}/{version}/{key:.*}")
    public Response showVersion(
            @Context HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("version") final int version,
            @PathParam("key") final String key) {

        final Query query = Query.parse(String.format("" +
                "--show-version %s %s",
                version,
                key));
        return handleQuery(query, source, key, request, null);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/search")
    public Response search(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String queryString,
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") String flags) {
        return doSearch(request, queryString, sources, inverseAttributes, types, flags, false);
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/grs-search")
    public Response grssearch(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String queryString,
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") String flags) {
        return doSearch(request, queryString, sources, inverseAttributes, types, flags, true);
    }

    private Response doSearch(final HttpServletRequest request, final String queryString, final Set<String> sources, final Set<String> inverseAttributes, final Set<String> types, final String flags, final boolean isGrsExpected) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("Argument 'source' is missing, you have to specify a valid RIR source for your search request");
        }

        for (final String source : sources) {
            if (isGrsExpected) {
                if (!sourceContext.getGrsSourceNames().contains(ciString(source))) {
                    throw new IllegalArgumentException(String.format("The given grs source id: '%s' is not valid", source));
                }
            } else if (!sourceContext.getCurrentSource().getName().contains(ciString(source))) {
                throw new IllegalArgumentException(String.format("The given source id: '%s' is not valid", source));
            }
        }

        if (StringUtils.isNotBlank(flags)) {
            final CharacterIterator charIterator = new StringCharacterIterator(flags);
            for (char flag = charIterator.first(); flag != CharacterIterator.DONE; flag = charIterator.next()) {
                if (NOT_ALLOWED_SEARCH_FLAGS.contains(flag)) {
                    throw new IllegalArgumentException(String.format("The flag: %s is not valid.", flag));
                }
            }
        }

        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s",
                QueryFlag.SOURCES.getLongFlag(), JOINER.join(sources),
                (types == null || types.isEmpty()) ? "" : QueryFlag.SELECT_TYPES.getLongFlag(), JOINER.join(types),
                (inverseAttributes == null || inverseAttributes.isEmpty()) ? "" : QueryFlag.INVERSE.getLongFlag(), JOINER.join(inverseAttributes),
                (flags == null) ? "" : "-" + flags,
                (queryString == null ? "" : queryString)));

        final Parameters parameters = new Parameters();
        parameters.setSources(sources);
        parameters.setQueryStrings(queryString);
        parameters.setInverseLookup(inverseAttributes);
        parameters.setTypeFilters(types);
        parameters.setFlags(flags == null ? Collections.<String>emptySet() : Sets.newHashSet(flags.split("(?!^)")));

        return handleQuery(query, JOINER.join(sources), queryString, request, parameters);
    }

    /**
     * Finds tags for given RPSL object
     * <p/>
     * Example:
     * http://apps.db.ripe.net/whois/tags/RIPE/TEST-DBM?include=foo&include=bar&exclude=boo
     *
     * @param source  TEST or RIPE
     * @param key     sought RPSL object
     * @param include only show RPSL objects that have these tags. Can be multiple.
     * @param exclude only show RPSL objects that <i>do not</i> have these tags. Can be multiple.
     * @return returns the RPSL object(s) asked for with their respective tags
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/tags/{source}/{key:.*}")
    public Response tagSearch(
            @Context HttpServletRequest request,
            @PathParam("source") String source,
            @PathParam("key") String key,
            @QueryParam("include") Set<String> include,
            @QueryParam("exclude") Set<String> exclude) {

        final Query query = Query.parse(String.format("%s %s %s %s %s %s",
                key,
                QueryFlag.SHOW_TAGINFO.getLongFlag(),
                (include == null || include.isEmpty()) ? "" :
                        QueryFlag.FILTER_TAG_INCLUDE.getLongFlag(), JOINER.join(include),

                (exclude == null || exclude.isEmpty()) ? "" :
                        QueryFlag.FILTER_TAG_EXCLUDE.getLongFlag(), JOINER.join(exclude)));

        return handleQuery(query, source, key, request, null);
    }

    private UpdateResponse performUpdate(final Origin origin, final Update update, final String content, final Keyword keyword, final String source) {
        if (!sourceMatchesContext(source)) {
            throw new IllegalArgumentException("Invalid source specified: " + source);
        }

        loggerContext.init(getRequestId(origin.getFrom()));
        try {
            final UpdateContext updateContext = new UpdateContext(loggerContext);
            final boolean notificationsEnabled = true;

            final UpdateRequest updateRequest = new UpdateRequest(
                    origin,
                    keyword,
                    content,
                    Lists.newArrayList(update),
                    notificationsEnabled);

            final UpdateResponse response = updateRequestHandler.handle(updateRequest, updateContext);

            if (updateContext.getStatus(update) == UpdateStatus.FAILED_AUTHENTICATION) {
                return new UpdateResponse(UpdateStatus.FAILED_AUTHENTICATION, response.getResponse());
            }

            return response;
        } finally {
            loggerContext.remove();
        }
    }

    private String createContent(final RpslObject rpslObject, final List<String> passwords, final String deleteReason) {
        final StringBuilder builder = new StringBuilder();
        builder.append(rpslObject.toString());

        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }

        if (deleteReason != null) {
            builder.append("delete: ");
            builder.append(deleteReason);
            builder.append("\n\n");
        }

        for (final String password : passwords) {
            builder.append("password: ");
            builder.append(password);
            builder.append('\n');
        }

        return builder.toString();
    }

    private Update createUpdate(final RpslObject rpslObject, final RpslObjectUpdateInfo objectInfo, final List<String> passwords, final String deleteReason) {
        return new Update(
                createParagraph(rpslObject, passwords),
                deleteReason != null ? Operation.DELETE : Operation.UNSPECIFIED,
                deleteReason != null ? Lists.newArrayList(deleteReason) : null,
                rpslObject,
                objectInfo);
    }

    private Paragraph createParagraph(final RpslObject rpslObject, final List<String> passwords) {
        final Set<PasswordCredential> passwordCredentials = Sets.newHashSet();
        for (String password : passwords) {
            passwordCredentials.add(new PasswordCredential(password));
        }

        return new Paragraph(rpslObject.toString(), new Credentials(passwordCredentials));
    }

    private Origin createOrigin(final HttpServletRequest request) {
        return new WhoisRestApi(dateTimeProvider, request.getRemoteAddr());
    }

    private WhoisResources createWhoisResources(final HttpServletRequest request, final RpslObject rpslObject) {
        final WhoisResources whoisResources = new WhoisResources();
        whoisResources.setService("lookup");
        whoisResources.setWhoisObjects(Lists.newArrayList(WhoisObjectMapper.map(rpslObject)));
        whoisResources.setLink(new Link("locator", RestServiceHelper.getRequestURL(request)));
        return whoisResources;
    }

    private RpslObject getSubmittedObject(final WhoisResources whoisResources) {
        if (whoisResources.getWhoisObjects().isEmpty() || whoisResources.getWhoisObjects().size() > 1) {
            throw new IllegalArgumentException("Expected a single RPSL object");
        }
        return WhoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    private Response getResponse(final UpdateResponse updateResponse) {
        int status;
        switch (updateResponse.getStatus()) {
            case FAILED: {
                status = HttpServletResponse.SC_BAD_REQUEST;

                final String errors = findAllErrors(updateResponse);
                if (!errors.isEmpty()) {
                    if (errors.contains("Enforced new keyword specified, but the object already exists")) {
                        status = HttpServletResponse.SC_CONFLICT;
                    }
                    return Response.status(status).entity(errors).build();
                }

                break;
            }
            case EXCEPTION:
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                break;
            case FAILED_AUTHENTICATION:
                status = HttpServletResponse.SC_UNAUTHORIZED;
                final String errors = findAllErrors(updateResponse);
                if (!errors.isEmpty()) {
                    return Response.status(status).entity(errors).build();
                }
                break;
            default:
                status = HttpServletResponse.SC_OK;
        }

        return Response.status(status).build();
    }

    private String getRequestId(final String remoteAddress) {
        return "rest_" + remoteAddress + "_" + System.nanoTime();
    }

    private boolean sourceMatchesContext(final String source) {
        return (source != null) && sourceContext.getCurrentSource().getName().equals(CIString.ciString(source));
    }

    private String findAllErrors(final UpdateResponse updateResponse) {
        final StringBuilder builder = new StringBuilder();
        final Matcher matcher = UPDATE_RESPONSE_ERRORS.matcher(updateResponse.getResponse());
        while (matcher.find()) {
            builder.append(matcher.group(1).replaceAll("[\\n ]+", " "));
            builder.append('\n');
        }
        return builder.toString();
    }
}
