package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.TaggedRpslObject;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapObjectMapper.class);


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

        debug(rpslObject);

        String name = rpslObjectType.getName();

        if (name.equals(ObjectType.PERSON.getName())) {

            Entity entity = new ObjectFactory().createEntity();
            entity.setHandle(rpslObject.getKey().toString());
            entity.setVcardArray(generateVcards(rpslObject));
            rdapResponse = entity;

        } else if (name.equals(ObjectType.ORGANISATION.getName())) {

        } else if (name.equals(ObjectType.ROLE.getName())) {

        } else if (name.equals(ObjectType.IRT.getName())) {

        } else if (name.equals(ObjectType.DOMAIN.getName())) {

            Domain domain = new ObjectFactory().createDomain();
            domain.setHandle(rpslObject.getKey().toString());

            rdapResponse = domain;
        } else if (name.equals(ObjectType.INETNUM.getName())
                || name.equals(ObjectType.INET6NUM.getName())) {

            Ip ip = new ObjectFactory().createIp();
            ip.setHandle(rpslObject.getKey().toString());

            rdapResponse = ip;
        } else if (name.equals(ObjectType.AUT_NUM.getName())) {
            Autnum an = new ObjectFactory().createAutnum();
            an.setHandle(rpslObject.getKey().toString());

            RpslAttribute asn =
                rpslObject.findAttribute(AttributeType.AUT_NUM);
            String asn_str = asn.getValue().replace("AS", "")
                                           .replace(" ", "");

            an.setStartAutnum(new BigInteger(asn_str));
            an.setEndAutnum(new BigInteger(asn_str));

            an.setName("name");
            an.setType("type");
            an.setCountry("AU");
            an.getStatus().add("ALLOCATED");

            rdapResponse = an;
        }
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
