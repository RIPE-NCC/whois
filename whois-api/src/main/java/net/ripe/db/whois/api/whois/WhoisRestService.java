package net.ripe.db.whois.api.whois;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
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
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
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
import static net.ripe.db.whois.query.query.QueryFlag.*;

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
    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;

    private static final Pattern UPDATE_RESPONSE_ERRORS = Pattern.compile("(?m)^\\*\\*\\*Error:\\s*((.*)(\\n[ ]+.*)*)$");
    private static final Joiner JOINER = Joiner.on(",");
    private static final Set<String> NOT_ALLOWED_SEARCH_QUERY_FLAGS = Sets.newHashSet();

    @Autowired
    public WhoisRestService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        this.dateTimeProvider = dateTimeProvider;
        this.updateRequestHandler = updateRequestHandler;
        this.loggerContext = loggerContext;
        this.rpslObjectDao = rpslObjectDao;
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        initDisallowedQueryFlagCache();
    }

    private void initDisallowedQueryFlagCache() {
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(TEMPLATE.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(VERBOSE.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(CLIENT.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(NO_GROUPING.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(SOURCES.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(NO_TAG_INFO.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(SHOW_TAG_INFO.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(ALL_SOURCES.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(LIST_SOURCES_OR_VERSION.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(LIST_SOURCES.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(DIFF_VERSIONS.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(LIST_VERSIONS.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(SHOW_VERSION.getFlags());
        NOT_ALLOWED_SEARCH_QUERY_FLAGS.addAll(PERSISTENT_CONNECTION.getFlags());
    }

    /**
     * <p><div>The lookup service returns a single object specified by the source, object type and primary-key arguments.</div>
     * <p/>
     * <p><div>Example query:</div>
     * http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT</p>
     * <p/>
     * <p><div>Example XML response:</div>
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;
     * &lt;whois-resources service="lookup" xsi:noNamespaceSchemaLocation="http://apps.db.ripe.net/whois-beta/xsd/whois-resources.xsd"
     * xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
     *  &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT"/&gt;
     *  &lt;objects&gt;
     *    &lt;object type="mntner"&gt;
     *      &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT"/&gt;
     *      &lt;source id="ripe"/&gt;
     *      &lt;primary-key&gt;
     *        &lt;attribute name="mntner" value="RIPE-DBM-MNT"/&gt;
     *      &lt;/primary-key&gt;
     *      &lt;attributes&gt;
     *        &lt;attribute name="mntner" value="RIPE-DBM-MNT"/&gt;
     *        &lt;attribute name="descr" value="Mntner for RIPE DBM objects."/&gt;
     *        &lt;attribute name="admin-c" value="RD132-RIPE" referenced-type="person-role"&gt;
     *          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/person-role/RD132-RIPE"/&gt;
     *        &lt;/attribute&gt;
     *        &lt;attribute name="tech-c" value="RD132-RIPE" referenced-type="person-role"&gt;
     *          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/person-role/RD132-RIPE"/&gt;
     *        &lt;/attribute&gt;
     *        &lt;attribute name="org" value="ORG-NCC1-RIPE" referenced-type="organisation"&gt;
     *          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/organisation/ORG-NCC1-RIPE"/&gt;
     *        &lt;/attribute&gt;
     *        &lt;attribute name="auth" value="PGPKEY-1290F9D2" referenced-type="key-cert"&gt;
     *          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/key-cert/PGPKEY-1290F9D2"/&gt;
     *        &lt;/attribute&gt;
     *        &lt;attribute name="auth" value="MD5-PW" comment="Filtered"/&gt;
     *        &lt;attribute name="mnt-by" value="RIPE-DBM-MNT" referenced-type="mntner"&gt;
     *          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT"/&gt;
     *        &lt;/attribute&gt;
     *        &lt;attribute name="changed" value="hostmaster@ripe.net 20050830"/&gt;
     *        &lt;attribute name="source" value="RIPE" comment="Filtered"/&gt;
     *      &lt;/attributes&gt;
     *    &lt;/object&gt;
     *  &lt;/objects&gt;
     * &lt;/whois-resources&gt;
     * </pre>
     * </p>
     * <p/>
     * <p><div>Example JSON response:</div>
     * <pre>
     * {
     * "whois-resources": {
     *    "objects": {
     *      "object": {
     *        "type": "mntner",
     *        "link": {
     *          "xlink:type": "locator",
     *          "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT"
     *        },
     *        "source": {
     *          "id": "ripe"
     *        },
     *        "primary-key": {
     *          "attribute": [
     *            {
     *              "name": "mntner",
     *              "value": "RIPE-DBM-MNT"
     *            }
     *          ]
     *        },
     *        "attributes": {
     *          "attribute": [
     *            {
     *              "name": "mntner",
     *              "value": "RIPE-DBM-MNT"
     *            },
     *            {
     *              "name": "descr",
     *              "value": "Mntner for RIPE DBM objects."
     *            },
     *            {
     *              "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/person-role/RD132-RIPE"
     *              },
     *              "name": "admin-c",
     *              "value": "RD132-RIPE",
     *              "referenced-type": "person-role"
     *            },
     *            {
     *              "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/person-role/RD132-RIPE"
     *              },
     *              "name": "tech-c",
     *              "value": "RD132-RIPE",
     *              "referenced-type": "person-role"
     *            },
     *            {
     *              "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/organisation/ORG-NCC1-RIPE"
     *              },
     *              "name": "organisation",
     *              "value": "ORG-NCC1-RIPE",
     *              "referenced-type": "organisation"
     *            },
     *            {
     *              "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/key-cert/PGPKEY-1290F9D2"
     *              },
     *              "name": "auth",
     *              "value": "PGPKEY-1290F9D2",
     *              "referenced-type": "key-cert"
     *            },
     *            {
     *              "name": "auth",
     *              "value": "MD5-PW",
     *              "comment": "Filtered"
     *            },
     *            {
     *              "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/OWNER-MNT"
     *              },
     *              "name": "mnt-by",
     *              "value": "RIPE-DBM-MNT",
     *              "referenced-type": "mntner"
     *            },
     *            {
     *              "name": "changed",
     *              "value": "hostmaster@ripe.net 20050830"
     *            }
     *            {
     *              "name": "source",
     *              "value": "RIPE",
     *              "comment": "Filtered"
     *            }
     *          ]
     *        }
     *      }
     *    }
     *  }
     * }
     * </pre>
     * </p>
     *
     * @param source     Source name (RIPE or TEST).
     * @param objectType Object type of given object.
     * @param key        Primary key of the given object.
     * @return Returns the lookup result.
     */
    @GET
    @TypeHint(WhoisResources.class)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @Path("/lookup/{source}/{objectType}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Object found for the specified key"),
            @ResponseCode(code = 404, condition = "The query didn't return any valid object")
    })
    public Response lookup(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {
        return lookupObject(request, source, objectType, key);
    }

    private Response lookupObject(
            final HttpServletRequest request,
            final String source,
            final String objectTypeString,
            final String key) {

        checkForInvalidSource(source);

        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s %s",
                QueryFlag.EXACT.getLongFlag(),
                QueryFlag.NO_GROUPING.getLongFlag(),
                QueryFlag.NO_REFERENCED.getLongFlag(),
                QueryFlag.SOURCES.getLongFlag(),
                source,
                QueryFlag.SELECT_TYPES.getLongFlag(),
                objectTypeString,
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                key));

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

    // TODO: [AH] refactor this spaghetti
    private Response handleVersionQuery(final Query query, final String source, final String key, final InetAddress remoteAddress, final int contextId) {
        final ApiResponseHandlerVersions apiResponseHandlerVersions = new ApiResponseHandlerVersions();
        queryHandler.streamResults(query, remoteAddress, contextId, apiResponseHandlerVersions);

        final VersionWithRpslResponseObject versionResponseObject = apiResponseHandlerVersions.getVersionWithRpslResponseObject();
        final List<DeletedVersionResponseObject> deleted = apiResponseHandlerVersions.getDeletedObjects();
        final List<VersionResponseObject> versions = apiResponseHandlerVersions.getVersionObjects();

        if (versionResponseObject == null && versions.isEmpty()) {
            if (deleted.isEmpty() || query.isObjectVersion()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
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
                final Queue<RpslObject> rpslObjectQueue = new ArrayDeque<>(1);
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
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }
                } catch (QueryException e) {
                    if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                        throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
                    } else {
                        throw e;
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

    /**
     * <p>Create an object in the RIPE database.</p>
     * <p/>
     * <p>Example XML request object:
     * <pre>&lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;
     * &lt;whois-resources&gt;
     * &lt;objects&gt;
     * &lt;object type="person"&gt;
     * &lt;source id="test"/&gt;
     * &lt;attributes&gt;
     * &lt;attribute name="person" value="Pauleth Palthen"/&gt;
     * &lt;attribute name="address" value="Singel 258"/&gt;
     * &lt;attribute name="phone" value="+31-1234567890"/&gt;
     * &lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     * &lt;attribute name="mnt-by" value="PP-MNT" /&gt;
     * &lt;attribute name="nic-hdl" value="AUTO-1" /&gt;
     * &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;
     * &lt;attribute name="source" value="TEST"/&gt;
     * &lt;/attributes&gt;
     * &lt;/object&gt;
     * &lt;/objects&gt;
     * &lt;/whois-resources&gt;</pre></p>
     * <p/>
     * <p/>
     * <p><div>Example JSON request object:</div>
     * <pre>
     *   {
     *    "whois-resources": {
     *      "objects": {
     *        "object": {
     *          "type": "person",
     *          "link": {
     *            "xlink:type": "locator",
     *            "xlink:href": "http:\/\/apps.db.ripe.net\/whois-beta\/lookup\/test\/person\/PP1-TEST"
     *          },
     *          "source": {
     *            "id": "test"
     *          },
     *          "primary-key": {
     *            "attribute": [
     *              {
     *                "name": "nic-hdl",
     *                "value": "PP1-TEST"
     *              }
     *            ]
     *          },
     *          "attributes": {
     *            "attribute": [
     *              {
     *                "name": "person",
     *                "value": "Pauleth Palthen"
     *              },
     *              {
     *                "name": "address",
     *                "value": "Singel 258"
     *              },
     *              {
     *                "name": "phone",
     *                "value": "+31-1234567890"
     *              },
     *              {
     *                "link": {
     *                  "xlink:type": "locator",
     *                  "xlink:href": "http:\/\/apps.db.ripe.net\/whois-beta\/lookup\/test\/mntner\/OWNER-MNT"
     *                },
     *                "name": "mnt-by",
     *                "value": "OWNER-MNT",
     *                "referenced-type": "mntner"
     *              },
     *              {
     *                "name": "nic-hdl",
     *                "value": "PP1-TEST"
     *              },
     *              {
     *                "name": "remarks",
     *                "value": "remark"
     *              },
     *              {
     *                "name": "source",
     *                "value": "TEST",
     *                "comment": "Filtered"
     *              }
     *            ]
     *          }
     *        }
     *      }
     *    }
     *  }
     *
     * </pre></p>
     *
     * @param resources Request body.
     * @param source    RIPE or TEST.
     * @param passwords One or more password values.
     * @return The response body will be empty.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @TypeHint(WhoisResources.class)
    @Path("/create/{source}")
    @StatusCodes({
            @ResponseCode(code = 201, condition = "Successful create"),
            @ResponseCode(code = 400, condition = "Incorrect value for source"),
    })
    public Response create(
            final WhoisResources resources,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @QueryParam(value = "password") final List<String> passwords) {

        final RpslObject submittedObject = getSubmittedObject(resources);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(submittedObject, passwords, null),
                createContent(submittedObject, passwords, null),
                Keyword.NEW,
                source);

        return getResponse(response);
    }

    // TODO: [AH] drop this a couple of weeks after deployment

    /**
     * Create request without including source in URL is no longer allowed - use <a href="path__create_-source-.html">/create/source</a> instead.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Path("/create")
    public Response create() {
        // Source needs to be included to be consistent with the other CRUD operations, and also
        // to allow mod-proxy to redirect requests to the correct instance.
        throw new IllegalArgumentException("Source must be specified in URL");
    }

    /**
     * <p>A successful update request replaces all of an object attributes with the new set of attributes described in
     * the request. </p>
     * <p/>
     * <p>Example of an XML request body:
     * <pre>&lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;
     * &lt;whois-resources&gt;
     * &lt;objects&gt;
     * &lt;object type="person"&gt;
     * &lt;source id="test"/&gt;
     * &lt;attributes&gt;
     * &lt;attribute name="person" value="Pauleth Palthen"/&gt;
     * &lt;attribute name="address" value="Singel 123"/&gt;
     * &lt;attribute name="phone" value="+31-0987654321"/&gt;
     * &lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     * &lt;attribute name="mnt-by" value="PP-MNT" /&gt;
     * &lt;attribute name="nic-hdl" value="PP16-TEST" /&gt;
     * &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;
     * &lt;attribute name="source" value="TEST"/&gt;
     * &lt;/attributes&gt;
     * &lt;/object&gt;
     * &lt;/objects&gt;
     * &lt;/whois-resources&gt;</pre>
     * </p>
     * <p/>
     * <p/>
     * <p>Example of success XML response body:</p>
     * <pre>&lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;
     * &lt;whois-resources service="lookup" xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person/PP3-TEST"/&gt;
     * &lt;objects&gt;
     * &lt;object type="person"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person/PP16-TEST"/&gt;
     * &lt;source id="test"/&gt;
     * &lt;primary-key&gt;
     * &lt;attribute name="nic-hdl" value="PP16-TEST"/&gt;
     * &lt;/primary-key&gt;
     * &lt;attributes&gt;
     * &lt;attribute name="person" value="Pauleth Palthen"/&gt;
     * &lt;attribute name="address" value="Singel 123"/&gt;
     * &lt;attribute name="phone" value="+31-0987654321"/&gt;
     * &lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     * &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;
     * &lt;attribute name="mnt-by" value="PP-MNT" referenced-type="mntner"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/mntner/PP-MNT"/&gt;
     * &lt;/attribute&gt;
     * &lt;attribute name="nic-hdl" value="PP16-TEST"/&gt;
     * &lt;attribute name="source" value="TEST"/&gt;
     * &lt;/attributes&gt;
     * &lt;/object&gt;
     * &lt;/objects&gt;</pre>
     * <p/>
     * <p>Example of a JSON request body:</p>
     * <pre>
     *   {
     *    "objects" : {
     *        "object" : [ {
     *          "source" : {
     *            "id" : "test"
     *          },
     *          "attributes" : {
     *            "attribute" : [
     *              {"name":"mntner", "value":"OWNER-MNT"},
     *              {"name":"descr", "value":"description"},
     *              {"name":"admin-c", "value":"TP1-TEST"},
     *              {"name":"upd-to", "value":"noreply@ripe.net"},
     *              {"name":"auth", "value":"MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/"},
     *              {"name":"mnt-by", "value":"OWNER-MNT"},
     *              {"name":"referral-by", "value":"OWNER-MNT"},
     *              {"name":"changed", "value":"dbtest@ripe.net 20120101"},
     *              {"name":"source", "value":"TEST"}
     *          ] }
     *       }]
     *     }
     *  }
     * </pre>
     * <p/>
     * <p>Example of a JSON success response:</p>
     * <pre>
     * {
     *    "whois-resources" : {
     *      "service" : "lookup",
     *      "link" : {
     *        "xlink:type" : "locator",
     *        "xlink:href" : "http://localhost:51761/whois-beta/update/test/mntner/OWNER-MNT?password=test"
     *      },
     *      "objects" : {
     *        "object" : [ {
     *          "type" : "mntner",
     *          "link" : {
     *            "xlink:type" : "locator",
     *            "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *          },
     *          "source" : {
     *            "id" : "test"
     *          },
     *          "primary-key" : {
     *            "attribute" : [ {
     *              "name" : "mntner",
     *              "value" : "OWNER-MNT"
     *            } ]
     *          },
     *          "attributes" : {
     *            "attribute" : [ {
     *              "name" : "mntner",
     *              "value" : "OWNER-MNT"
     *            }, {
     *              "name" : "descr",
     *              "value" : "description"
     *            }, {
     *              "link" : {
     *                "xlink:type" : "locator",
     *                "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"
     *              },
     *              "name" : "admin-c",
     *              "value" : "TP1-TEST",
     *              "referenced-type" : "person-role"
     *            }, {
     *              "name" : "upd-to",
     *              "value" : "noreply@ripe.net"
     *            }, {
     *              "name" : "auth",
     *              "value" : "MD5-PW",
     *              "comment" : "Filtered"
     *            }, {
     *              "link" : {
     *                "xlink:type" : "locator",
     *                "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *              },
     *              "name" : "mnt-by",
     *              "value" : "OWNER-MNT",
     *              "referenced-type" : "mntner"
     *            }, {
     *              "link" : {
     *                "xlink:type" : "locator",
     *                "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *              },
     *              "name" : "referral-by",
     *              "value" : "OWNER-MNT",
     *              "referenced-type" : "mntner"
     *            }, {
     *              "name" : "changed",
     *              "value" : "dbtest@ripe.net 20120101"
     *            }, {
     *              "name" : "source",
     *              "value" : "TEST",
     *              "comment" : "Filtered"
     *            } ]
     *          }
     *        } ]
     *      }
     *    }
     *  }
     *  </pre>
     *
     * @param resource   Request body.
     * @param source     RIPE or TEST.
     * @param objectType Object type for given object.
     * @param key        Primary key of the given object.
     * @param passwords  One or more password values.
     * @return Response in appropriate format.
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @TypeHint(WhoisResources.class)
    @Path("/update/{source}/{objectType}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Successful modification"),
            @ResponseCode(code = 400, condition = "Incorrect value for source, objectType or key "),
            @ResponseCode(code = 401, condition = "Incorrect password"),
            @ResponseCode(code = 404, condition = "Object not found")
    })
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
                createUpdate(submittedObject, passwords, null),
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

    /**
     * <p>The modify service supports adding, removing or replacing individual attributes :</p>
     * <ul>
     * <li>add new attributes</li>
     * <li>add new attributes starting at index N (index starts at zero)</li>
     * <li>replace all attributes of a certain type</li>
     * <li>remove all attributes of a certain type</li>
     * <li>remove the Nth attribute</li>
     * </ul>
     * <p/>
     * <p/>
     * <p><div>Examples</div><p/>
     * <ul>
     * <li><div>Add new attributes XML request:</div>
     * <pre>
     *  &lt;whois-modify&gt;
     *      &lt;add&gt;
     *          &lt;attributes&gt;
     *              &lt;attribute name="phone" value="+31 20 535 4444"/&gt;
     *              &lt;attribute name="fax-no" value="+31 20 535 4445"/&gt;
     *          &lt;/attributes&gt;
     *      &lt;/add&gt;
     *  &lt;/whois-modify&gt;
     *  </pre>
     *
     * <p>Example of XML response:</p>
     * <pre>
     *     &lt;?xml version="1.0" encoding="UTF-8" standalone="no" ?&gt;
     *      &lt;whois-resources service="lookup" xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
     *      &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person/PP16-TEST"/&gt;
     *      &lt;objects&gt;
     *      &lt;object type="person"&gt;
     *      &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person/PP16-TEST"/&gt;
     *      &lt;source id="test"/&gt;
     *      &lt;primary-key&gt;
     *      &lt;attribute name="nic-hdl" value="PP16-TEST"/&gt;
     *      &lt;/primary-key&gt;
     *      &lt;attributes&gt;
     *      &lt;attribute name="person" value="Pauleth Palthen"/&gt;
     *      &lt;attribute name="address" value="RIPE Network Coordination Centre (NCC)"/&gt;
     *      &lt;attribute name="address" value="P.O. Box 10096"/&gt;
     *      &lt;attribute name="address" value="1001 EB Amsterdam"/&gt;
     *      &lt;attribute name="address" value="The Netherlands"/&gt;
     *      &lt;attribute name="remarks" value="This is our new address!"/&gt;
     *      &lt;attribute name="phone" value="+31 20 535 4444"/&gt;
     *      &lt;attribute name="fax-no" value="+31 20 535 4445"/&gt;
     *      &lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     *      &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;
     *      &lt;attribute name="mnt-by" value="PP-MNT" referenced-type="mntner"&gt;
     *      &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/mntner/PP-MNT"/&gt;
     *      &lt;/attribute&gt;
     *      &lt;attribute name="nic-hdl" value="PP16-TEST"/&gt;
     *      &lt;attribute name="source" value="TEST"/&gt;
     *      &lt;/attributes&gt;
     *      &lt;/object&gt;
     *      &lt;/objects&gt;
     *      &lt;/whois-resources&gt;
     * </pre>
     * <p/>
     * <div>Example of JSON request:</div>
     * <pre>
     *   {
     *   "add" : {
     *     "attributes" : {
     *           "attribute" : [
     *                 {"name" : "phone", "value" : "+31 20 535 4444"},
     *                 {"name" : "fax-no", "value" : "+31 20 535 4445"}
     *         ] }
     *   }
     *  }
     *  </pre>
     *
     * <p>Example of JSON response:</p>
     * <pre>
     *   {
     *   "whois-resources" : {
     *     "service" : "lookup",
     *     "link" : {
     *       "xlink:type" : "locator",
     *       "xlink:href" : "http://apps.db.ripe.net:64499/whois-beta/modify/test/person/PP1-TEST?password=test"
     *     },
     *     "objects" : {
     *       "object" : [ {
     *         "type" : "person",
     *         "link" : {
     *           "xlink:type" : "locator",
     *           "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/person/PP1-TEST"
     *         },
     *         "source" : {
     *           "id" : "test"
     *         },
     *         "primary-key" : {
     *           "attribute" : [ {
     *             "name" : "nic-hdl",
     *             "value" : "PP1-TEST"
     *           } ]
     *         },
     *         "attributes" : {
     *           "attribute" : [ {
     *             "name" : "person",
     *             "value" : "Pauleth Palthen"
     *           }, {
     *             "name" : "address",
     *             "value" : "Singel 258"
     *           }, {
     *             "name" : "phone",
     *             "value" : "+31-1234567890"
     *           }, {
     *             "name" : "phone",
     *             "value" : "+31 20 535 4444"
     *           }, {
     *             "name" : "fax-no",
     *             "value" : "+31 20 535 4445"
     *           }, {
     *             "name" : "e-mail",
     *             "value" : "noreply@ripe.net"
     *           }, {
     *             "name" : "nic-hdl",
     *             "value" : "PP1-TEST"
     *           }, {
     *             "name" : "remarks",
     *             "value" : "remark"
     *           }, {
     *             "link" : {
     *               "xlink:type" : "locator",
     *               "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *             },
     *             "name" : "mnt-by",
     *             "value" : "OWNER-MNT",
     *             "referenced-type" : "mntner"
     *           }, {
     *             "name" : "changed",
     *             "value" : "noreply@ripe.net 20120101"
     *           }, {
     *             "name" : "source",
     *             "value" : "TEST"
     *           } ]
     *         }
     *       } ]
     *     }
     *   }
     *  }
     *  </pre>
     * </li>
     *
     * <li><div>Add new attributes starting at index N:</div>
     * <pre>
     *  &lt;whois-modify&gt;
     *      &lt;add index="6"&gt;
     *          &lt;attributes&gt;
     *              &lt;attribute name="remarks" value="These remark lines will be added"/&gt;
     *              &lt;attribute name="remarks" value="starting from index 6 (line 7) !"/&gt;
     *          &lt;/attributes&gt;
     *      &lt;/add&gt;
     *  &lt;/whois-modify&gt;
     *  </pre></li>
     *
     * <li><div>Replace all attributes of a certain type</div>
     * <pre>
     *       &lt;whois-modify&gt;
     *          &lt;replace attribute-type="address"&gt;
     *              &lt;attributes&gt;
     *              &lt;attribute name="address" value="RIPE Network Coordination Centre (NCC)"/&gt;
     *                  &lt;attribute name="address" value="P.O. Box 10096"/&gt;
     *                  &lt;attribute name="address" value="1001 EB Amsterdam"/&gt;
     *                  &lt;attribute name="address" value="The Netherlands"/&gt;
     *                  &lt;attribute name="remarks" value="This is our new address!"/&gt;
     *              &lt;/attributes&gt;
     *          &lt;/replace&gt;
     *      &lt;/whois-modify&gt;
     *  </pre>
     * </li>
     *
     * <li><div>Remove all attributes of a certain type:</div>
     * <pre>
     *      &lt;whois-modify&gt;
     *          &lt;remove attribute-type="remarks"/&gt;
     *      &lt;/whois-modify&gt;
     *  </pre>
     * </li>
     *
     * <li><div>Remove the Nth attribute:</div>
     * <pre>
     *          &lt;whois-modify&gt;
     *              &lt;remove index="6"/&gt;
     *          &lt;/whois-modify&gt;
     *      </pre>
     * </li>
     *
     * </ul></p>
     *
     * @param whoisModify Request body.
     * @param source      RIPE or TEST.
     * @param objectType  Object type of given object.
     * @param key         Primary key of given object.
     * @param passwords   One or more password values.
     * @return Returns the modified object.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @TypeHint(WhoisResources.class)
    @Path("/modify/{source}/{objectType}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 204, condition = "Successful modification"),
            @ResponseCode(code = 400, condition = "Incorrect value for source, objectType or key (or when applicable, index)"),
            @ResponseCode(code = 401, condition = "Incorrect password"),
            @ResponseCode(code = 404, condition = "Object not found")
    })
    public Response modify(
            final WhoisModify whoisModify,
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key,
            @QueryParam(value = "password") final List<String> passwords) {

        RpslObject originalObject = rpslObjectDao.getByKey(ObjectType.getByName(objectType), key);
        RpslObject updatedObject = modifyRpslObject(originalObject, whoisModify);

        final UpdateResponse response = performUpdate(
                createOrigin(request),
                createUpdate(updatedObject, passwords, null),
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

    /**
     * <p>The delete service deletes a single object from the database.</p>
     * <p>The HTTP Request body must be empty.</p>
     * <p/>
     * <div>Example using CURL:</div>
     * <pre>
     * curl -X DELETE https://apps.db.ripe.net/whois-beta/delete/test/person/pp16-test?password=123
     * </pre>
     * <p/>
     *
     * @param source     RIPE or TEST. Mandatory.
     * @param objectType Object type of given object. Mandatory.
     * @param key        Primary key for given object. Mandatory.
     * @param reason     Reason for deleting given object. Optional.
     * @param passwords  One or more password values. Mandatory.
     * @return Returns only HTTP headers
     */
    @DELETE
    @Path("/delete/{source}/{objectType}/{key:.*}")
    @Produces({})
    @StatusCodes({
            @ResponseCode(code = 204, condition = "Successful delete"),
            @ResponseCode(code = 400, condition = "Incorrect value for source, objectType or key "),
            @ResponseCode(code = 401, condition = "Incorrect password"),
            @ResponseCode(code = 404, condition = "Object not found")
    })
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
                createUpdate(originalObject, passwords, reason),
                createContent(originalObject, passwords, reason),
                Keyword.NONE,
                source);

        return getResponse(response);
    }

    /**
     * <p>Lists versions of an RPSL object</p>
     * <p/>
     * <div>Example query:</div>
     * <pre>
     *  http://apps.db.ripe.net/whois-beta/versions/TEST/AS102
     * </pre>
     * <p/>
     * <div>Example response in XML:</div>
     * <pre>
     *  &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * &lt;whois-resources xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *                  xsi:noNamespaceSchemaLocation="http://apps.db.ripe.net/whois-beta/xsd/whois-resources.xsd"&gt;
     *     &lt;versions type="aut-num" key="AS102"&gt;
     *         &lt;source id="TEST"/&gt;
     *         &lt;version deleted="2013-06-27 13:22"/&gt;
     *         &lt;version&gt;
     *             &lt;revision&gt;1&lt;/revision&gt;
     *             &lt;date&gt;2013-06-27 13:22&lt;/date&gt;
     *             &lt;operation&gt;ADD/UPD&lt;/operation&gt;
     *         &lt;/version&gt;
     *         &lt;version&gt;
     *              &lt;revision&gt;2&lt;/revision&gt;
     *              &lt;date&gt;2013-06-27 13:22&lt;/date&gt;
     *              &lt;operation&gt;ADD/UPD&lt;/operation&gt;
     *         &lt;/version&gt;
     *     &lt;/versions&gt;
     * &lt;/whois-resources&gt;
     * </pre>
     * <p/>
     * <div>Example response in JSON:</div>
     * <pre>
     * {
     * "whois-resources" : {
     *  "versions" : {
     *      "source" : {
     *          "id" : "TEST"
     *      },
     *      "type" : "aut-num",
     *      "key" : "AS102",
     *      "version" : [ {
     *          "deleted" : "2013-06-27 14:02"
     *          }, {
     *          "deleted" : null,
     *          "revision" : 1,
     *          "date" : "2013-06-27 14:02",
     *          "operation" : "ADD/UPD"
     *          }, {
     *          "deleted" : null,
     *          "revision" : 2,
     *          "date" : "2013-06-27 14:02",
     *          "operation" : "ADD/UPD"
     *          } ]
     *      }
     *  }
     * }
     * </pre>
     *
     * @param source RIPE or TEST
     * @param key    sought RPSL object
     * @return Returns all updates of given RPSL object
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/versions/{source}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Versions were found"),
            @ResponseCode(code = 400, condition = "Illegal input - incorrect source or key"),
            @ResponseCode(code = 404, condition = "No versions found")
    })
    public Response listVersions(
            @Context HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("key") final String key) {
        final Query query = Query.parse(String.format("--list-versions %s", key));
        return handleQuery(query, source, key, request, null);
    }

    /**
     * <p>Show a specific version of an RPSL object</p>
     * <p/>
     * <div>Example query:</div>
     * <pre>
     *  http://apps.db.ripe.net/whois-beta/version/TEST/2/AS102
     * </pre>
     * <p/>
     * <div>Example response in XML:</div>
     * <pre>
     *  &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * &lt;whois-resources xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:noNamespaceSchemaLocation="http://apps.db.ripe.net/whois-beta/xsd/whois-resources.xsd"&gt;
     * &lt;objects&gt;
     * &lt;object type="aut-num" version="1"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/aut-num/AS102"/&gt;
     * &lt;source id="test"/&gt;
     * &lt;primary-key&gt;
     * &lt;attribute name="aut-num" value="AS102"/&gt;
     * &lt;/primary-key&gt;
     * &lt;attributes&gt;
     * &lt;attribute name="aut-num" value="AS102"/&gt;
     * &lt;attribute name="as-name" value="End-User-2"/&gt;
     * &lt;attribute name="descr" value="description"/&gt;
     * &lt;attribute name="admin-c" value="TP1-TEST" referenced-type="person-role"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"/&gt;
     * &lt;/attribute&gt;
     * &lt;attribute name="tech-c" value="TP1-TEST" referenced-type="person-role"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"/&gt;
     * &lt;/attribute&gt;
     * &lt;attribute name="mnt-by" value="OWNER-MNT" referenced-type="mntner"&gt;
     * &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"/&gt;
     * &lt;/attribute&gt;
     * &lt;attribute name="source" value="TEST"/&gt;
     * &lt;/attributes&gt;
     * &lt;/object&gt;
     * &lt;/objects&gt;
     * &lt;/whois-resources&gt;
     * </pre>
     * <p/>
     * <div>Example response in JSON:</div>
     * <pre>
     *  {
     *   "whois-resources" : {
     *   "objects" : {
     *   "object" : [ {
     *   "type" : "aut-num",
     *   "link" : {
     *   "xlink:type" : "locator",
     *   "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/aut-num/AS102"
     *  },
     *  version" : 1,
     *  "source" : {
     *      "id" : "test"
     *  },
     *  "primary-key" : {
     *      "attribute" : [ {
     *          "name" : "aut-num",
     *          "value" : "AS102"
     *      } ]
     *  },
     *  "attributes" : {
     *      "attribute" : [ {
     *          "name" : "aut-num",
     *          "value" : "AS102"
     *          }, {
     *          "name" : "as-name",
     *          "value" : "End-User-2"
     *          }, {
     *          "name" : "descr",
     *          "value" : "description"
     *          }, {
     *          "link" : {
     *              "xlink:type" : "locator", "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"
     *          },
     *          "name" : "admin-c",
     *          "value" : "TP1-TEST",
     *          "referenced-type" : "person-role"
     *          }, {
     *          "link" : {
     *              "xlink:type" : "locator", "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"
     *          } ,
     *          "name" : "tech-c",
     *          "value" : "TP1-TEST",
     *          "referenced-type" : "person-role"
     *          } , {
     *          "link" : {
     *              "xlink:type" : "locator", "xlink:href" : "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *          },
     *          "name" : "mnt-by",
     *          "value" : "OWNER-MNT",
     *          "referenced-type" : "mntner"
     *          }, {
     *              "name" : "source",
     *              "value" : "TEST"
     *          } ]
     *          }
     *      } ]
     *    }
     *  }
     * }
     * * </pre>
     *
     * @param source  RIPE or TEST
     * @param version sought version
     * @param key     sought RPSL object
     * @return Returns the version of the RPSL object asked for
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/version/{source}/{version}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Version was found"),
            @ResponseCode(code = 400, condition = "Illegal input - incorrect source or key"),
            @ResponseCode(code = 404, condition = "Sought version not found")
    })
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

    /**
     * <p>The search interface resembles a standard Whois client query with the extra features of multi-registry client, multiple response styles that can be selected via content negotiation and with an extensible URL parameters schema.</p>
     * <p/>
     * <p>Query using multiple sources: It is possible to specify multiple sources for the same request. This will execute the request on all the specified sources. Queries are executed on the online Whois servers, not on mirrored data, so they return live objects directly from the trusted sources.
     * In case of system exception on any of the sources the client will get an appropriate error code in response.</p>
     * <p/>
     * <p><div>Examples:</div>
     * <ul>
     * <li><div>Valid inverse lookup query on an org value, filtering by inetnum:</div>
     * <pre>
     * http://apps.db.ripe.net/whois-beta/search?inverse-attribute=org&type-filter=inetnum&source=ripe&query-string=ORG-NCC1-RIPE
     * </pre>
     * </li>
     * <li><div>Search for objects of type organisation on the same query-string and specifying a preference for non recursion:</div>
     * <pre>
     * http://apps.db.ripe.net/whois-beta/search?inverse-attribute=org&flags=no-referenced&type-filter=inetnum&source=ripe&query-string=ORG-NCC1-RIPE
     * </pre>
     * </li>
     * <li><div>A search on multiple sources:</div>
     * <pre>
     * http://apps.db.ripe.net/whois-beta/search?source=ripe&source=apnic&flags=no-referenced&flags=no-irt&query-string=MAINT-APNIC-AP
     * </pre>
     * </li>
     * <li><div>A search on multiple sources and multiple type-filters:</div>
     * <pre>http://apps.db.ripe.net/whois-beta/search?source=ripe&source=apnic&query-string=google&type-filter=person&type-filter=organisation</pre>
     * </li>
     * <li><div>A search using multiple flags:</div>
     * <pre>
     * http://apps.db.ripe.net/whois-beta/search?source=ripe&query-string=aardvark-mnt&flags=no-filtering&flags=brief&flags=no-referenced
     * </pre>
     * </li>
     * </ul>
     * Further documentation on the standard Whois Database Query flags can be found on the RIPE Whois Database Query Reference Manual.</p>
     * <p/>
     * <p/>
     * <div>Example of response in XML:</div>
     * <pre>
     *     &lt;?xml version='1.0' encoding='UTF-8'?&gt;
     *      &lt;whois-resources&gt;
     *          &lt;parameters xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
     *              &lt;inverse-lookup/&gt;
     *              &lt;type-filters/&gt;
     *              &lt;flags&gt;
     *                  &lt;flag value="B"/&gt;
     *              &lt;/flags&gt;
     *              &lt;query-strings&gt;
     *                  &lt;query-string value="AS102"/&gt;
     *              &lt;/query-strings&gt;
     *              &lt;sources&gt;
     *                  &lt;source id="TEST"/&gt;
     *              &lt;/sources&gt;
     *          &lt;/parameters&gt;
     *          &lt;objects&gt;
     *              &lt;object xmlns:xlink="http://www.w3.org/1999/xlink" type="aut-num"&gt;
     *                  &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/aut-num/AS102"/&gt;
     *                  &lt;source id="test"/&gt;
     *                  &lt;primary-key&gt;
     *                      &lt;attribute name="aut-num" value="AS102"/&gt;
     *                  &lt;/primary-key&gt;
     *                  &lt;attributes&gt;
     *                      &lt;attribute name="aut-num" value="AS102"/&gt;
     *                      &lt;attribute name="as-name" value="End-User-2"/&gt;
     *                      &lt;attribute name="descr" value="description"/&gt;
     *                      &lt;attribute name="admin-c" value="TP1-TEST" referenced-type="person-role"&gt;
     *                          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"/&gt;
     *                      &lt;/attribute&gt;
     *                      &lt;attribute name="tech-c" value="TP1-TEST" referenced-type="person-role"&gt;
     *                          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"/&gt;
     *                      &lt;/attribute&gt;
     *                      &lt;attribute name="mnt-by" value="OWNER-MNT" referenced-type="mntner"&gt;
     *                          &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"/&gt;
     *                      &lt;/attribute&gt;
     *                     &lt;attribute name="source" value="TEST"/&gt;
     *                 &lt;/attributes&gt;
     *                 &lt;tags/&gt;
     *             &lt;/object&gt;
     *         &lt;/objects&gt;
     *     &lt;/whois-resources&gt;
     * </pre>
     * <p/>
     * <div>Example of response in JSON:</div>
     * <pre>
     *    {"whois-resources": {
     *      "parameters": {
     *          "inverse-lookup": {
     *              "inverse-attribute": []
     *          },
     *          "type-filters": {
     *              "type-filter": []
     *          },
     *          "flags": {
     *              "flag": [
     *                  {
     *                      "value": "B"
     *                  }
     *              ]
     *          },
     *          "query-strings": {
     *              "query-string": [
     *                  {
     *                      "value": "AS102"
     *                  }
     *              ]
     *          },
     *          "sources": {
     *              "source": [
     *                  {
     *                      "id": "TEST"
     *                  }
     *              ]
     *          }
     *      },
     *      "objects": {
     *          "object": {
     *              "type": "aut-num",
     *              "link": {
     *                  "xlink:type": "locator",
     *                  "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/aut-num/AS102"
     *              },
     *              "source": {
     *                  "id": "test"
     *              },
     *              "primary-key": {
     *                  "attribute": [
     *                      {
     *                          "name": "aut-num",
     *                          "value": "AS102"
     *                      }
     *                  ]
     *              },
     *              "attributes": {
     *                  "attribute": [
     *                      {
     *                          "name": "aut-num",
     *                          "value": "AS102"
     *                      },
     *                      {
     *                          "name": "as-name",
     *                          "value": "End-User-2"
     *                      },
     *                      {
     *                          "name": "descr",
     *                          "value": "description"
     *                      },
     *                      {
     *                          "link": {
     *                              "xlink:type": "locator",
     *                              "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"
     *                          },
     *                          "name": "admin-c",
     *                          "value": "TP1-TEST",
     *                          "referenced-type": "person-role"
     *                      },
     *                      {
     *                          "link": {
     *                              "xlink:type": "locator",
     *                              "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/person-role/TP1-TEST"
     *                          },
     *                          "name": "tech-c",
     *                          "value": "TP1-TEST",
     *                          "referenced-type": "person-role"
     *                      },
     *                      {
     *                          "link": {
     *                              "xlink:type": "locator",
     *                              "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *                          },
     *                          "name": "mnt-by",
     *                          "value": "OWNER-MNT",
     *                          "referenced-type": "mntner"
     *                      },
     *                      {
     *                          "name": "source",
     *                          "value": "TEST"
     *                      }
     *                  ]
     *              },
     *              "tags": {
     *                  "tag": []
     *              }
     *          },
     *          "object": {
     *              "type": "person",
     *              "link": {
     *                  "xlink:type": "locator",
     *                  "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/person/TP1-TEST"
     *              },
     *              "source": {
     *                  "id": "test"
     *              },
     *              "primary-key": {
     *                  "attribute": [
     *                      {
     *                          "name": "nic-hdl",
     *                          "value": "TP1-TEST"
     *                      }
     *                  ]
     *              },
     *              "attributes": {
     *                  "attribute": [
     *                      {
     *                          "name": "person",
     *                          "value": "Test Person"
     *                      },
     *                      {
     *                          "name": "address",
     *                          "value": "Singel 258"
     *                      },
     *                      {
     *                          "name": "phone",
     *                          "value": "+31 6 12345678"
     *                      },
     *                      {
     *                          "name": "nic-hdl",
     *                          "value": "TP1-TEST"
     *                      },
     *                      {
     *                          "link": {
     *                              "xlink:type": "locator",
     *                              "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test/mntner/OWNER-MNT"
     *                          },
     *                          "name": "mnt-by",
     *                          "value": "OWNER-MNT",
     *                          "referenced-type": "mntner"
     *                      },
     *                      {
     *                          "name": "changed",
     *                          "value": "dbtest@ripe.net 20120101"
     *                      },
     *                      {
     *                          "name": "source",
     *                          "value": "TEST"
     *                      }
     *                  ]
     *              },
     *              "tags": {
     *                  "tag": []
     *              }
     *          }
     *      }
     *  }}
     * </pre>
     * <p/>
     * <p><div>The service URL must be:</div>
     * <div>'http://apps.db.ripe.net/whois-beta/search'</div>
     * and the following can be specified as HTTP parameters:</p>
     *
     * @param sources           Mandatory. It's possible to specify multiple sources.
     * @param queryString       Mandatory.
     * @param inverseAttributes If specified the query is an inverse lookup on the given attribute, if not specified the query is a direct lookup search.
     * @param include           Only show RPSL objects with given tags. Can be multiple.
     * @param exclude           Only show RPSL objects that <i>do not</i> have given tags. Can be multiple.
     * @param types             If specified the results will be filtered by object-type, multiple type-filters can be specified.
     * @param flags             Optional query-flags. Use separate flags parameters for each option (see examples above)
     * @return Returns the query result.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/search")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Search successful"),
            @ResponseCode(code = 400, condition = "Illegal input - incorrect value in one or more of the parameters"),
            @ResponseCode(code = 404, condition = "Query rendered no results")
    })
    public Response search(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String queryString,
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("include-tag") Set<String> includeTags,
            @QueryParam("exclude-tag") Set<String> excludeTags,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") Set<String> flags) {
        return doSearch(request, queryString, sources, inverseAttributes, include, exclude, types, flags);
    }

    private Response doSearch(
            final HttpServletRequest request,
            final String queryString,
            final Set<String> sources,
            final Set<String> inverseAttributes,
            final Set<String> includeTags,
            final Set<String> excludeTags,
            final Set<String> types,
            final Set<String> flags) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("Argument 'source' is missing, you have to specify a valid RIR source for your search request");
        }

        checkForInvalidSources(sources);
        final Set<String> separateFlags = splitInputFlags(flags);
        checkForInvalidFlags(separateFlags);

        final Query query = Query.parse(String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s",
                QueryFlag.SOURCES.getLongFlag(),
                JOINER.join(sources),
                QueryFlag.SHOW_TAG_INFO.getLongFlag(),
                (types == null || types.isEmpty()) ? "" : QueryFlag.SELECT_TYPES.getLongFlag(),
                JOINER.join(types),
                (inverseAttributes == null || inverseAttributes.isEmpty()) ? "" : QueryFlag.INVERSE.getLongFlag(),
                JOINER.join(inverseAttributes),
                (includeTags == null || includeTags.isEmpty()) ? "" : QueryFlag.FILTER_TAG_INCLUDE.getLongFlag(),
                JOINER.join(includeTags),
                (excludeTags == null || excludeTags.isEmpty()) ? "" : QueryFlag.FILTER_TAG_EXCLUDE.getLongFlag(),
                JOINER.join(excludeTags),
                Joiner.on(" ").join(Iterables.transform(separateFlags, new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return input.length() > 1 ? "--" + input : "-" + input;
                    }
                })),
                (queryString == null ? "" : queryString)));

        final Parameters parameters = new Parameters();
        parameters.setSources(sources);
        parameters.setQueryStrings(queryString);
        parameters.setInverseLookup(inverseAttributes);
        parameters.setTypeFilters(types);
        parameters.setFlags(separateFlags);

        return handleQuery(query, JOINER.join(sources), queryString, request, parameters);
    }

    private void checkForInvalidSources(final Set<String> sources) {
        for (final String source : sources) {
            checkForInvalidSource(source);
        }
    }

    private void checkForInvalidSource(final String source) {
        if (!sourceContext.getAllSourceNames().contains(ciString(source))) {
            throw new IllegalArgumentException(String.format("Invalid source '%s'", source));
        }
    }

    private Set<String> splitInputFlags(final Set<String> inputFlags) {
        final Set<String> separateFlags = Sets.newLinkedHashSet();  // reporting errors should happen in the same order
        for (final String flagParameter : inputFlags) {
            if (QueryFlag.getValidLongFlags().contains(flagParameter)) {
                separateFlags.add(flagParameter);
            } else {
                final CharacterIterator charIterator = new StringCharacterIterator(flagParameter);
                for (char flag = charIterator.first(); flag != CharacterIterator.DONE; flag = charIterator.next()) {
                    final String flagString = String.valueOf(flag);
                    if (!QueryFlag.getValidShortFlags().contains(flagString)) {
                        throw new IllegalArgumentException(String.format("Invalid option '%s'", flag));
                    }
                    separateFlags.add(flagString);
                }
            }
        }
        return separateFlags;
    }

    private void checkForInvalidFlags(final Set<String> flags) {
        for (final String flag : flags) {
            if (NOT_ALLOWED_SEARCH_QUERY_FLAGS.contains(flag)) {
                throw new IllegalArgumentException(String.format("Disallowed option '%s'", flag));
            }
        }
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

    private Update createUpdate(final RpslObject rpslObject, final List<String> passwords, final String deleteReason) {
        return new Update(
                createParagraph(rpslObject, passwords),
                deleteReason != null ? Operation.DELETE : Operation.UNSPECIFIED,
                deleteReason != null ? Lists.newArrayList(deleteReason) : null,
                rpslObject);
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
