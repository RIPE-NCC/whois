package net.ripe.db.whois.api.whois.rdap;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.ripe.db.whois.common.rpsl.AttributeType.AS_NAME;
import static net.ripe.db.whois.common.rpsl.AttributeType.AUT_NUM;

class RdapObjectMapper {
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

        final Link selfLink = new Link();
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
        for (final RpslObject object : queue) {
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
                final Entity entity = createEntity(object);
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

        if (!rpslObject.getType().getName().equals(ObjectType.AUT_NUM.getName())) {
            throw new IllegalArgumentException("as-blocks are not allowed for rdap queries");
        }

        final CIString autnumAttributeValue = rpslObject.getValueForAttribute(AUT_NUM);
        final long startAndEnd = Long.valueOf(autnumAttributeValue.toString().replace("AS", "").replace(" ", ""));
        autnum.setStartAutnum(startAndEnd);
        autnum.setEndAutnum(startAndEnd);

        autnum.setCountry(rpslObject.findAttribute(AttributeType.COUNTRY).getValue().replace(" ", ""));
        autnum.setName(rpslObject.getValueForAttribute(AS_NAME).toString().replace(" ", ""));

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

        final Set<AttributeType> contactAttributeTypes = Sets.newHashSet();
        contactAttributeTypes.add(AttributeType.ADMIN_C);
        contactAttributeTypes.add(AttributeType.TECH_C);
        setEntities(autnum, rpslObject, queue, contactAttributeTypes);

        autnum.getLinks().add(new Link().setRel("self").setValue(requestUrl).setHref(requestUrl));
        return autnum;
    }

    private Domain createDomainResponse(final RpslObject rpslObject) {
        final Domain domain = new Domain();
        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        for (final RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            final Nameserver ns = new Nameserver();
            ns.setLdhName(rpslAttribute.getCleanValue().toString());
            domain.getNameservers().add(ns);
        }

        setRemarks(domain, rpslObject);
        return domain;
    }

    private List<Object> generateVcards(final RpslObject rpslObject) {
        final VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();
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

    private DatatypeFactory createDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (final DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
