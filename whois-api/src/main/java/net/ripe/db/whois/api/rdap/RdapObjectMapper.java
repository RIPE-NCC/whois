package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.commons.ip.AbstractIpRange;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Domain;

import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.SearchResult;

import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;

import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Remark;
import net.ripe.db.whois.api.rdap.domain.Event;

import net.ripe.db.whois.api.rdap.domain.vcard.VCard;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.INDIVIDUAL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.ORGANISATION;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardKind.GROUP;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.iptree.Ipv6Entry;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.rpsl.AttributeType;
import static net.ripe.db.whois.common.rpsl.AttributeType.ABUSE_MAILBOX;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.DsRdata;
import net.ripe.db.whois.common.rpsl.attrs.NServer;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.ws.rs.InternalServerErrorException;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

import java.util.HashMap;

import java.util.stream.Collectors;


import static net.ripe.db.whois.common.rpsl.AttributeType.ORG_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADMIN_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.TECH_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.GEOLOC;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static net.ripe.db.whois.common.rpsl.AttributeType.FAX_NO;
import static net.ripe.db.whois.common.rpsl.AttributeType.PERSON;
import static net.ripe.db.whois.common.rpsl.AttributeType.PHONE;
import static net.ripe.db.whois.common.rpsl.AttributeType.ROLE;
import static net.ripe.db.whois.common.rpsl.AttributeType.DS_RDATA;
import static net.ripe.db.whois.common.rpsl.AttributeType.ZONE_C;
import static net.ripe.db.whois.common.rpsl.AttributeType.MNT_BY;
import static net.ripe.db.whois.common.rpsl.AttributeType.ADDRESS;
import static net.ripe.db.whois.common.rpsl.AttributeType.ORG;
import static net.ripe.db.whois.common.rpsl.ObjectType.DOMAIN;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;

@Component
class RdapObjectMapper {
    private static final String DIRECT_ALLOCATION = "DIRECT ALLOCATION";
    private static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/data-tools/support/documentation/terms";
    private static final Link COPYRIGHT_LINK = new Link(TERMS_AND_CONDITIONS, "copyright", TERMS_AND_CONDITIONS, null, null);

    private static final List<String> RDAP_CONFORMANCE_LEVEL = Lists.newArrayList("rdap_level_0");

    private static final Map<AttributeType, Role> CONTACT_ATTRIBUTE_TO_ROLE_NAME = Maps.newHashMap();

    static {
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(ADMIN_C, Role.ADMINISTRATIVE);
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(TECH_C, Role.TECHNICAL);
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(MNT_BY, Role.REGISTRANT);
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(ZONE_C, Role.ZONE);
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(ORG, Role.REGISTRANT); // TODO: [MA] both mnt_by and org have same role
    }

    private final NoticeFactory noticeFactory;
    private final RpslObjectDao rpslObjectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final String port43;

    @Autowired
    public RdapObjectMapper(
            final NoticeFactory noticeFactory,
            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            @Value("${rdap.port43:}") final String port43) {
        this.noticeFactory = noticeFactory;
        this.rpslObjectDao = rpslObjectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.port43 = port43;
    }

    public Object map(final String requestUrl, final RpslObject rpslObject, final LocalDateTime lastChangedTimestamp, @Nullable final RpslObject abuseContact) {
        return mapCommons(getRdapObject(requestUrl, rpslObject, lastChangedTimestamp, abuseContact), requestUrl);
    }

    public Object mapSearch(final String requestUrl, final List<RpslObject> objects, final Iterable<LocalDateTime> localDateTimes) {
        final SearchResult searchResult = new SearchResult();
        final Iterator<LocalDateTime> iterator = localDateTimes.iterator();

        for (final RpslObject object : objects) {
            if (object.getType() == DOMAIN) {
                searchResult.addDomainSearchResult((Domain) getRdapObject(requestUrl, object, iterator.next(), null));
            } else {
                searchResult.addEntitySearchResult((Entity) getRdapObject(requestUrl, object, iterator.next(), null));
            }
        }

        return mapCommons(searchResult, requestUrl);
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

    public RdapObject mapHelp(final String requestUrl) {
        return mapCommons(new RdapObject(), requestUrl);
    }

    private RdapObject getRdapObject(final String requestUrl, final RpslObject rpslObject, final LocalDateTime lastChangedTimestamp, @Nullable final RpslObject abuseContact) {
        RdapObject rdapResponse;
        final ObjectType rpslObjectType = rpslObject.getType();

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject);
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(rpslObject);
                break;
            case AS_BLOCK:
                rdapResponse = createAsBlockResponse(rpslObject);
                break;
            case INETNUM:
            case INET6NUM:
                rdapResponse = createIp(rpslObject);
                break;
            case PERSON:
            case ROLE:
            case MNTNER:
            case ORGANISATION:
                rdapResponse = createEntity(rpslObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
        }

        if (hasRemark(rpslObject)) {
            rdapResponse.getRemarks().add(createRemark(rpslObject));
        }

        rdapResponse.getEvents().add(createEvent(lastChangedTimestamp));

        rdapResponse.getNotices().addAll(noticeFactory.generateNotices(requestUrl, rpslObject));

        if (abuseContact != null) {
            rdapResponse.getEntitySearchResults().add(createEntity(abuseContact, Role.ABUSE));
        }

        return rdapResponse;
    }

    private RdapObject mapCommons(final RdapObject rdapResponse, final String requestUrl) {
        rdapResponse.getNotices().add(noticeFactory.generateTnC(requestUrl));

        rdapResponse.getRdapConformance().addAll(RDAP_CONFORMANCE_LEVEL);

        if (requestUrl != null) {
            rdapResponse.getLinks().add(new Link(requestUrl, "self", requestUrl, null, null));
        }

        rdapResponse.getLinks().add(COPYRIGHT_LINK);

        rdapResponse.setPort43(port43);
        return rdapResponse;
    }

    private Ip createIp(final RpslObject rpslObject) {
        final Ip ip = new Ip();
        final IpInterval ipInterval = IpInterval.parse(rpslObject.getKey());
        ip.setHandle(rpslObject.getKey().toString());

        ip.setIpVersion(rpslObject.getType() == INET6NUM? "v6" : "v4");
        ip.setStartAddress(toIpRange(ipInterval).start().toString());
        ip.setEndAddress(toIpRange(ipInterval).end().toString());
        ip.setName(rpslObject.getValueForAttribute(AttributeType.NETNAME).toString());
        ip.setType(rpslObject.getValueForAttribute(AttributeType.STATUS).toString());
        ip.setParentHandle(lookupParentHandle(ipInterval));

        handleLanguageAttribute(rpslObject, ip);
        handleCountryAttribute(rpslObject, ip);

        ip.getEntitySearchResults().addAll(createContactEntities(rpslObject));

        return ip;
    }

    private static AbstractIpRange toIpRange(IpInterval interval) {
        return interval instanceof Ipv4Resource? toIpv4Range((Ipv4Resource)interval) : toIpv6Range((Ipv6Resource)interval);
    }

    private static AbstractIpRange toIpv4Range(final Ipv4Resource ipv4Resource) {
        return Ipv4Range.from(ipv4Resource.begin()).to(ipv4Resource.end());
    }

    private static AbstractIpRange toIpv6Range(final Ipv6Resource ipv6Resource) {
        return Ipv6Range.from(ipv6Resource.begin()).to(ipv6Resource.end());
    }

    @Nullable
    private String lookupParentHandle(final IpInterval ipInterval) {
        final RpslObject parentRpslObject;
        try {
            parentRpslObject = rpslObjectDao.getById(lookupParentIpEntry(ipInterval).getObjectId());
        } catch (DataAccessException e) {
            throw new IllegalStateException("Couldn't get parent for " + ipInterval.toString());
        }

        if (parentRpslObject == null) {
            throw new IllegalStateException("No parentHandle for " + ipInterval.toString());
        }

        return parentRpslObject.getKey().toString();
    }

    private IpEntry lookupParentIpEntry(final IpInterval ipInterval) {
        if (ipInterval instanceof Ipv4Resource) {
            final List<Ipv4Entry> firstLessSpecific = ipv4Tree.findFirstLessSpecific((Ipv4Resource) ipInterval);
            if (firstLessSpecific.isEmpty()) {
                throw new IllegalStateException("No parent for " + ipInterval.toString());
            }
            return firstLessSpecific.get(0);
        }

        if (ipInterval instanceof Ipv6Resource) {
            final List<Ipv6Entry> firstLessSpecific = ipv6Tree.findFirstLessSpecific((Ipv6Resource) ipInterval);
            if (firstLessSpecific.isEmpty()) {
                throw new IllegalStateException("No parent for " + ipInterval.toString());
            }
            return firstLessSpecific.get(0);
        }
        throw new IllegalStateException("Unknown interval type " + ipInterval.getClass().getName());
    }

    private static Remark createRemark(final RpslObject rpslObject) {
        final List<String> descriptions = Lists.newArrayList();

        for (final CIString description : rpslObject.getValuesForAttribute(AttributeType.DESCR)) {
            descriptions.add(description.toString());
        }

        return new Remark(descriptions);
    }

    private static boolean hasRemark(final RpslObject rpslObject) {
        return !rpslObject.getValuesForAttribute(AttributeType.DESCR).isEmpty();
    }

    private static Event createEvent(final LocalDateTime lastChanged) {
        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction(Action.LAST_CHANGED);
        lastChangedEvent.setEventDate(lastChanged);
        return lastChangedEvent;
    }

    private static List<Entity> createContactEntities(final RpslObject rpslObject) {
        final List<Entity> entities = Lists.newArrayList();
        final Map<CIString, Set<AttributeType>> contacts = Maps.newTreeMap();

        for (final AttributeType attributeType : CONTACT_ATTRIBUTE_TO_ROLE_NAME.keySet()) {
            for (final RpslAttribute attribute : rpslObject.findAttributes(attributeType)) {
                final CIString contactName = attribute.getCleanValue();
                if (contacts.containsKey(contactName)) {
                    contacts.get(contactName).add(attribute.getType());
                } else {
                    contacts.put(contactName, Sets.newHashSet(attribute.getType()));
                }
            }
        }

        for (final Map.Entry<CIString, Set<AttributeType>> entry : contacts.entrySet()) {
            final Entity entity = new Entity();
            entity.setHandle(entry.getKey().toString());
            for (final AttributeType attributeType : entry.getValue()) {
                entity.getRoles().add(CONTACT_ATTRIBUTE_TO_ROLE_NAME.get(attributeType));
            }
            entities.add(entity);
        }

        return entities;
    }

    private static Entity createEntity(final RpslObject rpslObject) {
        // top-level entity has no role
        return createEntity(rpslObject, null);
    }

    private static Entity createEntity(final RpslObject rpslObject, final Role role) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        if (role != null) {
            entity.getRoles().add(role);
        }
        entity.setVCardArray(createVCard(rpslObject));
        entity.getEntitySearchResults().addAll(createContactEntities(rpslObject));

        handleLanguageAttribute(rpslObject, entity);

        return entity;
    }

    private static Autnum createAutnumResponse(final RpslObject rpslObject) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());
        autnum.setName(rpslObject.getValueForAttribute(AttributeType.AS_NAME).toString().replace(" ", ""));
        autnum.setType(DIRECT_ALLOCATION);
        autnum.getEntitySearchResults().addAll(createContactEntities(rpslObject));
        return autnum;
    }

    private static Autnum createAsBlockResponse(final RpslObject rpslObject) {
        final Autnum autnum = new Autnum();

        final String key = rpslObject.getValueForAttribute(AttributeType.AS_BLOCK).toString();
        final AsBlockRange blockRange = getAsBlockRange(key);

        autnum.setHandle(blockRange.getBeginWithPrefix());
        //TODO :check what should be the name
        String asName = String.join("-", blockRange.getBeginWithPrefix(), blockRange.getEndWithPrefix());
        autnum.setName(asName);
        autnum.setStartAutnum(blockRange.getBegin());
        autnum.setEndAutnum(blockRange.getEnd());
        autnum.setType(DIRECT_ALLOCATION);
        autnum.getEntitySearchResults().addAll(createContactEntities(rpslObject));
        return autnum;
    }

    private static Domain createDomain(final RpslObject rpslObject) {
        final Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        final Map<CIString, Set<IpInterval>> hostnameMap = new HashMap<>();

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

        domain.getEntitySearchResults().addAll(createContactEntities(rpslObject));
        return domain;
    }

    private static VCard createVCard(final RpslObject rpslObject) {
        final VCardBuilder builder = new VCardBuilder();
        builder.addVersion();

        switch (rpslObject.getType()) {
            case PERSON:
                builder.addFn(rpslObject.getValueForAttribute(PERSON))
                        .addKind(INDIVIDUAL);
                break;
            case MNTNER:
                builder.addFn(rpslObject.getValueForAttribute(AttributeType.MNTNER))
                        .addKind(INDIVIDUAL);
                break;
            case ORGANISATION:
                builder.addFn(rpslObject.getValueForAttribute(ORG_NAME))
                        .addKind(ORGANISATION);
                break;
            case ROLE:
                builder.addFn(rpslObject.getValueForAttribute(ROLE))
                        .addKind(GROUP);
                break;
            default:
                break;
        }

        builder.addAdr(rpslObject.getValuesForAttribute(ADDRESS))
                .addTel(rpslObject.getValuesForAttribute(PHONE))
                .addFax(rpslObject.getValuesForAttribute(FAX_NO))
                .addEmail(rpslObject.getValuesForAttribute(E_MAIL))
                .addAbuseMailBox(rpslObject.getValueOrNullForAttribute(ABUSE_MAILBOX))
                .addOrg(rpslObject.getValuesForAttribute(ORG))
                .addGeo(rpslObject.getValuesForAttribute(GEOLOC));

        return builder.build();
    }

    private static AsBlockRange getAsBlockRange(String asBlock) {
        try {
            return AsBlockRange.parse(asBlock);
        } catch (AttributeParseException ex) {
            throw new InternalServerErrorException("Invalid AS Block found in database");
        }
    }

    private static void handleLanguageAttribute(final RpslObject rpslObject, final RdapObject rdapObject) {
        if (!rpslObject.containsAttribute(AttributeType.LANGUAGE)) {
            return;
        }

        List<RpslAttribute> languages = rpslObject.findAttributes(AttributeType.LANGUAGE);
        rdapObject.setLang(rpslObject.findAttributes(AttributeType.LANGUAGE).get(0).getCleanValue().toString());
        addNoticeForMultipleValues(rdapObject, AttributeType.LANGUAGE, languages, rpslObject.getKey().toString());
    }

    private static void handleCountryAttribute(final RpslObject rpslObject, final Ip ip) {
        if (!rpslObject.containsAttribute(AttributeType.COUNTRY)) {
            return;
        }

        List<RpslAttribute> countries = rpslObject.findAttributes(AttributeType.COUNTRY);
        ip.setCountry(countries.get(0).getCleanValue().toString());
        addNoticeForMultipleValues(ip, AttributeType.COUNTRY, countries, ip.getHandle());
    }

    private static void addNoticeForMultipleValues(final RdapObject rdapObject, final AttributeType type, final List<RpslAttribute> values, final String key) {
        if(values.isEmpty() || values.size() == 1) {
            return;
        }

        final String commaSeperatedValues = values.stream().map( value -> value.getCleanValue()).collect(Collectors.joining(", "));
        final String title = String.format("Multiple %s attributes found", type.getName());
        final String desc = String.format("There are multiple %s attributes %s in %s, but only the first %s %s was returned.", type.getName(), commaSeperatedValues, key, type.getName(), values.get(0).getCleanValue());

        final Notice notice = new Notice();
        notice.setTitle(title);
        notice.getDescription().add(desc);

        rdapObject.getNotices().add(notice);
    }
}
