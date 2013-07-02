package net.ripe.db.whois.api.whois;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.domain.Attribute;
import net.ripe.db.whois.api.whois.domain.Link;
import net.ripe.db.whois.api.whois.domain.Parameters;
import net.ripe.db.whois.api.whois.domain.WhoisModify;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import net.ripe.db.whois.update.domain.Credentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.PasswordCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.query.query.QueryFlag.ALL_SOURCES;
import static net.ripe.db.whois.query.query.QueryFlag.CLIENT;
import static net.ripe.db.whois.query.query.QueryFlag.DIFF_VERSIONS;
import static net.ripe.db.whois.query.query.QueryFlag.LIST_SOURCES;
import static net.ripe.db.whois.query.query.QueryFlag.LIST_SOURCES_OR_VERSION;
import static net.ripe.db.whois.query.query.QueryFlag.LIST_VERSIONS;
import static net.ripe.db.whois.query.query.QueryFlag.NO_GROUPING;
import static net.ripe.db.whois.query.query.QueryFlag.NO_TAG_INFO;
import static net.ripe.db.whois.query.query.QueryFlag.PERSISTENT_CONNECTION;
import static net.ripe.db.whois.query.query.QueryFlag.SHOW_TAG_INFO;
import static net.ripe.db.whois.query.query.QueryFlag.SHOW_VERSION;
import static net.ripe.db.whois.query.query.QueryFlag.SOURCES;
import static net.ripe.db.whois.query.query.QueryFlag.TEMPLATE;
import static net.ripe.db.whois.query.query.QueryFlag.VERBOSE;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRestService  extends WhoisService {
    private static final Pattern UPDATE_RESPONSE_ERRORS = Pattern.compile("(?m)^\\*\\*\\*Error:\\s*((.*)(\\n[ ]+.*)*)$");
    private static final Joiner JOINER = Joiner.on(",");
    private static final Set<QueryFlag> NOT_ALLOWED_SEARCH_QUERY_FLAGS = Sets.newHashSet(TEMPLATE, VERBOSE, CLIENT, NO_GROUPING, SOURCES, NO_TAG_INFO, SHOW_TAG_INFO, ALL_SOURCES, LIST_SOURCES_OR_VERSION, LIST_SOURCES, DIFF_VERSIONS, LIST_VERSIONS, SHOW_VERSION, PERSISTENT_CONNECTION);

    @Autowired
    public WhoisRestService(final DateTimeProvider dateTimeProvider, final UpdateRequestHandler updateRequestHandler, final LoggerContext loggerContext, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final SourceContext sourceContext, final QueryHandler queryHandler) {
        super(dateTimeProvider, updateRequestHandler,loggerContext, rpslObjectDao, rpslObjectUpdateDao, sourceContext, queryHandler);
    }

    /**
     * <p><div>The lookup service returns a single object specified by the source, object type and primary-key arguments.</div>
     * <p/>
     * <p><div>Example query:</div>
     * http://apps.db.ripe.net/whois-beta/lookup/ripe/mntner/RIPE-DBM-MNT</p>
     *
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
     *
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
     *}
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
        return lookupObject(request, source, objectType, key, false);
    }

    /**
     * <p>The grs-lookup interface returns the single object that satisfy the key conditions specified as path parameters via the grs-source and the primary-key arguments</p>
     * <p/>
     * <p><div>Example query:</div>
     * http://apps.db.ripe.net/whois-beta/grs-lookup/apnic-grs/mntner/MAINT-APNIC-AP</p>
     *
     * @param source     Source
     * @param objectType Object type for given object.
     * @param key        Primary key of the given object.
     */
    @GET
    @TypeHint(WhoisResources.class)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @Path("/grs-lookup/{source}/{objectType}/{key:.*}")
    public Response grslookup(
            @Context final HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("objectType") final String objectType,
            @PathParam("key") final String key) {
        return lookupObject(request, source, objectType, key, true);
    }

    /**
     * <p>A successful create request creates an object in the RIPE Database or in the RIPE Test Database, depending on the source that you specify in the source element of your request XML. Source can be "ripe" or "test".</p>
     * <p>One or more password values can be specified as HTTP parameters.</p>
     * <p>The create interface is accessible using the HTTP POST method. The request must include a 'content-type: application/xml' header because your request body will contain an XML document describing the new object and the target source.</p>
     * <p>An example of XML object:
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
     * <p>Example<div>Create request using the CURL command:</div>
     * <pre>curl -X POST -H 'Content-Type: application/xml' -d
     * '&lt;whois-resources&gt;&lt;objects&gt;
     * &lt;object-type="person"&gt;
     * &lt;source-id="test"/&gt;
     * &lt;attributes&gt;
     * &lt;attribute name="person" value="Pauleth Palthen"/&gt;&lt;attribute name="address" value="Singel 258"/&gt;
     * &lt;attribute name="phone" value="+31-1234567890"/&gt;&lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     * &lt;attribute name="mnt-by" value="PP-MNT" /&gt;&lt;attribute name="nic-hdl" value="AUTO-1" /&gt;
     * &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;&lt;attribute name="source" value="TEST"/&gt;
     * &lt;/attributes&gt;
     * &lt;/object&gt;&lt;/objects&gt;&lt;/whois-resources&gt;'
     * https://apps.db.ripe.net/whois-beta/create?password=123 -D headers.txt</pre></p>
     * The HTTP headers for a success response:
     * <pre>
     * HTTP/1.1 201 Created
     * Date: Tue, 28 Dec 2010 14:17:28 GMT
     * Server: Apache/2.2.3 (CentOS)
     * X-Powered-By: Servlet 2.5; JBoss-5.0/JBossWeb-2.1
     * Location: http://apps.db.ripe.net/whois-beta/lookup/test/person/PP16-TEST
     * Content-Length: 0
     * Connection: close
     * Content-Type: text/plain; charset=UTF-8</pre>
     * The response body will be empty.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_JSON, TEXT_XML})
    @Path("/create/{source}")
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
     * <p>A successful update request replaces all of an object attributes with the new set of attributes described in the request. The target database can be ripe or test and is specified with the source element in the XML document sent with the request.</p>
     * <p>The update interface is accessible using the HTTP PUT method. The request must include a 'content-type: application/xml' header because your request body will contain an XML document describing the object update and the target source.</p>
     * <p/>
     * <p>An example of XML object:
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
     * <p>An example of update request using the CURL command:
     * <pre>curl -X PUT -H 'Content-Type: application/xml' -d '&lt;whois-resources&gt;&lt;objects&gt;
     * &lt;object-type="person"&gt;&lt;source-id="test"/&gt;&lt;attributes&gt;
     * &lt;attribute name="person" value="Pauleth Palthen"/&gt;
     * &lt;attribute name="address" value="Singel 123"/&gt;
     * &lt;attribute name="phone" value="+31-0987654321"/&gt;
     * &lt;attribute name="e-mail" value="ppalse@ripe.net"/&gt;
     * &lt;attribute name="changed" value="ppalse@ripe.net 20101228"/&gt;
     * &lt;attribute name="mnt-by" value="PP-MNT" /&gt;
     * &lt;attribute name="nic-hdl" value="PP16-TEST" /&gt;
     * &lt;attribute name="source" value="TEST"/&gt;&lt;/attributes&gt;&lt;/object&gt;&lt;/objects&gt;&lt;/whois-resources&gt;'
     * https://apps.db.ripe.net/whois-beta/update/test/person/pp16-test?password=123 -D headers.txt</pre></p>
     * <p/>
     * <p>The HTTP headers for a success response:
     * <p/>
     * <pre>HTTP/1.1 200 OK
     * Date: Tue, 28 Dec 2010 15:24:35 GMT
     * Server: Apache/2.2.3 (CentOS)
     * X-Powered-By: Servlet 2.5; JBoss-5.0/JBossWeb-2.1
     * Connection: close
     * Transfer-Encoding: chunked
     * Content-Type: application/xml</pre></p>
     * <p/>
     * The response body for a success response:
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
     *
     * @param source     Source.
     * @param objectType Object type for given object.
     * @param key        Primary key of the given object.
     * @param passwords  One or more password values.
     */
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
     * <p>The modify interface implements complex object manipulations that would otherwise require multiple client side operations, like:</p>
     * <ul>
     * <li>querying the Whois Database</li>
     * <li>filtering from the query response the only object that need to be modified</li>
     * <li>parsing the object RPSL (handling all the intricacies of RPSL)</li>
     * <li>modifying specific attributes</li>
     * <li>submitting the edited object</li>
     * </ul>
     * <p/>
     * <p>With the modify interface this error prone processing of objects can be replaced with just one simple request.
     * A client just needs to specify the type of operation to be applied, the primary key of an object and eventually a set of attributes.</p>
     * <p/>
     * <p>A request containing XML format content must include a "content-type: application/xml" HTTP header.</p>
     * <p/>
     * <p>Important, a modify request succeeds only if the final modified object satisfies the RPSL specification.
     * For example a modify request resulting in an object missing mandatory attributes will fail.</p>
     * <p/>
     * <p>Different actions that can be executed by specifying one of 'add', 'remove' or 'replace':
     * <p/>
     * <ul>
     * <li>replace attributes</li>
     * <li>append new attributes</li>
     * <li>add new attributes starting from the line at index N</li>
     * <li>remove all attributes of a given type</li>
     * <li>remove the Nth attribute</li>
     * </ul></p>
     * <p/>
     * <p><div>Examples</div>
     * <p/>
     * <ul>
     * <li><div>Add attributes request</div>
     * <pre>
     *  &lt;whois-modify&gt;
     *      &lt;add&gt;
     *          &lt;attributes&gt;
     *              &lt;attribute name="phone" value="+31 20 535 4444"/&gt;
     *              &lt;attribute name="fax-no" value="+31 20 535 4445"/&gt;
     *          &lt;/attributes&gt;
     *      &lt;/add&gt;
     *  &lt;/whois-modify&gt;
     *  </pre></li>
     *
     * <li><div>Add attributes using CURL</div>
     * <pre>curl -X POST -H 'Content-Type: application/xml' -d
     * '&lt;whois-modify&gt;&lt;add&gt;&lt;attributes&gt;&lt;attribute name="phone" value="+31 20 535 4444"/&gt;
     * &lt;attribute name="fax-no" value="+31 20 535 4445"/&gt;&lt;/attributes&gt;&lt;/add&gt;&lt;/whois-modify&gt;'
     * https://apps.db.ripe.net/whois-beta/modify/test/person/pp16-test?password=123 -D headers.txt</pre>
     * </li>
     * </ul></p>
     *
     *
     * The response headers may be:
     * <pre>
     * HTTP/1.1 200 OK
     * Date: Wed, 29 Dec 2010 11:06:43 GMT
     * Server: Apache/2.2.3 (CentOS)
     * X-Powered-By: Servlet 2.5; JBoss-5.0/JBossWeb-2.1
     * Connection: close
     * Transfer-Encoding: chunked
     * Content-Type: application/xml</pre>
     *
     * <p>The response body may be:</p>
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
     *      &lt;attribute name="phone" value="+31-0987654321"/&gt;
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
     *
     * @param source     RIPE or TEST.
     * @param objectType Object type of given object.
     * @param key        Primary key of given object.
     * @param passwords  One or more password values.
     */
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
     * <p>A successful delete request deletes an object from the RIPE Database or the RIPE Test Database. The target database for the delete service is specified directly as a URL parameter as well as the primary key and the object type of the object to be deleted.</p>
     * <p/>
     * <p>The HTTP Request body must be empty.</p>
     * <p/>
     * <div>Example using CURL:</div>
     * <pre>
     * curl -X DELETE https://apps.db.ripe.net/whois-beta/delete/test/person/pp16-test?password=123 -D headers.txt
     * </pre>
     * <p/>
     *
     * <p>The HTTP headers for a success response:
     * <p/>
     * <pre>HTTP/1.1 204 No Content
     * Date: Wed, 29 Dec 2010 09:43:17 GMT
     * Server: Apache/2.2.3 (CentOS)
     * X-Powered-By: Servlet 2.5; JBoss-5.0/JBossWeb-2.1
     * Content-Length: 0
     * Connection: close
     * Content-Type: text/plain; charset=UTF-8</pre></p>
     *
     * @param source     Source.
     * @param objectType Object type of given object.
     * @param key        Primary key for given object.
     * @param reason     Reason for deleting given object. Optional.
     * @param passwords  One or more password values.
     */
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
                createUpdate(originalObject, passwords, reason),
                createContent(originalObject, passwords, reason),
                Keyword.NONE,
                source);

        return getResponse(response);
    }

    /**
     * <p>Lists versions of an RPSL object</p>
     *
     * <div>Example query:</div>
     * <pre>
     *  http://apps.db.ripe.net/whois-beta/versions/TEST/AS102
     * </pre>
     *
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
     *
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
    public Response listVersions(
            @Context HttpServletRequest request,
            @PathParam("source") final String source,
            @PathParam("key") final String key) {
        final Query query = Query.parse(String.format("--list-versions %s", key));
        return handleQuery(query, source, key, request, null);
    }

    /**
     * <p>Show a specific version of an RPSL object</p>
     *
     * <div>Example query:</div>
     * <pre>
     *  http://apps.db.ripe.net/whois-beta/version/TEST/2/AS102
     * </pre>
     *
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
     *
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
     *}
     ** </pre>
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
     *
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
     *
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
     *
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
    public Response search(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String queryString,
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("include") Set<String> include,
            @QueryParam("exclude") Set<String> exclude,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") Set<String> flags) {
        return doSearch(request, queryString, sources, inverseAttributes, include, exclude, types, flags, false);
    }

    /**
     * <p>The grs-search interface has exactly the same features of the search, with the only difference that in the source parameter you will be specifying one or more GRS sources.
     * The query will therefore be executed on the GRS sources that you specify and will return data from the respective mirrors maintained in the RIPE Database platform.</p>
     * <p/>
     * <p><div>The service URL is:</div>
     * 'http://apps.db.ripe.net/whois-beta/grs-search'</p>
     * <p/>
     * <p><div>Example:</div>
     * <ul>
     * <li><div>Search for 193/8 on the ripe, apnic, arin, lacnic, radb GRS mirrors:</div>
     * <pre>
     *  http://apps.db.ripe.net/whois-beta/grs-search?flags=&source=apnic-grs&source=arin-grs&source=lacnic-grs&source=radb-grs&query-string=193%2F8</li>
     * </pre>
     *
     * <div>Example response in XML:</div>
     * <pre>
     *     &lt;?xml version='1.0' encoding='UTF-8'?&gt;
     &lt;whois-resources&gt;
     &lt;parameters xmlns:xlink="http://www.w3.org/1999/xlink"&gt;
     &lt;inverse-lookup/&gt;
     &lt;type-filters/&gt;
     &lt;flags/&gt;
     &lt;query-strings&gt;
     &lt;query-string value="AS102"/&gt;
     &lt;/query-strings&gt;
     &lt;sources&gt;
     &lt;source id="TEST-GRS"/&gt;
     &lt;/sources&gt;
     &lt;/parameters&gt;
     &lt;objects&gt;
     &lt;object xmlns:xlink="http://www.w3.org/1999/xlink"
     type="aut-num"&gt;
     &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test-grs/aut-num/AS102"/&gt;
     &lt;source id="test-grs"/&gt;
     &lt;primary-key&gt;
     &lt;attribute name="aut-num" value="AS102"/&gt;
     &lt;/primary-key&gt;
     &lt;attributes&gt;
     &lt;attribute name="aut-num" value="AS102"/&gt;
     &lt;attribute name="as-name" value="End-User-2"/&gt;
     &lt;attribute name="descr" value="description"/&gt;
     &lt;attribute name="admin-c" value="DUMY-RIPE" referenced-type="person-role"&gt;
     &lt;link xlink:type="locator"
     xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test-grs/person-role/DUMY-RIPE"/&gt;
     &lt;/attribute&gt;
     &lt;attribute name="tech-c" value="DUMY-RIPE" referenced-type="person-role"&gt;
     &lt;link xlink:type="locator"
     xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test-grs/person-role/DUMY-RIPE"/&gt;
     &lt;/attribute&gt;
     &lt;attribute name="mnt-by" value="OWNER-MNT" referenced-type="mntner"&gt;
     &lt;link xlink:type="locator"
     xlink:href="http://apps.db.ripe.net/whois-beta/lookup/test-grs/mntner/OWNER-MNT"/&gt;
     &lt;/attribute&gt;
     &lt;attribute name="source" value="TEST-GRS"/&gt;
     &lt;attribute name="remarks" value="****************************"/&gt;
     &lt;attribute name="remarks" value="* THIS OBJECT IS MODIFIED"/&gt;
     &lt;attribute name="remarks" value="* Please note that all data that is generally regarded as personal"/&gt;
     &lt;attribute name="remarks" value="* data has been removed from this object."/&gt;
     &lt;attribute name="remarks" value="* To view the original object, please query the RIPE Database at:"/&gt;
     &lt;attribute name="remarks" value="* http://www.ripe.net/whois"/&gt;
     &lt;attribute name="remarks" value="****************************"/&gt;
     &lt;/attributes&gt;
     &lt;tags/&gt;
     &lt;/object&gt;
     &lt;/objects&gt;
     &lt;/whois-resources&gt;
     * </pre>
     * <div>Example response in JSON:</div>
     * <pre>
     *{"whois-resources": {
     *    "parameters": {
     *        "inverse-lookup": {
     *            "inverse-attribute": []
     *        },
     *        "type-filters": {
     *            "type-filter": []
     *        },
     *        "flags": {
     *            "flag": []
     *        },
     *        "query-strings": {
     *            "query-string": [
     *                {
     *                    "value": "AS102"
     *                }
     *            ]
     *        },
     *        "sources": {
     *            "source": [
     *                {
     *                    "id": "TEST-GRS"
     *                }
     *            ]
     *        }
     *    },
     *    "objects": {
     *        "object": {
     *            "type": "aut-num",
     *            "link": {
     *                "xlink:type": "locator",
     *                "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test-grs/aut-num/AS102"
     *            },
     *            "source": {
     *                "id": "test-grs"
     *            },
     *            "primary-key": {
     *                "attribute": [
     *                    {
     *                        "name": "aut-num",
     *                        "value": "AS102"
     *                    }
     *                ]
     *            },
     *            "attributes": {
     *                "attribute": [
     *                    {
     *                        "name": "aut-num",
     *                        "value": "AS102"
     *                    },
     *                    {
     *                        "name": "as-name",
     *                        "value": "End-User-2"
     *                    },
     *                    {
     *                        "name": "descr",
     *                        "value": "description"
     *                    },
     *                    {
     *                        "link": {
     *                            "xlink:type": "locator",
     *                            "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test-grs/person-role/DUMY-RIPE"
     *                        },
     *                        "name": "admin-c",
     *                        "value": "DUMY-RIPE",
     *                        "referenced-type": "person-role"
     *                    },
     *                    {
     *                        "link": {
     *                            "xlink:type": "locator",
     *                            "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test-grs/person-role/DUMY-RIPE"
     *                        },
     *                        "name": "tech-c",
     *                        "value": "DUMY-RIPE",
     *                        "referenced-type": "person-role"
     *                    },
     *                    {
     *                        "link": {
     *                            "xlink:type": "locator",
     *                            "xlink:href": "http://apps.db.ripe.net/whois-beta/lookup/test-grs/mntner/OWNER-MNT"
     *                        },
     *                        "name": "mnt-by",
     *                        "value": "OWNER-MNT",
     *                        "referenced-type": "mntner"
     *                    },
     *                    {
     *                        "name": "source",
     *                        "value": "TEST-GRS"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "****************************"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "* THIS OBJECT IS MODIFIED"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "* Please note that all data that is generally regarded as personal"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "* data has been removed from this object."
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "* To view the original object, please query the RIPE Database at:"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "* http://www.ripe.net/whois"
     *                    },
     *                    {
     *                        "name": "remarks",
     *                        "value": "****************************"
     *                    }
     *                ]
     *            },
     *            "tags": {
     *                "tag": []
     *            }
     *        }
     *    }
     *}}
     * </pre>
     * </ul></p>
     *
     * @param sources           Mandatory. It's possible to specify multiple sources.
     * @param queryString       Mandatory.
     * @param inverseAttributes If specified the query is an inverse lookup on the given attribute, if not specified the query is a direct lookup search.
     * @param include           Only show RPSL objects with given tags. Can be multiple.
     * @param exclude           Only show RPSL objects that <i>do not</i> have given tags. Can be multiple.
     * @param types             If specified the results will be filtered by object-type, multiple type-filters can be specified.
     * @param flags             Optional query-flags.
     * @return Returns the result of a grs query.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, TEXT_XML, TEXT_JSON})
    @TypeHint(WhoisResources.class)
    @Path("/grs-search")
    public Response grssearch(
            @Context HttpServletRequest request,
            @QueryParam("source") Set<String> sources,
            @QueryParam("query-string") String queryString,
            @QueryParam("inverse-attribute") Set<String> inverseAttributes,
            @QueryParam("include") Set<String> include,
            @QueryParam("exclude") Set<String> exclude,
            @QueryParam("type-filter") Set<String> types,
            @QueryParam("flags") Set<String> flags) {
        return doSearch(request, queryString, sources, inverseAttributes, include, exclude, types, flags, true);
    }

    private Response doSearch(
            final HttpServletRequest request,
            final String queryString,
            final Set<String> sources,
            final Set<String> inverseAttributes,
            final Set<String> includeTags,
            final Set<String> excludeTags,
            final Set<String> types,
            final Set<String> flags,
            final boolean isGrs) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("Argument 'source' is missing, you have to specify a valid RIR source for your search request");
        }

        checkForInvalidSources(sources, isGrs);
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

    private void checkForInvalidSources(final Set<String> sources, final boolean isGrs) {
        for (final String source : sources) {
            checkForInvalidSource(source, isGrs);
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
            // TODO: [AH] cache this instead of executing fot each request
            if (0 <= Iterables.indexOf(NOT_ALLOWED_SEARCH_QUERY_FLAGS, new Predicate<QueryFlag>() {
                @Override
                public boolean apply(final QueryFlag input) {
                    return input.getFlags().contains(flag);
                }
            })) {
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
