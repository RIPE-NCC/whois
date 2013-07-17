package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.VCard;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.DsRdata;
import net.ripe.db.whois.common.domain.attrs.NServer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.*;
import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;

class RdapObjectMapper {
    private static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/data-tools/support/documentation/terms";
    private static final Link COPYRIGHT_LINK = new Link().setRel("copyright").setValue(TERMS_AND_CONDITIONS).setHref(TERMS_AND_CONDITIONS);
    private static final String PORT43 = "whois.ripe.net";

    private static final List<String> RDAP_CONFORMANCE_LEVEL = Lists.newArrayList("rdap_level_0");

    private static final Map<AttributeType, String> CONTACT_ATTRIBUTE_TO_ROLE_NAME = Maps.newHashMap();

    static {
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(ADMIN_C, "administrative");
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(TECH_C, "technical");
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(MNT_BY, "registrant");
        CONTACT_ATTRIBUTE_TO_ROLE_NAME.put(ZONE_C, "zone");
    }

    private final NoticeFactory noticeFactory;

    public RdapObjectMapper(final NoticeFactory noticeFactory) {
        this.noticeFactory = noticeFactory;
    }

    public Object map(final String requestUrl, final RpslObject rpslObject, final LocalDateTime lastChangedTimestamp, final List<RpslObject> abuseContacts) {
        RdapObject rdapResponse;
        final ObjectType rpslObjectType = rpslObject.getType();

        String noticeValue = requestUrl;

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject);
                noticeValue = noticeValue + "/domain/";
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(rpslObject);
                noticeValue = noticeValue + "/autnum/";
                break;
            case INETNUM:
            case INET6NUM:
                rdapResponse = createIp(rpslObject);
                noticeValue = noticeValue + "/ip/";
                break;
            case PERSON:
            case ROLE:
            case ORGANISATION:
                rdapResponse = createEntity(rpslObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
        }

        rdapResponse.getRemarks().add(createRemark(rpslObject));
        rdapResponse.getEvents().add(createEvent(lastChangedTimestamp));

        rdapResponse.getRdapConformance().addAll(RDAP_CONFORMANCE_LEVEL);
        rdapResponse.getNotices().addAll(noticeFactory.generateNotices(noticeValue + rpslObject.getKey(), rpslObject));

        rdapResponse.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(requestUrl));
        rdapResponse.getLinks().add(COPYRIGHT_LINK);

        for (final RpslObject abuseContact : abuseContacts) {
            rdapResponse.getEntities().add(createEntity(abuseContact));
        }

        return rdapResponse;
    }

    private static Ip createIp(final RpslObject rpslObject) {
        final Ip ip = new Ip();
        ip.setHandle(rpslObject.getKey().toString());
        IpInterval ipInterval;
        if (rpslObject.getType() == INET6NUM) {
            ipInterval = Ipv6Resource.parse(rpslObject.getKey());
            ip.setIpVersion("v6");
        } else {
            ipInterval = Ipv4Resource.parse(rpslObject.getKey());
            ip.setIpVersion("v4");
        }
        ip.setStartAddress(IpInterval.asIpInterval(ipInterval.beginAsInetAddress()).toString());
        ip.setEndAddress(IpInterval.asIpInterval(ipInterval.endAsInetAddress()).toString());

        ip.setName(rpslObject.getValueForAttribute(AttributeType.NETNAME).toString());
        ip.setCountry(rpslObject.getValueForAttribute(AttributeType.COUNTRY).toString());
        ip.setLang(rpslObject.getValuesForAttribute(AttributeType.LANGUAGE).isEmpty() ? null : Joiner.on(",").join(rpslObject.getValuesForAttribute(AttributeType.LANGUAGE)));
        ip.setType(rpslObject.getValueForAttribute(AttributeType.STATUS).toString());

//        ip.getLinks().add(new Link().setRel("up")... //TODO parent (first less specific) - do parentHandle at the same time

        return ip;
    }

    private static Remark createRemark(final RpslObject rpslObject) {
        final List<String> descriptions = Lists.newArrayList();

        for (final CIString description : rpslObject.getValuesForAttribute(AttributeType.DESCR)) {
            descriptions.add(description.toString());
        }

        return new Remark(descriptions);
    }

    private static Event createEvent(final LocalDateTime lastChanged) {
        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
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
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVCardArray(createVCard(rpslObject));
        entity.getEntities().addAll(createContactEntities(rpslObject));
        entity.setPort43(PORT43);
        return entity;
    }

    private static Autnum createAutnumResponse(final RpslObject rpslObject) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());
        autnum.setName(rpslObject.getValueForAttribute(AttributeType.AS_NAME).toString().replace(" ", ""));
        autnum.setType("DIRECT ALLOCATION");
        autnum.getEntities().addAll(createContactEntities(rpslObject));
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

        for (final CIString rdata : rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)) {
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

        domain.getEntities().addAll(createContactEntities(rpslObject));
        domain.setPort43(PORT43);
        return domain;
    }

    private static VCard createVCard(final RpslObject rpslObject) {
        final VCardBuilder builder = new VCardBuilder();
        builder.addVersion();

        switch (rpslObject.getType()) {
            case PERSON:
                for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PERSON)) {
                    builder.addFn(attribute.getCleanValue().toString());
                }
                builder.addKind("individual");
                break;
            case ORGANISATION:
                for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ORG_NAME)) {
                    builder.addFn(attribute.getCleanValue().toString());
                }
                builder.addKind("org");
                break;
            case ROLE:
            case IRT:
                builder.addKind("group");
                break;
            default:
                break;
        }

        for (final CIString address : rpslObject.getValuesForAttribute(AttributeType.ADDRESS)) {
            builder.addAdr(VCardHelper.createMap(Maps.immutableEntry("label", address)), null);
        }

        for (final CIString phone : rpslObject.getValuesForAttribute(AttributeType.PHONE)) {
            builder.addTel(phone.toString());
        }

        for (final CIString email : rpslObject.getValuesForAttribute(AttributeType.E_MAIL)) {
            // TODO ?? Is it valid to have more than 1 email
            builder.addEmail(email.toString());
        }

        for (final CIString org : rpslObject.getValuesForAttribute(AttributeType.ORG)) {
            builder.addOrg(org.toString());
        }

        return builder.build();
    }
}
