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

import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;

class RdapObjectMapper {
    private static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/data-tools/support/documentation/terms";
    private static final Link COPYRIGHT_LINK = new Link().setRel("copyright").setValue(TERMS_AND_CONDITIONS).setHref(TERMS_AND_CONDITIONS);
    private static final List<String> RDAPCONFORMANCE = Lists.newArrayList("rdap_level_0");

    private static final Map<AttributeType, String> typeToRole;

    static {
        typeToRole = Maps.newHashMap();
        typeToRole.put(AttributeType.ADMIN_C, "administrative");
        typeToRole.put(AttributeType.TECH_C, "technical");
        typeToRole.put(AttributeType.MNT_BY, "registrant");
    }

    public static Object map(final String requestUrl, final String baseUrl, final RpslObject rpslObject, final LocalDateTime lastChangedTimestamp, final List<RpslObject> abuseContacts) {
        RdapObject rdapResponse;
        final ObjectType rpslObjectType = rpslObject.getType();

        String noticeValue = requestUrl;

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject, requestUrl, baseUrl);
                noticeValue = noticeValue + "/domain/";
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(rpslObject, requestUrl, baseUrl);
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
                rdapResponse = createEntity(rpslObject, requestUrl, baseUrl);
                break;
            default:
                throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
        }

        rdapResponse.getRdapConformance().add("rdap_level_0");
        rdapResponse.getRemarks().add(createRemark(rpslObject));
        rdapResponse.getEvents().add(createEvent(lastChangedTimestamp));

        noticeValue = noticeValue + rpslObject.getKey();
        rdapResponse.getRdapConformance().addAll(RDAPCONFORMANCE);
        rdapResponse.getNotices().addAll(NoticeFactory.generateNotices(noticeValue, rpslObject));

        rdapResponse.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(requestUrl));
        rdapResponse.getLinks().add(COPYRIGHT_LINK);

        for (final RpslObject abuseContact : abuseContacts) {
            rdapResponse.getEntities().add(createEntity(abuseContact, requestUrl, baseUrl));
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
        ip.setLang(Joiner.on(",").join(rpslObject.getValuesForAttribute(AttributeType.LANGUAGE)));
        ip.setType(rpslObject.getValueForAttribute(AttributeType.STATUS).toString());

//        ip.getLinks().add(new Link().setRel("up")... //TODO parent (first less specific) - do parentHandle at the same time

        return ip;
    }

    private static void setRemarks(final RdapObject rdapObject, final RpslObject rpslObject) {
        final Remark remark = createRemark(rpslObject);
        if (remark.getDescription().size() > 0) {
            rdapObject.getRemarks().add(remark);
        }
    }

    private static Remark createRemark(final RpslObject rpslObject) {
        final Remark remark = new Remark();
        for (final CIString descriptionValue : rpslObject.getValuesForAttribute(AttributeType.DESCR)) {
            remark.getDescription().add(descriptionValue.toString());
        }

        return remark;
    }

    private static Event createEvent(final LocalDateTime lastChanged) {
        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
        lastChangedEvent.setEventDate(lastChanged);
        return lastChangedEvent;
    }

    private static void setEntities(final RdapObject rdapObject, final RpslObject rpslObject, final Set<AttributeType> attributeTypes, final String requestUrl, final String baseUrl) {
        /* Construct a map for finding the entity objects in the query
         * results. */
        final Map<CIString, RpslObject> objectMap = Maps.newHashMap();

        /* Construct a map from attribute value to a list of the
         * attribute types against which it is recorded. (To handle
         * the case where a single person/role/similar occurs multiple
         * times in a record.) */
        final Map<CIString, Set<AttributeType>> valueToRoles = Maps.newHashMap();
        for (final RpslAttribute attribute : rpslObject.findAttributes(attributeTypes)) {
            final CIString key = attribute.getCleanValue();
            final Set<AttributeType> roleAttributeTypes = valueToRoles.get(key);

            if (roleAttributeTypes != null) {
                roleAttributeTypes.add(attribute.getType());
            } else {
                final Set<AttributeType> newAttributes = Sets.newHashSet();
                newAttributes.add(attribute.getType());
                valueToRoles.put(key, newAttributes);
            }
        }

        for (final CIString key : valueToRoles.keySet()) {
            final Set<AttributeType> attributes = valueToRoles.get(key);
            final RpslObject object = objectMap.get(key);
            if (object != null) {
                final Entity entity = createEntity(object, requestUrl, baseUrl);
                final List<String> roles = entity.getRoles();
                for (final AttributeType at : attributes) {
                    roles.add(typeToRole.get(at));
                }
                rdapObject.getEntities().add(entity);
            }
        }
    }

    private static Entity createEntity(final RpslObject rpslObject, final String requestUrl, final String baseUrl) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVCardArray(createVCard(rpslObject));
        setRemarks(entity, rpslObject);

        if (rpslObject.getType() == ObjectType.ORGANISATION) {
            final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
            contactAttributeTypes.add(AttributeType.ADMIN_C);
            contactAttributeTypes.add(AttributeType.TECH_C);
            setEntities(entity, rpslObject, contactAttributeTypes, requestUrl, baseUrl);
        }

        return entity;
    }

    private static Autnum createAutnumResponse(final RpslObject rpslObject, final String requestUrl, final String baseUrl) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());
        autnum.setName(rpslObject.getValueForAttribute(AttributeType.AS_NAME).toString().replace(" ", ""));

        /* aut-num records don't have a 'type' or 'status' field, and
         * each is allocated directly by the relevant RIR. 'DIRECT
         * ALLOCATION' is the default value used in the response
         * draft, and it makes sense to use it here too, at least for
         * now. */
        autnum.setType("DIRECT ALLOCATION");

        /* None of the statuses from [9.1] in json-response is
         * applicable here, so 'status' will be left empty for now. */

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);
        setEntities(autnum, rpslObject, contactAttributeTypes, requestUrl, baseUrl);

        return autnum;
    }

    private static Domain createDomain(final RpslObject rpslObject, final String requestUrl, final String baseUrl) {
        Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        final HashMap<CIString, Set<IpInterval>> hostnameMap = new HashMap<>();

        for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            final NServer nserver = NServer.parse(rpslAttribute.getCleanValue().toString());

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

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);
        setEntities(domain, rpslObject, contactAttributeTypes, requestUrl, baseUrl);

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
