package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.RdapEntity;
import net.ripe.db.whois.api.whois.domain.RdapResponse;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private RdapResponse rdapResponse = new RdapResponse();

    public RdapObjectMapper(Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        this.taggedRpslObjectQueue = taggedRpslObjectQueue;
    }

    public RdapResponse build () throws Exception {
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

            /*rdapObject.decoratePrimary();
            rdapObject.setHandle(rpslObject.getKey());*/

            /*//build some vCards
            List<RdapVCard> vCards = Lists.newArrayList();

            // do some sort of iteration here but for now 1's enuff what
            RdapVCard vCard = new RdapVCard();
            vCards.add(vCard);

            // turn it into an array
            rdapObject.setvCards(vCards.toArray(new RdapVCard[]{}));*/

            // do the vcard dance
            /*List<String> vCards = Lists.newArrayList();

            // iterate here
            VCard vCard = generateVcard(rpslObject);*/

            RdapEntity rdapEntity= new RdapEntity(rpslObject.getKey(), );

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
}
