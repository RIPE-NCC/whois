package net.ripe.db.whois.api.whois;

import ezvcard.VCard;
import ezvcard.types.StructuredNameType;
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

            // do the vcard dance
            VCard vCard = generateVCard(rpslObject);
            RdapEntity rdapEntity= new RdapEntity(rpslObject.getKey(), vCard.writeJson());

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
        StructuredNameType n = new StructuredNameType();
        n.setFamily("Doe");
        n.setGiven("Jonathan");
        n.addPrefix("Mr");
        vCard.setStructuredName(n);
        vCard.setFormattedName("John Doe");

        return vCard;
    }
}
