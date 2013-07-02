package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Maps;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Events;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.Nameservers;
import net.ripe.db.whois.api.whois.rdap.domain.ObjectFactory;
import net.ripe.db.whois.api.whois.rdap.domain.Person;
import net.ripe.db.whois.api.whois.rdap.domain.Remarks;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class RdapObjectMapper {
    private static final Logger LOGGER = 
        LoggerFactory.getLogger(RdapObjectMapper.class);

    ObjectFactory rdapObjectFactory = new ObjectFactory();

    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private Object rdapResponse = new Object();

    public RdapObjectMapper(Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        this.taggedRpslObjectQueue = taggedRpslObjectQueue;
    }

    public Object build() throws Exception {
        if (taggedRpslObjectQueue == null) {
            return rdapResponse;
        }

        if (taggedRpslObjectQueue.isEmpty()) {
            throw new Exception("The RPSL queue is empty.");
        }

        TaggedRpslObject first = taggedRpslObjectQueue.poll();
        add(first, taggedRpslObjectQueue);

        return rdapResponse;
    }

    private void add(TaggedRpslObject taggedRpslObject,
                     Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        RpslObject rpslObject = taggedRpslObject.rpslObject;
        ObjectType rpslObjectType = rpslObject.getType();

        debug(rpslObject);

        switch (rpslObjectType) {
            case PERSON:
                rdapResponse = createPersonResponse(rpslObject);
                break;
            case DOMAIN:
                rdapResponse = createDomainResponse(rpslObject);
                break;
            case AUT_NUM:
            case AS_BLOCK:
                rdapResponse = createAutnumResponse(rpslObject,
                                                    taggedRpslObjectQueue);
                break;
            case INETNUM:
            case INET6NUM:
                Ip ip = new ObjectFactory().createIp();
                ip.setHandle(rpslObject.getKey().toString());
                rdapResponse = ip;
                break;
            case ORGANISATION:
            case ROLE:
            case IRT:
                break;
        };
    }

    private Person createPersonResponse(RpslObject rpslObject) {
        Person person = RdapHelper.createPerson();
        person.setHandle(rpslObject.getKey().toString());
        person.setVcardArray(generateVcards(rpslObject));

        // Remarks
        for (RpslAttribute rpslAttribute :
                rpslObject.findAttributes(AttributeType.REMARKS)) {
            Remarks remark = rdapObjectFactory.createRemarks();
            String descr = rpslAttribute.getCleanValue().toString();
            remark.getDescription().add(descr);
            person.getRemarks().add(remark);
        }

        // Events
        int counter = 0;
        List<RpslAttribute> changedList = rpslObject.findAttributes(AttributeType.CHANGED);
        int listSize = changedList.size();

        for (RpslAttribute rpslAttribute : changedList) {
            Events event = rdapObjectFactory.createEvents();
            String eventString = rpslAttribute.getValue();

            // Split the string and make the event entry
            eventString = eventString.trim();
            String[] eventStringElements = eventString.split(" ");

            String eventAction = "changed";
            if (counter == 0) {
                eventAction = "registration";
            } else if (counter == (listSize - 1) ) {
                eventAction = "last changed";
            }

            event.setEventAction(eventAction);
            event.setEventActor(eventStringElements[0]);

            int year = Integer.parseInt(eventStringElements[1].substring(0,4));
            int month = Integer.parseInt(eventStringElements[1].substring(5,6)) - 1;
            int day = Integer.parseInt(eventStringElements[1].substring(7,8));

            GregorianCalendar gc = new GregorianCalendar(year,month,day);

            XMLGregorianCalendar eventDate= new XMLGregorianCalendarImpl(gc);

            // and hack it coz the XMLGregorianCalendarImpl does weird stuff to it
            eventDate.setTimezone(0);
            event.setEventDate(eventDate);

            person.getEvents().add(event);

            counter++;
        }

        return person;
    }

    private Entity createEntity(RpslObject rpslObject) {
        /* todo: this is nearly the same as createPersonResponse. */
        Entity entity = rdapObjectFactory.createEntity();
        entity.setHandle(rpslObject.getKey().toString());
        entity.setVcardArray(generateVcards(rpslObject));
        
        for (RpslAttribute rpslAttribute :
                rpslObject.findAttributes(AttributeType.REMARKS)) {
            Remarks remark = rdapObjectFactory.createRemarks();
            String descr = rpslAttribute.getCleanValue().toString();
            remark.getDescription().add(descr);
            entity.getRemarks().add(remark);
        }

        return entity;
    }

    private Autnum createAutnumResponse(RpslObject rpslObject,
                                        Queue<TaggedRpslObject> qtro) {
        HashMap<String, RpslObject> mtro = new HashMap<String, RpslObject>();
        for (TaggedRpslObject tro : qtro) {
            mtro.put(tro.rpslObject.getKey().toString(),
                     tro.rpslObject);
        }

        Autnum an = RdapHelper.createAutnum();
        an.setHandle(rpslObject.getKey().toString());

        boolean is_autnum =
            rpslObject.getType().getName().equals(
                ObjectType.AUT_NUM.getName()
            );

        BigInteger start;
        BigInteger end;
        if (is_autnum) {
            RpslAttribute asn =
                rpslObject.findAttribute(AttributeType.AUT_NUM);
            String asn_str = asn.getValue().replace("AS", "").replace(" ", "");
            start = end = new BigInteger(asn_str);
        } else {
            RpslAttribute asn_range =
                rpslObject.findAttribute(AttributeType.AS_BLOCK);
            AsBlockRange abr = 
                AsBlockRange.parse(asn_range.getValue().replace(" ", ""));
            start = BigInteger.valueOf(abr.getBegin());
            end   = BigInteger.valueOf(abr.getEnd());
        }

        an.setStartAutnum(start);
        an.setEndAutnum(end);

        an.setCountry(rpslObject.findAttribute(AttributeType.COUNTRY)
                                .getValue().replace(" ", ""));
        
        /* For as-blocks, use the range as the name, since they do not
         * have an obvious 'name' attribute. */
        AttributeType name =
            (is_autnum)
                ? AttributeType.AS_NAME
                : AttributeType.AS_BLOCK; 
        an.setName(rpslObject.findAttribute(name)
                             .getValue().replace(" ", ""));
        /* aut-num records don't have a 'type' or 'status' field, and
         * each is allocated directly by the relevant RIR. 'DIRECT
         * ALLOCATION' is the default value used in the response
         * draft, and it makes sense to use it here too, at least for
         * now. */
        an.setType("DIRECT ALLOCATION");
        /* None of the statuses from [9.1] in json-response is
         * applicable here, so 'status' will be left empty for now. */
 
        List<RpslAttribute> remarks =
            rpslObject.findAttributes(AttributeType.REMARKS);
        List<RpslAttribute> descrs =
            rpslObject.findAttributes(AttributeType.DESCR);
        List<RpslAttribute> all_remarks =  new ArrayList<RpslAttribute>();
        all_remarks.addAll(remarks);
        all_remarks.addAll(descrs);
        for (RpslAttribute rpslAttribute : all_remarks) {
            Remarks remark = rdapObjectFactory.createRemarks();
            String descr = rpslAttribute.getCleanValue().toString();
            remark.getDescription().add(descr);
            an.getRemarks().add(remark);
        }

        /* Load the admin-c and tech-c attributes, find the
         * corresponding records in the queue (if possible), convert
         * them into Entities and add them to the response. */
        List<RpslAttribute> admin_cs =
            rpslObject.findAttributes(AttributeType.ADMIN_C);
        List<RpslAttribute> tech_cs =
            rpslObject.findAttributes(AttributeType.TECH_C);
        List<RpslAttribute> contacts = new ArrayList<RpslAttribute>();
        contacts.addAll(admin_cs);
        contacts.addAll(tech_cs);
        Set<String> seen = new HashSet<String>();
        for (RpslAttribute rpslAttribute : contacts) {
            String key = rpslAttribute.getCleanValue().toString();
            if (seen.contains(key)) {
                continue;
            }
            seen.add(key);
            RpslObject ro = mtro.get(key);
            if (ro != null) {
                Entity e = createEntity(ro);
                an.getEntities().add(e);
            }
        }

        return an;
    }

    private Domain createDomainResponse(RpslObject rpslObject) {
        Domain domain = RdapHelper.createDomain();

        domain.setHandle(rpslObject.getKey().toString());
        domain.setLdhName(rpslObject.getKey().toString());

        // Nameservers
        for  (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.NSERVER)) {
            Nameservers ns = rdapObjectFactory.createNameservers();
            ns.setLdhName(rpslAttribute.getCleanValue().toString());
            domain.getNameservers().add(ns);
        }

        // Remarks
        for  (RpslAttribute rpslAttribute : rpslObject.findAttributes(AttributeType.REMARKS)) {
            Remarks remark = rdapObjectFactory.createRemarks();
            remark.getDescription().add(rpslAttribute.getCleanValue().toString());
            domain.getRemarks().add(remark);
        }

//        // Entities
//        Entity entity = rdapObjectFactory.createEntity();
//        entity.getEntities().add()


//        "entities" :
//
//        //domain.setPort43();
//        domain.getPublicIds();
//        domain.getEntities();
//        domain.getEvents()
//        domain.getLinks()
//        domain.getNameserver()
//        domain.getRemarks()

//        {
//            "handle" : "XXX",
//                "ldhName" : "blah.example.com",
//            ...
//            "nameServers" :
//            [
//            ...
//            ],
//            ...
//            "entities" :
//            [
//            ...
//            ]
//        }
        return domain;

    }

    private void debug(RpslObject rpslObject) {
        List<RpslAttribute> rpslAttributes = rpslObject.getAttributes();

        Iterator<RpslAttribute> iter = rpslAttributes.iterator();
        RpslAttribute ra;
        while (iter.hasNext()) {
            ra = iter.next();
            LOGGER.info(ra.getKey() + ":" + ra.getValue());
        }
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
            // ?? Is it valid to have more than 1 email
            builder.setEmail(attribute.getCleanValue().toString());
        }

        if (builder.isEmpty()) {
            return null;
        }

        return builder.build();
    }

//    private String attributeListToString(List<RpslAttribute> rpslAttributes) {
//        StringBuilder ret = new StringBuilder();
//        for (RpslAttribute rpslAttribute : rpslAttributes) {
//            ret.append(rpslAttribute.getCleanValue()).append(" ");
//        }
//        return ret.toString().trim();
//    }


}
