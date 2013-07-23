package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.api.whois.ApiResponseHandler;
import net.ripe.db.whois.api.whois.rdap.domain.RdapObject;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.attrs.AttributeParseException;
import net.ripe.db.whois.common.domain.attrs.AutNum;
import net.ripe.db.whois.common.domain.attrs.Domain;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import net.ripe.db.whois.query.handler.QueryHandler;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static net.ripe.db.whois.common.rpsl.ObjectType.*;

@ExternallyManagedLifecycle
@Component
@Path("/")
public class WhoisRdapService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisRdapService.class);
    private static final int STATUS_TOO_MANY_REQUESTS = 429;
    private static final Set<ObjectType> ABUSE_CONTACT_TYPES = Sets.newHashSet(AUT_NUM, INETNUM, INET6NUM);
    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";

    private final SourceContext sourceContext;
    private final QueryHandler queryHandler;
    private final RpslObjectDao objectDao;
    private final AbuseCFinder abuseCFinder;
    private final RdapObjectMapper rdapObjectMapper;
    private final DelegatedStatsService delegatedStatsService;

    @Autowired
    public WhoisRdapService(final SourceContext sourceContext, final QueryHandler queryHandler,
                            final RpslObjectDao objectDao, final AbuseCFinder abuseCFinder, final NoticeFactory noticeFactory,
                            @Value("${rdap.port43:}") final String port43, final DelegatedStatsService delegatedStatsService) {
        this.sourceContext = sourceContext;
        this.queryHandler = queryHandler;
        this.objectDao = objectDao;
        this.abuseCFinder = abuseCFinder;
        this.rdapObjectMapper = new RdapObjectMapper(noticeFactory, port43);
        this.delegatedStatsService = delegatedStatsService;
    }

    /**
     * <h3>Queries</h3>
     * <p>
     * <div>Queries to the RIPE NCC RDAP implementation should be directed to http://rdap.db.ripe.net. For testing
     * purposes, queries can also be directed to http://rdap-test.db.ripe.net</div>
     * <div>The RDAP server accepts Content Media type application/json and application/rdap+json and will respond with the same</div>
     * </p>
     *
     * <h3>Examples</h3>
     * <ul>
     *     <li>
     *      <div>Successfully querying for an IP resource: http://rdap.db.ripe.net/ip/2003:2004:2005::/64</div>
     *      <div>Response header<pre>HTTP/1.1 200 OK</pre></div>
     *      <div>Content<pre>
     *    {
     *      "handle" : "2001:2002:2003::/48",
     *      "startAddress" : "2001:2002:2003::/128",
     *      "endAddress" : "2001:2002:2003:ffff:ffff:ffff:ffff:ffff/128",
     *      "ipVersion" : "v6",
     *      "name" : "RIPE-NCC",
     *      "type" : "ASSIGNED PA",
     *      "country" : "NL",
     *      "remarks" : [ {
     *        "description" : [ "Private Network" ]
     *      } ],
     *      "links" : [ {
     *     "value" : "http://localhost:56579/rdap/ip/2001:2002:2003::/48",
     *     "rel" : "self",
     *     "href" : "http://localhost:56579/rdap/ip/2001:2002:2003::/48"
     *         }, {
     *           "value" : "http://www.ripe.net/data-tools/support/documentation/terms",
     *           "rel" : "copyright",
     *           "href" : "http://www.ripe.net/data-tools/support/documentation/terms"
     *         } ],
     *         "events" : [ {
     *           "eventAction" : "last changed",
     *           "eventDate" : "2013-07-23T08:32:39Z"
     *         } ],
     *          "lang" : "EN",
     *          "rdapConformance" : [ "rdap_level_0" ],
     *          "notices" : [ {
     *            "title" : "Terms and Conditions",
     *            "description" : [ "This is the RIPE Database query service. The objects are in RDAP format." ],
     *            "links" : {
     *              "value" : "http://localhost:56579/rdap/ip/2001:2002:2003::/48/ip/2001:2002:2003::/48",
     *              "rel" : "terms-of-service",
     *              "href" : "http://www.ripe.net/db/support/db-terms-conditions.pdf",
     *              "type" : "application/pdf"
     *            }
     *          }, {
     *            "title" : "Filtered",
     *            "description" : [ "This output has been filtered." ]
     *          }, {
     *            "title" : "Source",
     *            "description" : [ "Objects returned came from source", "TEST" ]
     *          } ],
     *          "port43" : "whois.ripe.net"
     *        }
     *      </pre></div>
     *     </li>
     *
     *     <li>
     *         <div>Unsuccessfully quering for an autnum: http://rdap.db.ripe.net/entity/fred-mnt</div>
     *         <div>Response header<pre>
     *             HTTP/1.1 404 Not Found
     *         </pre>
     *         </div>
     *         <p>Note that queries for nameserver (ie, http://rdap.db.ripe.net/nameserver/whatever) will always result in
     *         404 Not Found since RIPE NCC does not have this information.</p>
     *     </li>
     *
     *     <li>
     *         <div>Quering for an autnum that exists at a different RIR: http://rdap.db.ripe.net/autnum/1840</div>
     *         <div>Response header<pre>
     *             HTTP/1.1 301 Moved Permanently
     *             Location: http://rdap.lacnic.net/autnum/1840
     *         </pre></div>
     *     </li>
     * </ul>
     *
     *
     * @param objectType The object type requested, one of ip, autnum, entity, domain, nameserver
     * @param key Primary key of the given object.
     * @return A JSON response is returned, containing the object requested. If the object is maintained at a different RIR
     * the response header will contain a pointer to where it can be found
     */
    @GET
    @TypeHint(RdapObject.class)
    @Produces({MediaType.APPLICATION_JSON, CONTENT_TYPE_RDAP_JSON})
    @Path("/{objectType}/{key:.*}")
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Object found for the specified key"),
            @ResponseCode(code = 301, condition = "Object can be found at a different RIR"),
            @ResponseCode(code = 400, condition = "Incorrect search value"),
            @ResponseCode(code = 404, condition = "The query didn't return any valid object")
    })
    public Response lookup(@Context final HttpServletRequest request,
                           @PathParam("objectType") final String objectType,
                           @PathParam("key") final String key) {

        final Set<ObjectType> whoisObjectTypes = Sets.newHashSet();

        switch (objectType.toLowerCase()) {
            case "autnum":
                whoisObjectTypes.add(AUT_NUM);
                validateAutnum(getKey(whoisObjectTypes, key));
                break;

            case "domain":
                validateDomain(key);
                whoisObjectTypes.add(DOMAIN);
                break;

            case "ip":
                validateIp(key);
                whoisObjectTypes.add(key.contains(":") ? INET6NUM : INETNUM);
                break;

            case "entity":
                validateEntity(key);
                if (key.toUpperCase().startsWith("ORG-")) {
                    whoisObjectTypes.add(ORGANISATION);
                } else {
                    whoisObjectTypes.add(PERSON);
                    whoisObjectTypes.add(ROLE);
                }
                break;

            case "nameserver":
                return Response.status(NOT_FOUND).build();

            default:
                return Response.status(BAD_REQUEST).build();
        }

        return lookupObject(request, whoisObjectTypes, getKey(whoisObjectTypes, key));
    }

    private void validateDomain(final String key) {
        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw new IllegalArgumentException("RIPE NCC does not support forward domain queries.");
        }
    }

    private void validateIp(final String key) {
        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid IP syntax.", e);
        }
    }

    private void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw new IllegalArgumentException("Invalid syntax.");
        }
    }

    private void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw new IllegalArgumentException("Invalid syntax.");
            }
        } else {
            if (!AttributeType.NIC_HDL.isValidValue(ObjectType.PERSON, key)) {
                throw new IllegalArgumentException("Invalid syntax");
            }
        }
    }

    private String getKey(final Set<ObjectType> objectTypes, final String key) {
        if (objectTypes.contains(AUT_NUM)) {
            return String.format("AS%s", key);
        }
        return key;
    }

    protected Response lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes, final String key) {
        final String source = sourceContext.getWhoisSlaveSource().getName().toString();
        final String objectTypesString = Joiner.on(",").join(Iterables.transform(objectTypes, new Function<ObjectType, String>() {
            @Override
            public String apply(final ObjectType input) {
                return input.getName();
            }
        }));

        final Query query = Query.parse(
                String.format("%s %s %s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SOURCES.getLongFlag(),
                        source,
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesString,
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));

        return handleQuery(query, request);
    }

    protected Response handleQuery(final Query query, final HttpServletRequest request) {

        final int contextId = System.identityHashCode(Thread.currentThread());
        final InetAddress remoteAddress = InetAddresses.forString(request.getRemoteAddr());

        final List<RpslObject> result = Lists.newArrayList();

        try {
            queryHandler.streamResults(query, remoteAddress, contextId, new ApiResponseHandler() {
                @Override
                public void handle(final ResponseObject responseObject) {
                    if (responseObject instanceof RpslObject) {
                        result.add((RpslObject) responseObject);
                    }
                }
            });

            if (result.isEmpty()) {
                return redirect(getRequestPath(request), query);
            }

            if (result.size() > 1) {
                throw new IllegalStateException("Unexpected result size: " + result.size());
            }

            final RpslObject resultObject = result.get(0);

            return Response.ok(
                    rdapObjectMapper.map(
                            getRequestUrl(request),
                            resultObject,
                            objectDao.getLastUpdated(resultObject.getObjectId()),
                            getAbuseContacts(resultObject))).build();

        } catch (final QueryException e) {
            if (e.getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                throw new WebApplicationException(Response.status(STATUS_TOO_MANY_REQUESTS).build());
            } else {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    private Response redirect(final String requestPath, final Query query) {
        final URI uri = delegatedStatsService.getUriForRedirect(requestPath, query);
        return Response.status(Response.Status.MOVED_PERMANENTLY).contentLocation(uri).build();
    }

    private String getRequestUrl(final HttpServletRequest request) {
        final StringBuffer buffer = request.getRequestURL();
        if (request.getQueryString() != null) {
            buffer.append('?');
            buffer.append(request.getQueryString());
        }
        return buffer.toString();
    }

    private String getRequestPath(final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder();
        builder.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            builder.append('?');
            builder.append(request.getQueryString());
        }
        return builder.toString();
    }

    private List<RpslObject> getAbuseContacts(final RpslObject rpslObject) {
        final ObjectType objectType = rpslObject.getType();
        if (ABUSE_CONTACT_TYPES.contains(objectType)) {
            return abuseCFinder.findAbuseContacts(rpslObject);
        }
        return Collections.emptyList();
    }
}
