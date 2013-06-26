package net.ripe.db.whois.api.whois.rdap;

import ezvcard.CustomEzvcard;
import ezvcard.VCard;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.TelephoneType;
import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.domain.RdapEntity;
import net.ripe.db.whois.api.whois.domain.RdapResponse;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.Fn;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.Version;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private RdapResponse rdapResponse = new RdapResponse();
    protected Map<String,Object> other = new HashMap<String,Object>();

    public RdapObjectMapper(Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        this.taggedRpslObjectQueue = taggedRpslObjectQueue;
    }

    public RdapResponse build() throws Exception {
        if (taggedRpslObjectQueue == null) {
            return rdapResponse;
        }

        if (!taggedRpslObjectQueue.isEmpty()) {
            primaryTaggedRpslObject = taggedRpslObjectQueue.poll();
        } else {
            throw new Exception("The rpsl queue is empty");
        }

        add(primaryTaggedRpslObject);

        /*while (!whoisObjectQueue.isEmpty()) {
            add(whoisObjectQueue.poll());
        }*/

        return rdapResponse;
    }

    private void add (TaggedRpslObject taggedRpslObject) {
        RpslObject rpslObject = taggedRpslObject.rpslObject;

        ObjectType rpslObjectType = rpslObject.getType();

        if (rpslObjectType.getName().equals(ObjectType.PERSON.getName())) {

            debug(rpslObject);

            // do the vcard dance
            VCard vCard = generateVCard(rpslObject);
            CustomEzvcard.WriterChainJsonSingle vcardWriter = CustomEzvcard.writeJson(vCard);
            vcardWriter.prodId(false);
            RdapEntity rdapEntity= new RdapEntity(rpslObject.getKey(), vcardWriter.go());

            rdapResponse.setRdapObject(rdapEntity);


        } else if (rpslObjectType.equals(ObjectType.ORGANISATION.getName())) {

        } else if (rpslObjectType.equals(ObjectType.ROLE.getName())) {

        } else if (rpslObjectType.equals(ObjectType.IRT.getName())) {

        }
    }

    private void debug (RpslObject rpslObject) {
        List<RpslAttribute> rpslAttributes = rpslObject.getAttributes();

        Iterator<RpslAttribute> iter = rpslAttributes.iterator();
        RpslAttribute ra;
        while (iter.hasNext()) {
            ra = iter.next();
            System.out.println(ra.getKey() + ":" + ra.getValue());
        }
    }

    private VCard generateVCard (RpslObject rpslObject) {
        // make the vcard
        VCard vCard = new VCard();

        List<RpslAttribute> addressAttributes = rpslObject.findAttributes(AttributeType.ADDRESS);
        if (!addressAttributes.isEmpty()) {
            AddressType at = new AddressType();
            at.setExtendedAddress(attributeListToString(addressAttributes));
            vCard.addAddress(at);
        }

        List<RpslAttribute> personAttributes = rpslObject.findAttributes(AttributeType.PERSON);
        if (!personAttributes.isEmpty()) {
            vCard.setFormattedName(attributeListToString(personAttributes));
        }

        List<RpslAttribute> phoneAttributes = rpslObject.findAttributes(AttributeType.PHONE);
        if (!phoneAttributes.isEmpty()) {
            TelephoneType tt = new TelephoneType(attributeListToString(phoneAttributes));
            vCard.addTelephoneNumber(tt);
        }

        List<RpslAttribute> emailAttributes = rpslObject.findAttributes(AttributeType.E_MAIL);
        if (!emailAttributes.isEmpty()) {
            EmailType et = new EmailType(attributeListToString(emailAttributes));
            vCard.addEmail(et);
        }

        return vCard;
    }

    private String attributeListToString (List<RpslAttribute> rpslAttributes) {
        Iterator<RpslAttribute> rpslAttributeIterator = rpslAttributes.iterator();

        String output = "";

        while (rpslAttributeIterator.hasNext()) {
            output = output + " " + rpslAttributeIterator.next().getValue().trim();
        }

        return output.trim();
    }
//
//    public Object generateNameserver() {
//        Nameserver ret = new Nameserver();
//        ret.setHandle("handle");
//        ret.setLdhName("ns1.xn--fo-5ja.example");
//        ret.setUnicodeName("foo.example");
//        ret.getStatus().add("active");
//        Nameserver.IpAddresses ipAddresses = new Nameserver.IpAddresses();
//        ipAddresses.getIpv4().add("192.0.2.1");
//        ipAddresses.getIpv4().add("192.0.2.2");
//        ipAddresses.getIpv6().add("2001:db8::123");
//        ret.setIpAddresses(ipAddresses);
//
//        Nameserver.Remarks remarks1 = new Nameserver.Remarks();
//        remarks1.getDescription().add("She sells sea shells down by the sea shore.");
//        remarks1.getDescription().add( "Originally written by Terry Sullivan.");
//        ret.getRemarks().add(remarks1);
//
//
//        Nameserver.Links link = new Nameserver.Links();
//        link.setHref("http://example.net/nameserver/xxxx");
//        link.setValue("http://example.net/nameserver/xxxx");
//        link.setRel("self");
//        ret.getLinks().add(link);
//
//        ret.setPort43("whois.example.net");
//
//        Nameserver.Events event1 = new Nameserver.Events();
//        event1.setEventAction("registration");
//
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.setTimeInMillis(System.currentTimeMillis());
//        try {
//            event1.setEventDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
//        } catch (Exception ex) {
//
//        }
//        ret.getEvents().add(event1);
//
//        Nameserver.Events event2 = new Nameserver.Events();
//        event2.setEventAction("last changed");
//        try {
//            event2.setEventDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
//        } catch (Exception ex) {
//
//        }
//        event2.setEventActor("joe@example.com");
//        ret.getEvents().add(event2);
//
//        return ret;
//    }

    public Object generateVcards() {


        Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.getVcardArray().add("vcard");

        Version version = new Version();
        version.setEntryType("text");
        version.setEntryValue("4.0");
        entity.getVcardArray().add(version.toObjects());

        Fn fn = new Fn();
        fn.setEntryType("text");
        HashMap vals = new HashMap();
        vals.put("key1","val1");
        vals.put("key2","val2");
        fn.setKeyValues(vals);
        fn.setEntryValue("Joe User");
        entity.getVcardArray().add(fn.toObjects());

        entity.getRoles().add("registrar");

        return entity;

    }


}
