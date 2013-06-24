package net.ripe.db.whois.api.whois;

import ezvcard.CustomEzvcard;
import ezvcard.VCard;
import ezvcard.types.AddressType;
import ezvcard.types.EmailType;
import ezvcard.types.StructuredNameType;
import ezvcard.types.TelephoneType;
import net.ripe.db.whois.api.whois.domain.RdapEntity;
import net.ripe.db.whois.api.whois.domain.RdapResponse;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.*;

public class RdapObjectMapper {
    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private RdapResponse rdapResponse = new RdapResponse();
    protected Map<String,Object> other = new HashMap<String,Object>();

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
        StructuredNameType n = new StructuredNameType();
        AddressType at = new AddressType();
        at.setStreetAddress(rpslObject.findAttribute(AttributeType.ADDRESS).getValue().trim());
        vCard.addAddress(at);

        String name = rpslObject.findAttribute(AttributeType.PERSON).getValue().trim();
        if (name != "") {
            vCard.setFormattedName(name);
        }

        TelephoneType tt = new TelephoneType(rpslObject.findAttribute(AttributeType.PHONE).getValue().trim());
        vCard.addTelephoneNumber(tt);

        try {
            if (rpslObject.findAttribute(AttributeType.E_MAIL).getValue().trim() != "") {
                EmailType et = new EmailType(rpslObject.findAttribute(AttributeType.E_MAIL).getValue().trim());
                vCard.addEmail(et);
            }
        } catch (Exception e) {
            // piece o crap
            System.out.println(e);
        }

        return vCard;
    }

}
