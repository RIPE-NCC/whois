package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RdapObjectMapper {
    private static List<String> RDAPCONFORMANCE = Lists.newArrayList("rdap_level_0");
    private static final Splitter SPLITTER = Splitter.on(' ');

    private final Queue<RpslObject> objectQueue;
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

    public RdapObjectMapper(final String requestUrl, final Queue<RpslObject> objectQueue) {
        this.objectQueue = objectQueue;
        this.requestUrl = requestUrl;
    }

    public Object build() {
        if (objectQueue == null) {
            return rdapResponse;
        }

        if (objectQueue.isEmpty()) {
            throw new IllegalStateException("The RPSL queue is empty.");
        }

        add(objectQueue.poll(), objectQueue);

        return rdapResponse;
    }

    private void add(final RpslObject rpslObject, final Queue<RpslObject> rpslObjectQueue) {
        final ObjectType rpslObjectType = rpslObject.getType();

        switch (rpslObjectType) {
            case DOMAIN:
                rdapResponse = createDomainResponse(rpslObject);
                break;
            case AUT_NUM:
            case AS_BLOCK:
                rdapResponse = createAutnumResponse(rpslObject, rpslObjectQueue);
                break;
            case INETNUM:
            case INET6NUM:
                Ip ip = new Ip();
                ip.setHandle(rpslObject.getKey().toString());
                rdapResponse = ip;
                break;
            case PERSON:
            case ORGANISATION:
            case ROLE:
            case IRT:
                rdapResponse = createEntity(rpslObject);
                break;
        }

        if (rdapResponse != null) {
            rdapResponse.getRdapConformance().addAll(RDAPCONFORMANCE);
        }
    }

    private void setRemarks(final RdapObject rdapObject, final RpslObject rpslObject) {
        final List<RpslAttribute> allRemarks = Lists.newArrayList();
        allRemarks.addAll(rpslObject.findAttributes(AttributeType.REMARKS));
        allRemarks.addAll(rpslObject.findAttributes(AttributeType.DESCR));

        final List<String> remarkList = Lists.newArrayList();
        for (final RpslAttribute rpslAttribute : allRemarks) {
            remarkList.add(rpslAttribute.getCleanValue().toString());
        }

        final Remark remark = new Remark();
        remark.getDescription().addAll(remarkList);
        rdapObject.getRemarks().add(remark);
    }

    private void setEvents(final RdapObject rdapObject, final RpslObject rpslObject) {
        final List<RpslAttribute> changedAttributes = rpslObject.findAttributes(AttributeType.CHANGED);
        final String eventString = changedAttributes.get(changedAttributes.size() - 1).getValue();

        final Iterable<String> eventStringParts = SPLITTER.split(eventString.trim());

        final Event event = new Event();
        event.setEventAction("last changed");
        event.setEventActor(Iterables.get(eventStringParts, 0));

        try {
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new SimpleDateFormat("yyyyMMdd").parse(Iterables.get(eventStringParts, 1)));
            event.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gc));
        } catch (final ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid event date: %s", Iterables.get(eventStringParts, 1)));
        }

        rdapObject.getEvents().add(event);
    }

    private Entity createEntity(final RpslObject rpslObject) {
        final Entity entity = new Entity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVcardArray(generateVcards(rpslObject));
        setRemarks(entity, rpslObject);
        setEvents(entity, rpslObject);

        Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setValue(requestUrl);
        selfLink.setHref(requestUrl);
        entity.getLinks().add(selfLink);

        return entity;
    }

    private void setEntities(final RdapObject rdapObject, final RpslObject rpslObject, final Queue<RpslObject> queue, final Set<AttributeType> attributeTypes) {
        /* Construct a map for finding the entity objects in the query
         * results. */
        final Map<CIString, RpslObject> objectMap = Maps.newHashMap();
        for (final RpslObject tro : queue) {
            objectMap.put(tro.getKey(), tro);
        }
        /* For each entity attribute type, load the attributes from
         * the object. */
        final List<RpslAttribute> allAttributes = Lists.newArrayList();
        for (final AttributeType attribute : attributeTypes) {
            allAttributes.addAll(rpslObject.findAttributes(attribute));
        }
        /* Construct a map from attribute value to a list of the
         * attribute types against which it is recorded. (To handle
         * the case where a single person/role/similar occurs multiple
         * times in a record.) */
        final Map<CIString, Set<AttributeType>> valueToRoles = Maps.newHashMap();
        for (final RpslAttribute attribute : allAttributes) {
            final CIString key = attribute.getCleanValue();
            final Set<AttributeType> attributeType = valueToRoles.get(key);

            if (attributeType != null) {
                attributeType.add(attribute.getType());
            } else {
                final Set<AttributeType> newAttributes = Sets.newHashSet();
                newAttributes.add(attribute.getType());
                valueToRoles.put(key, newAttributes);
            }
        }

        for (final CIString key : valueToRoles.keySet()) {
            final Set<AttributeType> attributes = valueToRoles.get(key);
            RpslObject ro = objectMap.get(key);
            if (ro != null) {
                Entity e = createEntity(ro);
                List<String> roles = e.getRoles();
                for (AttributeType at : attributes) {
                    String role = typeToRole.get(at);
                    roles.add(role);
                }
                rdapObject.getEntities().add(e);
            }
        }
    }

    private Autnum createAutnumResponse(final RpslObject rpslObject, final Queue<RpslObject> qtro) {
        Autnum autnum = new Autnum();
        autnum.setHandle(rpslObject.getKey().toString());

        boolean isAutnum = rpslObject.getType().getName().equals(ObjectType.AUT_NUM.getName());

        BigInteger start;
        BigInteger end;
        if (isAutnum) {
            RpslAttribute asn = rpslObject.findAttribute(AttributeType.AUT_NUM);
            String asn_str = asn.getValue().replace("AS", "").replace(" ", "");
            start = end = new BigInteger(asn_str);
        } else {
            RpslAttribute asn_range = rpslObject.findAttribute(AttributeType.AS_BLOCK);
            AsBlockRange abr = AsBlockRange.parse(asn_range.getValue().replace(" ", ""));
            start = BigInteger.valueOf(abr.getBegin());
            end = BigInteger.valueOf(abr.getEnd());
        }

        autnum.setStartAutnum(start);
        autnum.setEndAutnum(end);

        autnum.setCountry(rpslObject.findAttribute(AttributeType.COUNTRY).getValue().replace(" ", ""));

        /* For as-blocks, use the range as the name, since they do not
         * have an obvious 'name' attribute. */
        AttributeType name = (isAutnum) ? AttributeType.AS_NAME : AttributeType.AS_BLOCK;
        autnum.setName(rpslObject.findAttribute(name).getValue().replace(" ", ""));
        /* aut-num records don't have a 'type' or 'status' field, and
         * each is allocated directly by the relevant RIR. 'DIRECT
         * ALLOCATION' is the default value used in the response
         * draft, and it makes sense to use it here too, at least for
         * now. */
        autnum.setType("DIRECT ALLOCATION");
        /* None of the statuses from [9.1] in json-response is
         * applicable here, so 'status' will be left empty for now. */

        setRemarks(autnum, rpslObject);
        setEvents(autnum, rpslObject);

        final Set<AttributeType> eats = Sets.newHashSet();
        eats.add(AttributeType.ADMIN_C);
        eats.add(AttributeType.TECH_C);
        setEntities(autnum, rpslObject, qtro, eats);

        /* Do not add a link to the parent as-block: if each ASN
         * within the range has a corresponding aut-num record, then
         * there's no way to disambiguate. It may be an idea to add a
         * link to an external service at some later point, though. */

        /* Add a 'self' link. Do not use the beginning ASN to
         * construct the link, because if this is an as-block, there
         * may be an aut-num record for that ASN, and the link will
         * not work as expected. */
        Link sf = new Link();
        sf.setRel("self");
        sf.setValue(requestUrl);
        sf.setHref(requestUrl);
        autnum.getLinks().add(sf);

        return autnum;
    }

    private Domain createDomainResponse(RpslObject rpslObject) {
        Domain domain = new Domain();

        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        for (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            Nameserver ns = new Nameserver();
            ns.setLdhName(rpslAttribute.getCleanValue().toString());
            domain.getNameservers().add(ns);
        }

        setRemarks(domain, rpslObject);

        return domain;
    }

    private List<Object> generateVcards(RpslObject rpslObject) {
        VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();
        builder.setVersion();

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PERSON)) {
            builder.setFn(attribute.getCleanValue().toString());
        }

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.ADDRESS)) {
            builder.addAdr(VcardObjectHelper.createHashMap(Maps.immutableEntry("label", attribute.getCleanValue())), null);
        }

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.PHONE)) {
            builder.addTel(attribute.getCleanValue().toString());
        }

        for (RpslAttribute attribute : rpslObject.findAttributes(AttributeType.E_MAIL)) {
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
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
