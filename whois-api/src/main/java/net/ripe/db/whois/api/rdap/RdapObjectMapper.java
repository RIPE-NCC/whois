package net.ripe.db.whois.api.rdap;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.ws.rs.InternalServerErrorException;
import net.ripe.commons.ip.AbstractIpRange;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.db.whois.api.TopLevelFilter;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Event;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.IpCidr0;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.RdapRequestType;
import net.ripe.db.whois.api.rdap.domain.RelationType;
import net.ripe.db.whois.api.rdap.domain.Remark;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.api.rdap.domain.Status;
import net.ripe.db.whois.common.DateUtil;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.DsRdata;
import net.ripe.db.whois.common.rpsl.attrs.NServer;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.planner.AbuseContact;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.ripe.db.whois.api.rdap.RdapConformance.GEO_FEED_1;
import static net.ripe.db.whois.api.rdap.RedactionObjectMapper.mapRedactions;
import static net.ripe.db.whois.api.rdap.domain.Status.ACTIVE;
import static net.ripe.db.whois.api.rdap.domain.Status.RESERVED;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.GROUP;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.INDIVIDUAL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.ORGANISATION;
import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_MAILBOX;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADDRESS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADMIN_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
import static net.ripe.db.whois.common.rpsl.AttributeType.DESCR;
import static net.ripe.db.whois.common.rpsl.AttributeType.DS_RDATA;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.FAX_NO;
import static net.ripe.db.whois.common.rpsl.AttributeType.GEOFEED;
import static net.ripe.db.whois.common.rpsl.AttributeType.GEOLOC;
import static net.ripe.db.whois.common.rpsl.AttributeType.IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.LANGUAGE;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_BY;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_IRT;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.PHONE;
import static net.ripe.db.whois.common.rpsl.AttributeType.REMARKS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;
import static net.ripe.db.whois.common.rpsl.AttributeType.TECH_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.ZONE_C;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;

@Component
public class RdapObjectMapper {

    private static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/data-tools/support/documentation/terms";

    private static final String GEOFEED_CONTENT_TYPE = "application/geofeed+csv";
    private static final Link COPYRIGHT_LINK = new Link(TERMS_AND_CONDITIONS, "copyright", TERMS_AND_CONDITIONS, null, null, null);
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapObjectMapper.class);
    private static final String APPLICATION_RDAP_JSON = "application/rdap+json";
    private static final String PATH_DELIMITER = "/";

    private final NoticeFactory noticeFactory;
    private final RpslObjectDao rpslObjectDao;
    private final ReservedResources reservedResources;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final String port43;
    private final String rdapRirSearchSkeleton;
    private static final Map<AttributeType, Role> CONTACT_ATTRIBUTE_TO_ROLE_NAME = Map.of(
            ADMIN_C, Role.ADMINISTRATIVE,
            TECH_C, Role.TECHNICAL,
            MNT_BY, Role.REGISTRANT,
            ZONE_C, Role.ZONE,
            ORG, Role.REGISTRANT,// TODO: [MA] both mnt_by and org have same role
            MNT_IRT, Role.ABUSE);

    @Autowired
    public RdapObjectMapper(
            final NoticeFactory noticeFactory,
            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
            final ReservedResources reservedResources,
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            @Value("${rdap.port43:}") final String port43,
            @Value("${rdap.public.baseUrl:}") final String baseUrl) {
        this.noticeFactory = noticeFactory;
        this.rpslObjectDao = rpslObjectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.port43 = port43;
        this.reservedResources = reservedResources;
        this.rdapRirSearchSkeleton = baseUrl + "/%s/rirSearch1/%s/%s";
    }

    public Object map(final String requestUrl,
                      final RpslObject rpslObject,
                      @Nullable final AbuseContact abuseContact) {
        return mapCommons(getRdapObject(requestUrl, rpslObject, abuseContact), requestUrl);
    }

    public Object mapSearch(final String requestUrl, final List<RpslObject> objects, final int maxResultSize) {
        final SearchResult searchResult = new SearchResult();
        for (final RpslObject object : objects) {
            switch (object.getType()){
                case DOMAIN -> {
                    final Domain domain = (Domain) getRdapObject(requestUrl, object, null);
                    mapRedactions(domain);
                    searchResult.addDomainSearchResult(domain);
                }
                case INET6NUM, INETNUM -> {
                    final Ip ip = (Ip) getRdapObject(requestUrl, object, null);
                    mapRedactions(ip);
                    searchResult.addIpSearchResult(ip);
                }
                case AUT_NUM -> {
                    final Autnum autnum = (Autnum) getRdapObject(requestUrl, object, null);
                    mapRedactions(autnum);
                    searchResult.addAutnumSearchResult(autnum);
                }
                default -> {
                    final Entity entity = (Entity) getRdapObject(requestUrl, object, null);
                    mapRedactions(entity);
                    searchResult.addEntitySearchResult(entity);
                }
            }
        }

        if (objects.size() == maxResultSize) {
            final Notice notice = new Notice();
            notice.setTitle(String.format("limited search results to %s maximum" , maxResultSize));
            searchResult.getNotices().add(notice);
        }

        final RdapObject rdapObject = mapCommonNoticesAndPort(searchResult, requestUrl);
        mapCommonLinks(rdapObject, requestUrl);
        mapRirSearchConformanceWhenSearch(rdapObject, requestUrl);
        return mapCommonConformances(rdapObject);
    }

    public Object mapDomainEntity(
                final String requestUrl,
                final RpslObject domainResult,
                final RpslObject inetnumResult) {
        final RdapObject domain = getRdapObject(requestUrl, domainResult, null);
        if (inetnumResult != null) {
            domain.setNetwork((Ip) getRdapObject(requestUrl, inetnumResult, null));
        }
        final RdapObject rdapObject = mapCommonNoticesAndPort(domain, requestUrl);
        rdapObject.getLinks().add(COPYRIGHT_LINK);

        mapRedactions(rdapObject);
        return mapCommonConformances(rdapObject);
    }

    public Object mapOrganisationEntity(final String requestUrl,
                                        final RpslObject organisationObject,
                                        final List<RpslObjectInfo> autnumResult,
                                        final List<RpslObjectInfo> inetnumResult,
                                        final List<RpslObjectInfo> inet6numResult,
                                        final int maxResultSize) {

        final List<Autnum> autnums = mapAutnums(requestUrl, autnumResult);

        final List<RpslObjectInfo> topLevelInetnums = new TopLevelFilter<Ipv4Resource>(inetnumResult).getTopLevelValues();
        final List<RpslObjectInfo> topLevelInet6nums = new TopLevelFilter<Ipv4Resource>(inet6numResult).getTopLevelValues();

        final RdapObject organisation = getRdapObject(requestUrl, organisationObject, null);
        final List<Ip> networks = mapNetworks(requestUrl, topLevelInetnums, topLevelInet6nums, maxResultSize);

        if ((topLevelInetnums.size() + topLevelInet6nums.size()) > maxResultSize) {
            final Notice outOfLimitNotice = new Notice();
            outOfLimitNotice.setTitle(String.format("limited networks attribute results to %s maximum" , maxResultSize));
            organisation.getNotices().add(outOfLimitNotice);
        }

        organisation.setNetworks(networks);
        organisation.setAutnums(autnums);

        return mapCommons(organisation, requestUrl);
    }

    public RdapObject mapError(final int errorCode, final String errorTitle, final List<String> errorDescriptions) {
        if (Strings.isNullOrEmpty(errorTitle)) {
            throw new IllegalStateException("title is mandatory");
        }
        final RdapObject rdapObject = mapCommons(new RdapObject(), null);

        rdapObject.setErrorCode(errorCode);
        rdapObject.setErrorTitle(errorTitle);
        rdapObject.setDescription(errorDescriptions);
        return rdapObject;
    }

    public RdapObject mapAutnumError(final int errorCode, final String errorTitle, final List<String> errorDescriptions){
        final RdapObject rdapObject = mapError(errorCode, errorTitle, errorDescriptions);
        rdapObject.getRdapConformance().add(RdapConformance.FLAT_MODEL.getValue());

        return rdapObject;
    }

    public RdapObject mapHelp(final String requestUrl) {
        final RdapObject rdapObject = mapCommonNoticesAndPort(new RdapObject(), requestUrl);
        mapCommonLinks(rdapObject, requestUrl);
        rdapObject.getRdapConformance().addAll(Stream.of(RdapConformance.values()).map(RdapConformance::getValue).toList());
        return rdapObject;
    }

    private List<Autnum> mapAutnums(final String requestUrl, final List<RpslObjectInfo> autnumResult) {
        return autnumResult.stream()
                .map(this::getRpslObject)
                .filter(Objects::nonNull)
                .map(rpslObject -> (Autnum)getRdapObject(requestUrl, rpslObject, null))
                .collect(Collectors.toList());
    }

    private List<Ip> mapNetworks(final String requestUrl, final List<RpslObjectInfo> inetnums, final List<RpslObjectInfo> inet6nums, final int maxResultSize) {
        return Stream.concat(inet6nums.stream(), inetnums.stream())
                .limit(maxResultSize)
                .map(this::getRpslObject)
                .filter(Objects::nonNull)
                .map(rpslObject -> (Ip)getRdapObject(requestUrl, rpslObject, null))
                .toList();
    }

    @Nullable
    private RpslObject getRpslObject(final RpslObjectInfo rpslObjectInfo) {
        return getRpslObject(rpslObjectInfo.getObjectId());
    }

    @Nullable
    private RpslObject getRpslObject(final int objectId) {
        try {
            return rpslObjectDao.getById(objectId);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private RdapObject getRdapObject(final String requestUrl, final RpslObject rpslObject, @Nullable final AbuseContact abuseContact) {
        RdapObject rdapResponse;
        final ObjectType rpslObjectType = rpslObject.getType();

        try {
            rdapResponse = switch (rpslObjectType) {
                case DOMAIN -> createDomain(rpslObject, requestUrl);
                case AUT_NUM -> createAutnumResponse(rpslObject, requestUrl);
                case AS_BLOCK -> createAsBlockResponse(rpslObject, requestUrl);
                case INETNUM, INET6NUM -> createIp(rpslObject, requestUrl);
                case PERSON, ROLE, MNTNER, ORGANISATION -> createEntity(rpslObject, requestUrl);
                default -> throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
            };
        } catch (IllegalArgumentException ex){
            throw new RdapException("400 Bad Request", ex.getMessage(), HttpStatus.BAD_REQUEST_400);
        }

        if (abuseContact != null) {
            if (abuseContact.isSuspect() && abuseContact.getOrgId() != null) {
                rdapResponse.getRemarks().add(createRemark(rpslObject.getKey(), abuseContact));
            }
            rdapResponse.getEntitySearchResults().add(createEntity(abuseContact.getAbuseRole(), Role.ABUSE, requestUrl, true));
        }

        if (!rpslObject.getValuesForAttribute(DESCR).isEmpty() || !rpslObject.getValuesForAttribute(REMARKS).isEmpty()) {
            rdapResponse.getRemarks().add(createRemark(rpslObject));
        }

        rdapResponse.getEvents().add(createEvent(DateUtil.fromString(rpslObject.getValueForAttribute(AttributeType.CREATED)), Action.REGISTRATION));
        rdapResponse.getEvents().add(createEvent(DateUtil.fromString(rpslObject.getValueForAttribute(AttributeType.LAST_MODIFIED)), Action.LAST_CHANGED));
        mapConformanceAndLinksForRelation(rdapResponse, requestUrl);
        rdapResponse.getNotices().addAll(noticeFactory.generateNotices(requestUrl, rpslObject));
        return rdapResponse;
    }

    private RdapObject mapCommons(final RdapObject rdapResponse, final String requestUrl) {
        final RdapObject rdapObject = mapCommonNoticesAndPort(rdapResponse, requestUrl);
        mapCommonLinks(rdapObject, requestUrl);
        mapRedactions(rdapResponse);
        return mapCommonConformances(rdapObject);
    }


    private void mapConformanceAndLinksForRelation(final RdapObject rdapObject, final String requestUrl){
        mapCommonRelationLinks(rdapObject, requestUrl);
        mapRirSearchConformanceWhenLookup(rdapObject, requestUrl);
    }

    private void mapCommonLinks(final RdapObject rdapResponse, final String requestUrl) {
        if (requestUrl != null) {
            rdapResponse.getLinks().add(new Link(requestUrl, "self", requestUrl, null, null, null));
        }
        rdapResponse.getLinks().add(COPYRIGHT_LINK);
    }

    private void mapEntityLinks(final Entity entity, final String requestUrl, final CIString attributeValue) {
        if (requestUrl != null) {
            URL url;
            try {
                url = new URL(requestUrl);
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("Malformed Url");
            }
            entity.getLinks().add(new Link(requestUrl, "self",
                    url.getProtocol() + "://" + url.getHost() + PATH_DELIMITER + entity.getObjectClassName() + PATH_DELIMITER + attributeValue,
                    null, null, null));
        }

        entity.getLinks().add(COPYRIGHT_LINK);
    }


    private RdapObject mapCommonNoticesAndPort(final RdapObject rdapResponse, final String requestUrl) {
        rdapResponse.getNotices().add(noticeFactory.generateTnC(requestUrl));
        rdapResponse.setPort43(port43);
        return rdapResponse;
    }

    private RdapObject mapCommonConformances(final RdapObject rdapResponse) {
        rdapResponse.getRdapConformance().addAll(List.of(RdapConformance.CIDR_0.getValue(),
            RdapConformance.LEVEL_0.getValue(), RdapConformance.NRO_PROFILE_0.getValue(), RdapConformance.REDACTED.getValue()));
        return rdapResponse;
    }

    private Ip createIp(final RpslObject rpslObject, final String requestUrl) {
        final Ip ip = new Ip();
        final IpInterval ipInterval = IpInterval.parse(rpslObject.getKey());
        ip.setHandle(rpslObject.getKey().toString());
        ip.setIpVersion(rpslObject.getType() == INET6NUM? "v6" : "v4");
        ip.setStartAddress(toIpRange(ipInterval).start().toString());
        ip.setEndAddress(toIpRange(ipInterval).end().toString());
        ip.setName(rpslObject.getValueForAttribute(AttributeType.NETNAME).toString());
        ip.setType(rpslObject.getValueForAttribute(AttributeType.STATUS).toString());
        if (!isIANABlock(rpslObject)) {
            ip.setParentHandle(lookupParentHandle(ipInterval));
        }
        ip.setStatus(Collections.singletonList(getResourceStatus(rpslObject).getValue()));
        ip.setCidr0_cidrs(getIpCidr0Notation(toIpRange(ipInterval)));

        final String country = getAndHandleMultipleAttributes(rpslObject, COUNTRY, ip);
        if(country != null) {
            ip.setCountry(country);
        }

        final String language = getAndHandleMultipleAttributes(rpslObject, LANGUAGE, ip);
        if(language != null) {
            ip.setLang(language);
        }

        setGeoFeed(rpslObject, ip, requestUrl);

        this.mapContactEntities(ip, rpslObject, requestUrl);
        return ip;
    }

    public static boolean isIANABlock(final RpslObject rpslObject) {
        return rpslObject.getKey().toString().equals("::/0") || rpslObject.getKey().toString().equals("0.0.0.0 - 255.255.255.255");
    }
    private Status getResourceStatus(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case AUT_NUM:
                final Long asn = Long.parseLong(StringUtils.substringAfter(rpslObject.getKey().toUpperCase(), "AS"));
                return reservedResources.isReservedAsNumber(asn) ? RESERVED : ACTIVE;
            case AS_BLOCK:
                return reservedResources.isReservedAsBlock(rpslObject.getKey().toUpperCase()) ? RESERVED : ACTIVE;
            case INETNUM:
            case INET6NUM:
                return reservedResources.isBogon(rpslObject.getKey().toString()) ? RESERVED : ACTIVE;
            default:
                throw new RdapException("400 Bad Request", "Unhandled object type: " + rpslObject.getType(),
                        HttpStatus.BAD_REQUEST_400);
        }
    }

    private List<IpCidr0> getIpCidr0Notation(final AbstractIpRange ipRange) {
       return Lists.newArrayList(
               Iterables.transform(ipRange.splitToPrefixes(), (Function<AbstractIpRange, IpCidr0>) prefix -> {
                   final String[] cidrNotation = prefix.toStringInCidrNotation().split(PATH_DELIMITER);
                   final IpCidr0 ipCidr0 = new IpCidr0();
                   ipCidr0.setLength(Integer.parseInt(cidrNotation[1]));
                   if (prefix instanceof Ipv4Range) {
                       ipCidr0.setV4prefix(cidrNotation[0]);
                   } else {
                       ipCidr0.setV6prefix(cidrNotation[0]);
                   }
                   return ipCidr0;
               }));
    }

    private static AbstractIpRange toIpRange(final IpInterval interval) {
        return interval instanceof Ipv4Resource? toIpv4Range((Ipv4Resource)interval) : toIpv6Range((Ipv6Resource)interval);
    }

    private static AbstractIpRange toIpv4Range(final Ipv4Resource ipv4Resource) {
        return Ipv4Range.from(ipv4Resource.begin()).to(ipv4Resource.end());
    }

    private static AbstractIpRange toIpv6Range(final Ipv6Resource ipv6Resource) {
        return Ipv6Range.from(ipv6Resource.begin()).to(ipv6Resource.end());
    }

    private String lookupParentHandle(final IpInterval ipInterval) {
        final RpslObject parentRpslObject = getRpslObject(lookupParentIpEntry(ipInterval).getObjectId());
        if (parentRpslObject == null) {
            throw new RdapException("500 Internal Error", "No parentHandle for " + ipInterval, HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        return parentRpslObject.getKey().toString();
    }

    private IpEntry lookupParentIpEntry(final IpInterval ipInterval) {
        if (ipInterval instanceof Ipv4Resource) {
            final List<Ipv4Entry> firstLessSpecific = ipv4Tree.findFirstLessSpecific((Ipv4Resource) ipInterval);
            if (firstLessSpecific.isEmpty()) {
                throw new RdapException("500 Internal Error", "No parentHandle for " + ipInterval, HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            return firstLessSpecific.get(0);
        } else {
            if (ipInterval instanceof Ipv6Resource) {
                final List<Ipv6Entry> firstLessSpecific = ipv6Tree.findFirstLessSpecific((Ipv6Resource) ipInterval);
                if (firstLessSpecific.isEmpty()) {
                    throw new RdapException("500 Internal Error", "No parentHandle for " + ipInterval, HttpStatus.INTERNAL_SERVER_ERROR_500);
                }
                return firstLessSpecific.get(0);
            } else {
                throw new RdapException("500 Internal Error", "Unknown interval type " + ipInterval.getClass().getName(), HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
        }
    }

    private static Remark createRemark(final RpslObject rpslObject) {
        final List<String> descriptions = Lists.newArrayList();

        for (final CIString description : rpslObject.getValuesForAttribute(DESCR)) {
            descriptions.add(description.toString());
        }

        for (final CIString remark : rpslObject.getValuesForAttribute(REMARKS)) {
            descriptions.add(remark.toString());
        }

        return new Remark(descriptions);
    }

    private static Remark createRemark(final CIString key, final AbuseContact abuseContact) {
        return new Remark(
           Collections.singletonList(
               QueryMessages.unvalidatedAbuseCShown(key, abuseContact.getAbuseMailbox(), abuseContact.getOrgId()).toString().replaceAll("% ", "")));
    }

    private static Event createEvent(final LocalDateTime lastChanged, final Action action) {
        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction(action);
        lastChangedEvent.setEventDate(lastChanged);
        return lastChangedEvent;
    }

    private void mapContactEntities(final RdapObject rdapObject, final RpslObject rpslObject, final String requestUrl) {
        final Map<CIString, Entity> contactsEntities = Maps.newTreeMap();
        final List<RpslAttribute> filteredAttributes = rpslObject.getAttributes().stream().filter( rpslAttribute -> CONTACT_ATTRIBUTE_TO_ROLE_NAME.containsKey(rpslAttribute.getType())).collect(Collectors.toList());

        filteredAttributes.forEach( rpslAttribute -> {
            for (final CIString value : rpslAttribute.getCleanValues()) {
                if (contactsEntities.containsKey(value)) {
                    contactsEntities.get(value).getRoles().add(CONTACT_ATTRIBUTE_TO_ROLE_NAME.get(rpslAttribute.getType()));
                    continue;
                }

                final Entity entity = new Entity();
                entity.setHandle(value.toString());
                entity.getRoles().add(CONTACT_ATTRIBUTE_TO_ROLE_NAME.get(rpslAttribute.getType()));

                mapContactEntityVCard(value, rpslAttribute.getType(), entity);
                mapEntityLinks(entity, requestUrl, value);

                contactsEntities.put(value, entity);
            }
        });
        rdapObject.getEntitySearchResults().addAll(contactsEntities.values());
    }

    private void mapContactEntityVCard(final CIString attributeValue, final AttributeType type, final Entity entity) {
        final RpslObject referencedRpslObject = getRpslObjectByAttributeType(attributeValue, type);
        if (referencedRpslObject == null) {
            return;
        }

        createVCard(entity, referencedRpslObject, true);
    }

    private RpslObject getRpslObjectByAttributeType(final CIString attributeValue, final AttributeType type) {
        for (final ObjectType objectType : type.getReferences()){
            RpslObject referencedRpslObject = rpslObjectDao.getByKeyOrNull(objectType, attributeValue);
            if (referencedRpslObject != null){
               return referencedRpslObject;
            }
        }
        return null;
    }

    private Entity createEntity(final RpslObject rpslObject, final String requestUrl) {
        // top-level entity has no role and has unfiltered information
        return createEntity(rpslObject, null, requestUrl, false);
    }

    private Entity createEntity(final RpslObject rpslObject, @Nullable final Role role, final String requestUrl, final boolean filtered) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        if (role != null) {
            entity.getRoles().add(role);
        }

        final String language = getAndHandleMultipleAttributes(rpslObject, LANGUAGE, entity);
        if(language != null) {
            entity.setLang(language);
        }

        createVCard(entity, rpslObject, filtered);
        this.mapContactEntities(entity, rpslObject, requestUrl);

        return entity;
    }

    private Autnum createAutnumResponse(final RpslObject rpslObject, final String requestUrl) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());
        autnum.setName(rpslObject.getValueForAttribute(AttributeType.AS_NAME).toString().replace(" ", ""));
        final Long asNumber = Long.parseLong(StringUtils.substringAfter(rpslObject.getKey().toUpperCase(), "AS"));
        autnum.setStartAutnum(asNumber);
        autnum.setEndAutnum(asNumber);
        autnum.setStatus(Collections.singletonList(getResourceStatus(rpslObject).getValue()));
        this.mapContactEntities(autnum, rpslObject, requestUrl);
        autnum.getRdapConformance().add(RdapConformance.FLAT_MODEL.getValue());
        return autnum;
    }

    private Autnum createAsBlockResponse(final RpslObject rpslObject, final String requestUrl) {
        final Autnum autnum = new Autnum();
        final String key = rpslObject.getValueForAttribute(AttributeType.AS_BLOCK).toString();
        final AsBlockRange blockRange = getAsBlockRange(key);
        autnum.setHandle(blockRange.getBeginWithPrefix());
        //TODO :check what should be the name
        final String asName = String.join("-", blockRange.getBeginWithPrefix(), blockRange.getEndWithPrefix());
        autnum.setName(asName);
        autnum.setStartAutnum(blockRange.getBegin());
        autnum.setEndAutnum(blockRange.getEnd());
        autnum.setStatus(Collections.singletonList(getResourceStatus(rpslObject).getValue()));
        this.mapContactEntities(autnum, rpslObject, requestUrl);
        return autnum;
    }

    private Domain createDomain(final RpslObject rpslObject, final String requestUrl) {
        final Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(IpInterval.addTrailingDot(rpslObject.getKey().toString()));

        final Map<CIString, Set<IpInterval>> hostnameMap = Maps.newHashMap();

        for (final CIString nserverValue : rpslObject.getValuesForAttribute(AttributeType.NSERVER)) {
            final NServer nserver = NServer.parse(nserverValue.toString());
            final CIString hostname = nserver.getHostname();
            final Set<IpInterval> ipIntervalSet;
            if (hostnameMap.containsKey(hostname)) {
                ipIntervalSet = hostnameMap.get(hostname);
            } else {
                ipIntervalSet = Sets.newHashSet();
                hostnameMap.put(hostname, ipIntervalSet);
            }
            final IpInterval ipInterval = nserver.getIpInterval();
            if (ipInterval != null) {
                ipIntervalSet.add(ipInterval);
            }
        }

        for (final CIString hostname : hostnameMap.keySet()) {
            final Nameserver nameserver = new Nameserver();
            nameserver.setLdhName(hostname.toString());

            final Set<IpInterval> ipIntervals = hostnameMap.get(hostname);
            if (!ipIntervals.isEmpty()) {
                final Nameserver.IpAddresses ipAddresses = new Nameserver.IpAddresses();
                for (IpInterval ipInterval : ipIntervals) {
                    if (ipInterval instanceof Ipv4Resource) {
                        ipAddresses.getIpv4().add(IpInterval.asIpInterval(ipInterval.beginAsInetAddress()).toString());
                    } else if (ipInterval instanceof Ipv6Resource) {
                        ipAddresses.getIpv6().add(IpInterval.asIpInterval(ipInterval.beginAsInetAddress()).toString());
                    }
                }
                nameserver.setIpAddresses(ipAddresses);
            }

            domain.getNameservers().add(nameserver);
        }

        final Domain.SecureDNS secureDNS = new Domain.SecureDNS();
        secureDNS.setDelegationSigned(false);

        for (final CIString rdata : rpslObject.getValuesForAttribute(DS_RDATA)) {
            final DsRdata dsRdata = DsRdata.parse(rdata);
            secureDNS.setDelegationSigned(true);
            final Domain.SecureDNS.DsData dsData = new Domain.SecureDNS.DsData();
            dsData.setKeyTag(dsRdata.getKeytag());
            dsData.setAlgorithm(dsRdata.getAlgorithm());
            dsData.setDigestType(dsRdata.getDigestType());
            dsData.setDigest(dsRdata.getDigestHexString());
            secureDNS.getDsData().add(dsData);
        }

        if (secureDNS.isDelegationSigned()) {
            domain.setSecureDNS(secureDNS);
        }
        this.mapContactEntities(domain, rpslObject, requestUrl);

        return domain;
    }

    private static void createVCard(final Entity entity, final RpslObject rpslObject, final boolean filtered) {
        final VCardBuilder builder = new VCardBuilder();
        builder.addVersion();

        switch (rpslObject.getType()) {
            case PERSON -> builder.addFn(rpslObject.getValueForAttribute(PERSON)).addKind(INDIVIDUAL);
            case MNTNER -> builder.addFn(rpslObject.getValueForAttribute(AttributeType.MNTNER)).addKind(INDIVIDUAL);
            case ORGANISATION -> builder.addFn(rpslObject.getValueForAttribute(ORG_NAME)).addKind(ORGANISATION);
            case ROLE -> builder.addFn(rpslObject.getValueForAttribute(ROLE)).addKind(GROUP);
            case IRT -> builder.addFn(rpslObject.getValueForAttribute(IRT)).addKind(GROUP);
            default -> {
            }
        }
        builder.addAdr(rpslObject.getValuesForAttribute(ADDRESS))
                .addTel(rpslObject.getValuesForAttribute(PHONE))
                .addFax(rpslObject.getValuesForAttribute(FAX_NO))
                .addAbuseMailBox(rpslObject.getValueOrNullForAttribute(ABUSE_MAILBOX))
                .addOrg(rpslObject.getValuesForAttribute(ORG))
                .addGeo(rpslObject.getValuesForAttribute(GEOLOC));

        if (!filtered){
            builder.addEmail(rpslObject.getValuesForAttribute(E_MAIL));
        } else {
            entity.getRedactedRpslAttrs().addAll(rpslObject.findAttributes(E_MAIL));
        }

        entity.setVCardArray(builder.build());
    }

    private static AsBlockRange getAsBlockRange(final String asBlock) {
        try {
            return AsBlockRange.parse(asBlock);
        } catch (AttributeParseException ex) {
            throw new InternalServerErrorException("Invalid AS Block found in database");
        }
    }

    private static String getAndHandleMultipleAttributes(final RpslObject rpslObject, final AttributeType type, final RdapObject rdapObject) {
        final List<RpslAttribute> attributes =  rpslObject.findAttributes(type);
        if(attributes.isEmpty()) {
            return null;
        }

        if(attributes.size() > 1) {
            rdapObject.getRedactedRpslAttrs().addAll(attributes);
        }

        return attributes.get(0).getCleanValue().toString();
    }

    private static void setGeoFeed(final RpslObject rpslObject, final Ip ip, final String requestUrl) {
        ip.getRdapConformance().add(GEO_FEED_1.getValue());

        if(rpslObject.containsAttribute(GEOFEED)) {
            ip.getLinks().add(new Link(requestUrl, "geo", rpslObject.getValueForAttribute(GEOFEED).toString(), null, null, GEOFEED_CONTENT_TYPE));
            return;
        }

        final Optional<RpslAttribute> geoFeedAttribute = rpslObject.findAttributes(DESCR, REMARKS).stream().filter(rpslAttribute -> (rpslAttribute.getCleanValue().toUpperCase()).startsWith(GEOFEED.getName().toUpperCase())).findFirst();
        geoFeedAttribute.ifPresent(rpslAttribute -> {

            final String[] geoFeed = rpslAttribute.getCleanValue().toString().split(" ");
            if(geoFeed.length < 2) {
                LOGGER.warn("Seems like geo feed is not set properly for object {}", rpslObject.getKey());
                return;
            }
            ip.getLinks().add(new Link(requestUrl, "geo", geoFeed[1], null, null, GEOFEED_CONTENT_TYPE));
        });
    }

    private void mapRirSearchConformanceWhenLookup(final RdapObject rdapObject, final String requestUrl){
        if (StringUtils.isEmpty(requestUrl)){
            return;
        }

        if (requestUrl.contains(RdapConformance.RIR_SEARCH_1.getValue())){
            // This could happen if relation type is up or top search, the object is returned like a lookup
            mapRirSearchConformanceWhenSearch(rdapObject, requestUrl);
            return;
        }

        switch (rdapObject) {
            case Ip ip -> ip.getRdapConformance().addAll(List.of(RdapConformance.RIR_SEARCH_1.getValue(), RdapConformance.IPS.getValue()));
            case Autnum autnum -> autnum.getRdapConformance().addAll(List.of(RdapConformance.RIR_SEARCH_1.getValue(), RdapConformance.AUTNUMS.getValue()));
            case Domain domain -> domain.getRdapConformance().add(RdapConformance.RIR_SEARCH_1.getValue());
            default -> {}
        }
    }

    private void mapRirSearchConformanceWhenSearch(final RdapObject rdapObject, final String requestUrl){
        rdapObject.getRdapConformance().add(RdapConformance.RIR_SEARCH_1.getValue());
        if (!StringUtils.isEmpty(requestUrl)) {
            if (requestUrl.contains(RdapRequestType.IPS.name().toLowerCase())) {
                rdapObject.getRdapConformance().addAll(List.of(RdapConformance.IPS.getValue(), RdapConformance.IP_SEARCH_RESULTS.getValue()));
                return;
            }
            if (requestUrl.contains(RdapRequestType.AUTNUMS.name().toLowerCase())) {
                rdapObject.getRdapConformance().addAll(List.of(RdapConformance.AUTNUMS.getValue(), RdapConformance.AUTNUM_SEARCH_RESULTS.getValue()));
            }
        }
    }


    private void mapCommonRelationLinks(final RdapObject rdapResponse, final String requestUrl){
        if (StringUtils.isEmpty(requestUrl) || requestUrl.contains(RdapConformance.RIR_SEARCH_1.getValue())){
            return;
        }
        switch (rdapResponse){
            case Ip ip -> mapCommonRelationLinks(rdapResponse, requestUrl, RdapRequestType.IPS.name().toLowerCase(), ip.getHandle());
            case net.ripe.db.whois.api.rdap.domain.Domain domain -> mapCommonRelationLinks(rdapResponse, requestUrl, RdapRequestType.DOMAINS.name().toLowerCase(), domain.getHandle());
            case Autnum autnum -> mapCommonRelationLinks(rdapResponse, requestUrl, RdapRequestType.AUTNUMS.name().toLowerCase(), autnum.getHandle());
            default -> {}
        }
    }

    private void mapCommonRelationLinks(final RdapObject rdapResponse, final String requestUrl, final String objectType, final String handle){
        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.UP.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.UP.getValue(), handle), APPLICATION_RDAP_JSON, null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, "up-active", String.format(rdapRirSearchSkeleton + "?status=active",
                objectType, RelationType.UP.getValue(), handle), APPLICATION_RDAP_JSON, null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.DOWN.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.DOWN.getValue(), handle), APPLICATION_RDAP_JSON, null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.TOP.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.TOP.getValue(), handle), APPLICATION_RDAP_JSON, null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, "top-active", String.format(rdapRirSearchSkeleton + "?status=active",
                objectType, RelationType.TOP.getValue(), handle), APPLICATION_RDAP_JSON, null, null));

        rdapResponse.getLinks().add(new Link(requestUrl, RelationType.BOTTOM.getValue(), String.format(rdapRirSearchSkeleton,
                objectType, RelationType.BOTTOM.getValue(), handle), APPLICATION_RDAP_JSON, null, null));
    }

}
