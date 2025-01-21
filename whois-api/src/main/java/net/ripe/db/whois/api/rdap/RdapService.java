package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Service
public class RdapService {

    private static final String CONTENT_TYPE_RDAP_JSON = "application/rdap+json";

    private static final Logger LOGGER = LoggerFactory.getLogger(RdapService.class);

    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private final RdapFullTextSearch rdapFullTextSearch;

    private final RdapObjectMapper rdapObjectMapper;

    private final int maxResultSize;

    private final int maxEntityResultSize;

    private final String baseUrl;

    private final RdapRequestValidator rdapRequestValidator;

    private final RdapRelationService rdapRelationService;

    private final RdapQueryHandler rdapQueryHandler;

    private final SourceContext sourceContext;

    private final RpslObjectUpdateDao rpslObjectUpdateDao;

    private final Source source;

    private final AbuseCFinder abuseCFinder;


    /**
     * @param maxResultSize: If the response is bigger than maxResultSize, we truncate the response and we add a notification
     * @param maxEntityResultSize: used for networks maximum retrieved objects, if we retrieve more objects than
     *                           the maximum value we truncate the response and we add a notification in the response.
     */
    @Autowired
    public RdapService(final RdapFullTextSearch rdapFullTextSearch,
                       final RdapObjectMapper rdapObjectMapper,
                       @Value("${rdap.public.baseUrl:}") final String baseUrl,
                       @Value("${rdap.search.max.results:100}") final int maxResultSize,
                       @Value("${rdap.entity.max.results:100}") final int maxEntityResultSize,
                       final RdapRequestValidator rdapRequestValidator,
                       final RdapRelationService rdapRelationService,
                       final RdapQueryHandler rdapQueryHandler,
                       final SourceContext sourceContext,
                       final RpslObjectUpdateDao rpslObjectUpdateDao,
                       final AbuseCFinder abuseCFinder){
        this.rdapFullTextSearch = rdapFullTextSearch;
        this.rdapObjectMapper = rdapObjectMapper;
        this.maxResultSize = maxResultSize;
        this.maxEntityResultSize = maxEntityResultSize;
        this.baseUrl = baseUrl;
        this.rdapRequestValidator = rdapRequestValidator;
        this.rdapRelationService = rdapRelationService;
        this.rdapQueryHandler = rdapQueryHandler;
        this.sourceContext = sourceContext;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.source = sourceContext.getCurrentSource();
        this.abuseCFinder = abuseCFinder;

    }

    protected RdapObject mapHelp(final HttpServletRequest request){
        return rdapObjectMapper.mapHelp(getRequestUrl(request));
    }

    protected Object lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes,
                                 final String key) {
        final List<RpslObject> result = rdapQueryHandler.handleQueryStream(getQueryObject(objectTypes, key), request).toList();
        return getRdapObject(request, result);
    }

    protected Object lookupForAutNum(final HttpServletRequest request, final String key) {
        try {
            final Query query = getQueryObject(ImmutableSet.of(AUT_NUM), key);
            List<RpslObject> result = rdapQueryHandler.handleAutNumQuery(query, request);

            return getRdapObject(request, result);
        } catch (RdapException ex){
            throw new AutnumException(ex.getErrorTitle(), ex.getErrorDescription(), ex.getErrorCode());
        }
    }

    protected Object lookupForDomain(final HttpServletRequest request, final String key, final String reverseIp) {
        final Stream<RpslObject> domainResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), key), request);
        final Stream<RpslObject> inetnumResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), reverseIp), request);

        return getDomainEntity(request, domainResult, inetnumResult);
    }

    protected Object lookupForOrganisation(final HttpServletRequest request, final String key) {
        final List<RpslObject> organisationResult = rdapQueryHandler.handleQueryStream(getQueryObject(Set.of(ORGANISATION), key), request).toList();

        final RpslObject organisation = switch (organisationResult.size()) {
            case 0 ->
                    throw new RdapException("404 Not Found", "Requested organisation not found: " + key, HttpStatus.NOT_FOUND_404);
            case 1 -> organisationResult.get(0);
            default ->
                    throw new RdapException("500 Internal Error", "Unexpected result size: " + organisationResult.size(), HttpStatus.INTERNAL_SERVER_ERROR_500);
        };

        final Set<RpslObjectInfo> references = getReferences(organisation);

        final List<RpslObjectInfo> autnumResult = references.stream()
                .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == AUT_NUM)
                .toList();

        final List<RpslObjectInfo> inetnumResult = references.stream()
                .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == INETNUM)
                .toList();

        final List<RpslObjectInfo> inet6numResult = references.stream()
                .filter(rpslObjectInfo -> rpslObjectInfo.getObjectType() == INET6NUM)
                .toList();

        return getOrganisationRdapObject(request, organisation, autnumResult, inetnumResult, inet6numResult);
    }

    private List<RpslObject> handleRelationQuery(final HttpServletRequest request, final RdapRequestType requestType,
                                                final RelationType relationType, final String key) {
        final List<RpslObject> rpslObjects;
        switch (requestType) {
            case AUTNUMS -> throw new RdapException("501 Not Implemented", "Relation queries not allowed for autnum", HttpStatus.NOT_IMPLEMENTED_501);
            case DOMAINS -> {
                rdapRequestValidator.validateDomain(key);
                final List<String> relatedPkeys = rdapRelationService.getDomainsByRelationType(key, relationType);

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), relatedPkey), request))
                        .toList();
            }
            case IPS -> {
                rdapRequestValidator.validateIp(request.getRequestURI(), key);
                final List<String> relatedPkeys = rdapRelationService.getInetnumRelationPkeys(key, relationType);

                rpslObjects = relatedPkeys
                        .stream()
                        .flatMap(relatedPkey -> rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), relatedPkey), request))
                        .toList();
            }
            default -> throw new RdapException("400 Bad Request", "Invalid or unknown type " + requestType.toString().toLowerCase(), HttpStatus.BAD_REQUEST_400);
        }
        return rpslObjects;
    }

    protected Object handleRelationQuery(HttpServletRequest request, RdapRequestType requestType, String key, RelationType relation) {

        final List<RpslObject> rpslObjects = handleRelationQuery(request, requestType, relation, key);

        return rdapObjectMapper.mapSearch(
                getRequestUrl(request),
                rpslObjects,
                maxResultSize);
    }

    protected Object handleSearch(final String[] fields, final String term, final HttpServletRequest request) {
        LOGGER.debug("Search {} for {}", fields, term);

        if (StringUtils.isEmpty(term)) {
            throw new RdapException("400 Bad Request", "Empty search term", HttpStatus.BAD_REQUEST_400);
        }

        try {
            final List<RpslObject> objects = rdapFullTextSearch.performSearch(fields, term, request.getRemoteAddr(), source);

            if (objects.isEmpty()) {
                throw new RdapException("404 Not Found", "Requested object not found: " + term, HttpStatus.NOT_FOUND_404);
            }

            return rdapObjectMapper.mapSearch(
                    getRequestUrl(request),
                    objects,
                    maxResultSize);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RdapException("500 Internal Error", "search failed", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    private Set<RpslObjectInfo> getReferences(final RpslObject organisation) {
        final Source originalSource = sourceContext.getCurrentSource();
        try {
            sourceContext.setCurrent(sourceContext.getSlaveSource());
            return rpslObjectUpdateDao.getReferences(organisation);
        } finally {
            sourceContext.setCurrent(originalSource);
        }
    }

    private Object getOrganisationRdapObject(final HttpServletRequest request,
                                               final RpslObject organisation,
                                               final List<RpslObjectInfo> autnumResult,
                                               final List<RpslObjectInfo> inetnumResult,
                                               final List<RpslObjectInfo> inet6numResult) {
        return rdapObjectMapper.mapOrganisationEntity(
                getRequestUrl(request),
                organisation,
                autnumResult,
                inetnumResult,
                inet6numResult,
                maxEntityResultSize);
    }

    private Object getDomainEntity(final HttpServletRequest request, final Stream<RpslObject> domainResult,
                                   final Stream<RpslObject> inetnumResult) {
        final Iterator<RpslObject> domainIterator = domainResult.iterator();
        final Iterator<RpslObject> inetnumIterator = inetnumResult.iterator();
        if (!domainIterator.hasNext()) {
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }
        final RpslObject domainObject = domainIterator.next();
        final RpslObject inetnumObject = inetnumIterator.hasNext() ? inetnumIterator.next() : null;

        if (domainIterator.hasNext() || inetnumIterator.hasNext()) {
            throw new RdapException("500 Internal Error", "Unexpected result size: " + Iterators.size(domainIterator),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return rdapObjectMapper.mapDomainEntity(getRequestUrl(request), domainObject, inetnumObject);
    }

    private Query getQueryObject(final Set<ObjectType> objectTypes, final String key) {
        return Query.parse(
                String.format("%s %s %s %s %s %s",
                        QueryFlag.NO_GROUPING.getLongFlag(),
                        QueryFlag.NO_REFERENCED.getLongFlag(),
                        QueryFlag.SELECT_TYPES.getLongFlag(),
                        objectTypesToString(objectTypes),
                        QueryFlag.NO_FILTERING.getLongFlag(),
                        key));
    }

    private String objectTypesToString(final Collection<ObjectType> objectTypes) {
        return COMMA_JOINER.join(objectTypes.stream().map(ObjectType::getName).toList());
    }

    private String getRequestUrl(final HttpServletRequest request) {
        if (StringUtils.isNotEmpty(baseUrl)) {
            // TODO: don't include local base URL (lookup from request context and replace)
            return String.format("%s%s", baseUrl, getRequestPath(request).replaceFirst("/rdap", ""));
        }
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

    private Object getRdapObject(final HttpServletRequest request, final Iterable<RpslObject> result) {
        Iterator<RpslObject> rpslIterator = result.iterator();

        if (!rpslIterator.hasNext()) {
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }

        final RpslObject resultObject = rpslIterator.next();

        if (rpslIterator.hasNext()) {
            throw new RdapException("500 Internal Error", "Unexpected result size: " + Iterators.size(rpslIterator),
                    HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        if (RdapObjectMapper.isIANABlock(resultObject)){
            throw new RdapException("404 Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }

        return rdapObjectMapper.map(
                getRequestUrl(request),
                resultObject,
                abuseCFinder.getAbuseContact(resultObject).orElse(null));
    }

}
