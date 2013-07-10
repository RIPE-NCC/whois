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
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.*;


class RdapObjectMapper {
    private static final String TERMS_AND_CONDITIONS = "http://www.ripe.net/data-tools/support/documentation/terms";
    private static Link COPYRIGHT_LINK = new Link().setRel("copyright").setValue(TERMS_AND_CONDITIONS).setHref(TERMS_AND_CONDITIONS);

    private static final Map<AttributeType, String> typeToRole;
    static {
        typeToRole = new HashMap<AttributeType, String>();
        typeToRole.put(AttributeType.ADMIN_C, "administrative");
        typeToRole.put(AttributeType.TECH_C,  "technical");
        typeToRole.put(AttributeType.MNT_BY,  "registrant");
    }


    public static Object map(final String requestUrl, final RpslObject rpslObject, final VersionLookupResult versionLookupResult) {
        RdapObject rdapResponse = null;
        final ObjectType rpslObjectType = rpslObject.getType();
        final List<VersionInfo> versions = (versionLookupResult == null || rpslObjectType == PERSON || rpslObjectType == ROLE) ? Collections.<VersionInfo>emptyList() : versionLookupResult.getVersionInfos();

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject);
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(rpslObject);
                break;
            case INETNUM:
            case INET6NUM:
                rdapResponse = createIp(rpslObject);
                break;
            case PERSON:
            case ORGANISATION:
            case ROLE:
            case IRT:
                rdapResponse = createEntity(rpslObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
        }

        rdapResponse.getRdapConformance().add("rdap_level_0");
        rdapResponse.getRemarks().add(createRemark(rpslObject));
        rdapResponse.getEvents().addAll(createEvents(versions));

        rdapResponse.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(requestUrl));
        rdapResponse.getLinks().add(COPYRIGHT_LINK);

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

    private static Remark createRemark(final RpslObject rpslObject) {
        final Remark remark = new Remark();
        for (final CIString descriptionValue : rpslObject.getValuesForAttribute(AttributeType.DESCR)) {
            remark.getDescription().add(descriptionValue.toString());
        }

        return remark;
    }

    private static List<Event> createEvents(final List<VersionInfo> versions) {
        final List<Event> events = Lists.newArrayList();
        if (!versions.isEmpty()) {
            final VersionInfo first = versions.get(0);
            final Event registrationEvent = new Event();
            registrationEvent.setEventAction("registration");
            registrationEvent.setEventDate(first.getTimestamp().toLocalDateTime());
            events.add(registrationEvent);

            final VersionInfo last = versions.get(versions.size() - 1);
            final Event lastChangedEvent = new Event();
            lastChangedEvent.setEventAction("last changed");
            lastChangedEvent.setEventDate(last.getTimestamp().toLocalDateTime());
            events.add(lastChangedEvent);
        }
        return events;
    }

    private static void setEntities(final RdapObject rdapObject, final RpslObject rpslObject, final Set<AttributeType> attributeTypes) {
        /* Construct a map for finding the entity objects in the query
         * results. */
        final Map<CIString, RpslObject> objectMap = Maps.newHashMap();

//        for (final RpslObject object : rpslObjectQueue) {                         // TODO
//            objectMap.put(object.getKey(), object);
//        }

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
                final Entity entity = createEntity(object);
                final List<String> roles = entity.getRoles();
                for (final AttributeType at : attributes) {
                    roles.add(typeToRole.get(at));
                }
                rdapObject.getEntities().add(entity);
            }
        }
    }

    private static Entity createEntity(final RpslObject rpslObject) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVcardArray(createVcards(rpslObject));
        return entity;
    }

    private static Autnum createAutnumResponse(final RpslObject rpslObject) {
        final Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());

        final CIString autnumAttributeValue = rpslObject.getValueForAttribute(AttributeType.AUT_NUM);
        final long startAndEnd = Long.valueOf(autnumAttributeValue.toString().replace("AS", "").replace(" ", ""));
        autnum.setStartAutnum(startAndEnd);
        autnum.setEndAutnum(startAndEnd);

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
        setEntities(autnum, rpslObject, contactAttributeTypes);

        return autnum;
    }

    private static Domain createDomain(final RpslObject rpslObject) {
        final Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            final Nameserver ns = new Nameserver();
            ns.setLdhName(rpslAttribute.getCleanValue().toString());
            domain.getNameservers().add(ns);
        }

        return domain;
    }

    private static List<Object> createVcards(final RpslObject rpslObject) {
        VCardBuilder builder = new VCardBuilder();
        builder.addVersion();

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PERSON)) {
            builder.addFn(attribute.getCleanValue().toString());
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ADDRESS)) {
            builder.addAdr(VcardObjectHelper.createHashMap(Maps.immutableEntry("label", attribute.getCleanValue())), null);
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PHONE)) {
            builder.addTel(attribute.getCleanValue().toString());
        }

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.E_MAIL)) {
            // TODO ?? Is it valid to have more than 1 email
            builder.addEmail(attribute.getCleanValue().toString());
        }

        return builder.build();
    }
}
