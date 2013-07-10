package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.api.whois.rdap.domain.RdapObject;
import net.ripe.db.whois.api.whois.rdap.domain.Remark;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.Changed;
import net.ripe.db.whois.common.domain.attrs.DsRdata;
import net.ripe.db.whois.common.domain.attrs.NServer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;

class RdapObjectMapper {
    private static List<String> RDAPCONFORMANCE = Lists.newArrayList("rdap_level_0");

    private final Queue<RpslObject> rpslObjectQueue;
    private final String baseUrl;
    private final String requestUrl;

    private final DatatypeFactory dataTypeFactory = createDatatypeFactory();

    private RdapObject rdapResponse;

    /* Map from attribute type to role name, for entities. */
    private static final Map<AttributeType, String> typeToRole;

    static {
        typeToRole = Maps.newHashMap();
        typeToRole.put(AttributeType.ADMIN_C, "administrative");
        typeToRole.put(AttributeType.TECH_C, "technical");
        typeToRole.put(AttributeType.MNT_BY, "registrant");
    }

    public RdapObjectMapper(final String baseUrl, final String requestUrl, final Queue<RpslObject> rpslObjectQueue) {
        this.rpslObjectQueue = rpslObjectQueue;
        this.baseUrl = baseUrl;
        this.requestUrl = requestUrl;
    }

    public Object build() {
        if (rpslObjectQueue == null) {
            return rdapResponse;
        }

        if (rpslObjectQueue.isEmpty()) {
            throw new IllegalStateException("The RPSL queue is empty.");
        }

        add(rpslObjectQueue.poll(), rpslObjectQueue);

        return rdapResponse;
    }

    private void add(final RpslObject rpslObject, final Queue<RpslObject> rpslObjectQueue) {
        final ObjectType rpslObjectType = rpslObject.getType();

        String noticeValue = baseUrl;

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject, rpslObjectQueue);
                noticeValue = noticeValue + "/domain/";
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(rpslObject, rpslObjectQueue);
                noticeValue = noticeValue + "/autnum/";
                break;
            case INETNUM:
            case INET6NUM:
                rdapResponse = createIp(rpslObject);
                noticeValue = noticeValue + "/ip/";
                break;
            case PERSON:
            case ORGANISATION:
            case ROLE:
            case IRT:
                rdapResponse = createEntity(rpslObject, rpslObjectQueue);
                noticeValue = noticeValue + "/entity/";
                break;
        }

        if (rdapResponse != null) {
            noticeValue = noticeValue + rpslObject.getKey();
            rdapResponse.getRdapConformance().addAll(RDAPCONFORMANCE);
            //rdapResponse.getNotices().add(createTnC(noticeValue));
            rdapResponse.getNotices().addAll(NoticeFactory.generateNotices(noticeValue));
        }
    }

    private Ip createIp(final RpslObject rpslObject) {
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

        //TODO [AS] is parentHandle optional or not?
        return ip;
    }

    private Remark createRemark(final RpslObject rpslObject) {
        final List<String> descriptions = Lists.newArrayList();

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.REMARKS)) {
            descriptions.add(attribute.getCleanValue().toString());
        }

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.DESCR)) {
            descriptions.add(attribute.getCleanValue().toString());
        }

        final Remark remark = new Remark();
        remark.getDescription().addAll(descriptions);
        return remark;
    }

    private void setRemarks(final RdapObject rdapObject, final RpslObject rpslObject) {
        final Remark remark = createRemark(rpslObject);
        if (remark.getDescription().size() > 0) {
            rdapObject.getRemarks().add(remark);
        }
    }

    private Event createEvent(final RpslObject rpslObject) {
        final Changed latestChanged = findLatestChangedAttribute(rpslObject);

        final Event event = new Event();
        event.setEventAction("last changed");
        event.setEventActor(latestChanged.getEmail());

        if (latestChanged.getDate() != null) {
            final GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(latestChanged.getDate().toDate());
            event.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar));
        }

        return event;
    }

    private Changed findLatestChangedAttribute(final RpslObject rpslObject) {
        Changed result = null;
        for (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.CHANGED)) {
            final Changed changed = Changed.parse(rpslAttribute.getCleanValue());
            if ((result == null) || (changed.getDate().isAfter(result.getDate()))) {
                result = changed;
            }
        }
        return result;
    }

    private Entity createEntity(final RpslObject rpslObject, final Queue<RpslObject> queue) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVcardArray(createVcards(rpslObject));
        setRemarks(entity, rpslObject);
        entity.getEvents().add(createEvent(rpslObject));

        final Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setValue(requestUrl);
        selfLink.setHref(baseUrl + "/entity/" + entity.getHandle());
        entity.getLinks().add(selfLink);

        if (rpslObject.getType() == ObjectType.ORGANISATION) {
            final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
            contactAttributeTypes.add(AttributeType.ADMIN_C);
            contactAttributeTypes.add(AttributeType.TECH_C);
            setEntities(entity, rpslObject, queue, contactAttributeTypes);
        }

        return entity;
    }

    private void setEntities(final RdapObject rdapObject, final RpslObject rpslObject, final Queue<RpslObject> rpslObjectQueue, final Set<AttributeType> attributeTypes) {
        /* Construct a map for finding the entity objects in the query
         * results. */
        final Map<CIString, RpslObject> objectMap = Maps.newHashMap();
        for (final RpslObject object : rpslObjectQueue) {
            objectMap.put(object.getKey(), object);
        }

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
                final Entity entity = createEntity(object, rpslObjectQueue);
                final List<String> roles = entity.getRoles();
                for (final AttributeType at : attributes) {
                    roles.add(typeToRole.get(at));
                }
                rdapObject.getEntities().add(entity);
            }
        }
    }

    private Autnum createAutnumResponse(final RpslObject rpslObject, final Queue<RpslObject> queue) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());

        final CIString autnumAttributeValue = rpslObject.getValueForAttribute(AttributeType.AUT_NUM);
        final long startAndEnd = Long.valueOf(autnumAttributeValue.toString().replace("AS", "").replace(" ", ""));
        autnum.setStartAutnum(startAndEnd);
        autnum.setEndAutnum(startAndEnd);

        autnum.setCountry(rpslObject.findAttribute(AttributeType.COUNTRY).getValue().replace(" ", ""));
        autnum.setName(rpslObject.getValueForAttribute(AttributeType.AS_NAME).toString().replace(" ", ""));

        /* aut-num records don't have a 'type' or 'status' field, and
         * each is allocated directly by the relevant RIR. 'DIRECT
         * ALLOCATION' is the default value used in the response
         * draft, and it makes sense to use it here too, at least for
         * now. */
        autnum.setType("DIRECT ALLOCATION");

        /* None of the statuses from [9.1] in json-response is
         * applicable here, so 'status' will be left empty for now. */

        setRemarks(autnum, rpslObject);
        autnum.getEvents().add(createEvent(rpslObject));

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);
        setEntities(autnum, rpslObject, queue, contactAttributeTypes);

        autnum.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(baseUrl + "/autnum/" + new Long(startAndEnd).toString()));

        return autnum;
    }

    private Domain  createDomain(RpslObject rpslObject, Queue<RpslObject> queue) {
        Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        final HashMap<CIString,Set<IpInterval>> hostnameMap = new HashMap<>();

        for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            final NServer nserver = NServer.parse(rpslAttribute.getCleanValue().toString());

            final CIString hostname = nserver.getHostname();

            final Set<IpInterval> ipIntervalSet;
            if (hostnameMap.containsKey(hostname)) {
                ipIntervalSet = hostnameMap.get(hostname);
            }
            else {
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

            domain.getNameServers().add(nameserver);
        }

        final Domain.SecureDNS secureDNS = new Domain.SecureDNS();

        for (final CIString rdata : rpslObject.getValuesForAttribute(AttributeType.DS_RDATA)) {
            final DsRdata dsRdata = DsRdata.parse(rdata);

            secureDNS.setDelegationSigned(true);

            final Domain.SecureDNS.DsData dsData = new Domain.SecureDNS.DsData();
            dsData.setKeyTag(dsRdata.getKeyTag());
            dsData.setAlgorithm(dsRdata.getAlgorithmNumber());
            dsData.setDigestType(dsRdata.getDigestType());
            dsData.setDigest(dsRdata.getDigest().toString());

            secureDNS.getDsData().add(dsData);
        }

        if (secureDNS.isDelegationSigned()) {
            domain.setSecureDNS(secureDNS);
        }

        setRemarks(domain, rpslObject);
        domain.getEvents().add(createEvent(rpslObject));

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);
        setEntities(domain, rpslObject, queue, contactAttributeTypes);

        domain.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(baseUrl + "/domain/" + domain.getHandle()));


        return domain;
    }

    private List<Object> createVcards(RpslObject rpslObject) {
        VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();
        builder.setVersion();

        final ObjectType rpslObjectType = rpslObject.getType();

        if (rpslObjectType == ObjectType.PERSON)  {
            for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PERSON)) {
                builder.setFn(attribute.getCleanValue().toString());
            }
        } else if (rpslObjectType == ObjectType.ORGANISATION) {
            for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ORG_NAME)) {
                builder.setFn(attribute.getCleanValue().toString());
            }
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ADDRESS)) {
            builder.addAdr(VcardObjectHelper.createMap(Maps.immutableEntry("label", attribute.getCleanValue())), null);
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PHONE)) {
            builder.addTel(attribute.getCleanValue().toString());
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.E_MAIL)) {
            // TODO ?? Is it valid to have more than 1 email
            builder.setEmail(attribute.getCleanValue().toString());
        }

        if (builder.isEmpty()) {
            return null;
        }

        return builder.build();
    }

    private DatatypeFactory createDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (final DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
