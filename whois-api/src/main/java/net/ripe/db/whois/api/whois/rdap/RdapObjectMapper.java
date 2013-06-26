package net.ripe.db.whois.api.whois.rdap;

import ezvcard.VCard;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.TelephoneType;
import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.ObjectFactory;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
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

    private void add(TaggedRpslObject taggedRpslObject) {
        RpslObject rpslObject = taggedRpslObject.rpslObject;

        ObjectType rpslObjectType = rpslObject.getType();

        if (rpslObjectType.getName().equals(ObjectType.PERSON.getName())) {

            debug(rpslObject);

            Entity entity = new ObjectFactory().createEntity();
            entity.setHandle(rpslObject.getKey().toString());

            rdapResponse = entity;

        } else if (rpslObjectType.equals(ObjectType.ORGANISATION.getName())) {

        } else if (rpslObjectType.equals(ObjectType.ROLE.getName())) {

        } else if (rpslObjectType.equals(ObjectType.IRT.getName())) {

        }
    }

    private void debug(RpslObject rpslObject) {
        List<RpslAttribute> rpslAttributes = rpslObject.getAttributes();

        Iterator<RpslAttribute> iter = rpslAttributes.iterator();
        RpslAttribute ra;
        while (iter.hasNext()) {
            ra = iter.next();
            System.out.println(ra.getKey() + ":" + ra.getValue());
        }
    }

    private VCard generateVCard(RpslObject rpslObject) {


        List<RpslAttribute> addressAttributes = rpslObject.findAttributes(AttributeType.ADDRESS);
        if (!addressAttributes.isEmpty()) {
            AddressType at = new AddressType();
            at.setExtendedAddress(attributeListToString(addressAttributes));
            //vCard.addAddress(at);
        }

        List<RpslAttribute> personAttributes = rpslObject.findAttributes(AttributeType.PERSON);
        if (!personAttributes.isEmpty()) {
            //vCard.setFormattedName(attributeListToString(personAttributes));
        }

        List<RpslAttribute> phoneAttributes = rpslObject.findAttributes(AttributeType.PHONE);
        if (!phoneAttributes.isEmpty()) {
            TelephoneType tt = new TelephoneType(attributeListToString(phoneAttributes));
            //vCard.addTelephoneNumber(tt);
        }

        List<RpslAttribute> emailAttributes = rpslObject.findAttributes(AttributeType.E_MAIL);
        if (!emailAttributes.isEmpty()) {
            EmailType et = new EmailType(attributeListToString(emailAttributes));
            //vCard.addEmail(et);
        }

        return null;
    }

    private String attributeListToString(List<RpslAttribute> rpslAttributes) {
        Iterator<RpslAttribute> rpslAttributeIterator = rpslAttributes.iterator();

        String output = "";

        while (rpslAttributeIterator.hasNext()) {
            output = output + " " + rpslAttributeIterator.next().getValue().trim();
        }

        return output.trim();
    }


}
