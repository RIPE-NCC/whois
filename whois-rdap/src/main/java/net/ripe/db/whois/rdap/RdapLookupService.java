package net.ripe.db.whois.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcReferenceReadOnlyDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import net.ripe.db.whois.query.planner.AbuseContact;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.rdap.domain.RdapObject;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static net.ripe.db.whois.common.rpsl.ObjectType.AUT_NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Service
public class RdapLookupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdapLookupService.class);

    private static final Joiner COMMA_JOINER = Joiner.on(",");

    private final RdapObjectMapper rdapObjectMapper;

    private final int maxEntityResultSize;

    private final String baseUrl;

    private final RdapQueryHandler rdapQueryHandler;

    private final JdbcReferenceReadOnlyDao jdbcReferenceReadOnlyDao;

    private final AbuseCFinder abuseCFinder;

    private final ReservedResources reservedResources;

    /**
     *
     * @param baseUrl
     * @param maxEntityResultSize: used for networks maximum retrieved objects, if we retrieve more objects than
     *                           the maximum value we truncate the response and we add a notification in the response.
     * @param rdapObjectMapper
     * @param rdapQueryHandler
     * @param sourceContext
     * @param jdbcReferenceReadOnlyDao
     * @param abuseCFinder
     */

    @Autowired
    public RdapLookupService(@Value("${rdap.public.baseUrl:}") final String baseUrl,
                             @Value("${rdap.entity.max.results:100}") final int maxEntityResultSize,
                             final RdapObjectMapper rdapObjectMapper,
                             final ReservedResources reservedResources,
                             final RdapQueryHandler rdapQueryHandler,
                             final JdbcReferenceReadOnlyDao jdbcReferenceReadOnlyDao,
                             final AbuseCFinder abuseCFinder){
        this.rdapObjectMapper = rdapObjectMapper;
        this.maxEntityResultSize = maxEntityResultSize;
        this.baseUrl = baseUrl;
        this.rdapQueryHandler = rdapQueryHandler;
        this.jdbcReferenceReadOnlyDao = jdbcReferenceReadOnlyDao;
        this.abuseCFinder = abuseCFinder;
        this.reservedResources = reservedResources;
        LOGGER.info("testing logging");
    }

    protected Object lookupObject(final HttpServletRequest request, final Set<ObjectType> objectTypes,
                                 final String key) {
        final List<RpslObject> result = rdapQueryHandler.handleQueryStream(getQueryObject(objectTypes, key), request).toList();
        return getRdapObject(request, result, key);
    }

    protected Object lookupForAutNum(final HttpServletRequest request, final String key) {
        try {
            final Query query = getQueryObject(ImmutableSet.of(AUT_NUM), key);
            List<RpslObject> result = rdapQueryHandler.handleAutNumQuery(query, request);

            return getRdapObject(request, result, key);
        } catch (RdapException ex){
            throw new AutnumException(ex.getErrorTitle(), ex.getErrorDescription(), ex.getErrorCode());
        }
    }

    protected Object lookupForDomain(final HttpServletRequest request, final String reverseIp, final String key) {
        final Stream<RpslObject> domainResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(DOMAIN), reverseIp), request);
        final Stream<RpslObject> inetnumResult =
                rdapQueryHandler.handleQueryStream(getQueryObject(ImmutableSet.of(INETNUM, INET6NUM), key), request);

        return getDomainEntity(request, domainResult, inetnumResult);
    }

    protected Object getDomainEntity(final HttpServletRequest request, final Stream<RpslObject> domainResult,
                                     final Stream<RpslObject> inetnumResult) {
        final Iterator<RpslObject> domainIterator = domainResult.iterator();
        final Iterator<RpslObject> inetnumIterator = inetnumResult.iterator();
        if (!domainIterator.hasNext()) {
            throw new RdapException("Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }
        final RpslObject domainObject = domainIterator.next();
        final RpslObject inetnumObject = inetnumIterator.hasNext() ? inetnumIterator.next() : null;

        if (domainIterator.hasNext() || inetnumIterator.hasNext()) {
            throw new RdapException("Internal Error", "More than one object matches primary key", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
        return rdapObjectMapper.mapDomainEntity(getRequestUrl(request), domainObject, inetnumObject);
    }

    protected Object lookupForOrganisation(final HttpServletRequest request, final String key) {
        final List<RpslObject> organisationResult = rdapQueryHandler.handleQueryStream(getQueryObject(Set.of(ORGANISATION), key), request).toList();

        final RpslObject organisation = switch (organisationResult.size()) {
            case 0 ->
                    throw new RdapException("Not Found", "Requested organisation not found: " + key, HttpStatus.NOT_FOUND_404);
            case 1 -> organisationResult.getFirst();
            default ->
                    throw new RdapException("Internal Error", "More than one object matches primary key", HttpStatus.INTERNAL_SERVER_ERROR_500);
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


    private Set<RpslObjectInfo> getReferences(final RpslObject organisation) {
        return jdbcReferenceReadOnlyDao.getReferences(organisation);
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
                getAbuseContact(organisation),
                maxEntityResultSize);
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

    private Object getRdapObject(final HttpServletRequest request, final Iterable<RpslObject> result,  final String requestedkey) {
        Iterator<RpslObject> rpslIterator = result.iterator();
        LOGGER.info ("Checking for RDAP key {}", requestedkey);

        if (!rpslIterator.hasNext()) {
            throw new RdapException("Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404);
        }

        RpslObject resultObject = rpslIterator.next();

        if (rpslIterator.hasNext()) {
            throw new RdapException("Internal Error", "More than one object matches primary key", HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        if (RdapObjectMapper.isIANABlock(resultObject)){
            LOGGER.info("Returned result is an IANA Block, checking for administrative block");
            return  getAdministrativeBlock(getRequestUrl(request), requestedkey).orElseThrow(()-> new RdapException("Not Found", "Requested object not found", HttpStatus.NOT_FOUND_404));
        }

        return rdapObjectMapper.map(
                getRequestUrl(request),
                resultObject,
                getAbuseContact(resultObject));
    }

    public Optional<RdapObject> getAdministrativeBlock(final String requestUrl, final String requestedkey) {
        final RpslObject adminstrativeBlock = reservedResources.getAdministrativeRange(requestedkey);
        return adminstrativeBlock != null ? Optional.of((RdapObject) rdapObjectMapper.map(requestUrl, adminstrativeBlock, null)): Optional.empty();
    }

    @Nullable
    private AbuseContact getAbuseContact(final RpslObject resultObject) {
        return abuseCFinder.getAbuseContact(resultObject).orElse(null);
    }

}
