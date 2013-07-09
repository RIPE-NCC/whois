package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.Changed;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.ObjectType.INET6NUM;


class RdapObjectMapper {
    private static String RDAP_CONFORMANCE_LEVEL = "rdap_level_0";

    public static Object map(final String requestUrl, final RpslObject rpslObject) {
        RdapObject rdapResponse = null;
        final ObjectType rpslObjectType = rpslObject.getType();

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomain(rpslObject);
                break;
            case AUT_NUM:
                rdapResponse = createAutnumResponse(requestUrl, rpslObject);
                break;
            case INETNUM:
            case INET6NUM:
                rdapResponse = createIp(rpslObject);
                break;
            case PERSON:
            case ORGANISATION:
            case ROLE:
            case IRT:
                rdapResponse = createEntity(requestUrl, rpslObject);
                break;
            default:
                throw new IllegalArgumentException("Unhandled object type: " + rpslObject.getType());
        }

        if (rdapResponse != null) {
            rdapResponse.getRdapConformance().add(RDAP_CONFORMANCE_LEVEL);
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

        //TODO [AS] is parentHandle optional or not?
        return ip;
    }

    private static Remark createRemark(final RpslObject rpslObject) {
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

    private static Event createEvent(final RpslObject rpslObject) {
        final Changed latestChanged = findLatestChangedAttribute(rpslObject);

        final Event event = new Event();
        event.setEventAction("last changed");
        event.setEventActor(latestChanged.getEmail());

        final LocalDate eventDate = latestChanged.getDate();
        if (eventDate != null) {
            event.setEventDate(eventDate.toLocalDateTime(new LocalTime(0, 0, 0)));
        }

        return event;
    }

    private static Changed findLatestChangedAttribute(final RpslObject rpslObject) {
        Changed result = null;
        for (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.CHANGED)) {
            final Changed changed = Changed.parse(rpslAttribute.getCleanValue());
            if ((result == null) || (changed.getDate().isAfter(result.getDate()))) {
                result = changed;
            }
        }
        return result;
    }

    private static Entity createEntity(final String requestUrl, final RpslObject rpslObject) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVcardArray(createVcards(rpslObject));
        entity.getRemarks().add(createRemark(rpslObject));
        entity.getEvents().add(createEvent(rpslObject));

        final Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setValue(requestUrl);
        selfLink.setHref(requestUrl);
        entity.getLinks().add(selfLink);

        return entity;
    }

    private static Autnum createAutnumResponse(final String requestUrl, final RpslObject rpslObject) {
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

        autnum.getRemarks().add(createRemark(rpslObject));
        autnum.getEvents().add(createEvent(rpslObject));

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);

        autnum.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(requestUrl));
        return autnum;
    }

    private static Domain createDomain(RpslObject rpslObject) {
        Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            final Nameserver ns = new Nameserver();
            ns.setLdhName(rpslAttribute.getCleanValue().toString());
            domain.getNameservers().add(ns);
        }

        domain.getRemarks().add(createRemark(rpslObject));
        return domain;
    }

    private static List<Object> createVcards(RpslObject rpslObject) {
        VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();
        builder.setVersion();

        for (final RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PERSON)) {
            builder.setFn(attribute.getCleanValue().toString());
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
}
