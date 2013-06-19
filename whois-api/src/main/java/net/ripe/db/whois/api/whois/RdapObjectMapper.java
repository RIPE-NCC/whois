package net.ripe.db.whois.api.whois;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.domain.RdapObject;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class RdapObjectMapper {
    private TaggedRpslObject primaryTaggedRpslObject;
    private Queue<TaggedRpslObject> taggedRpslObjectQueue;
    private RdapObject rdapObject = new RdapObject();

    public RdapObjectMapper(Queue<TaggedRpslObject> taggedRpslObjectQueue) {
        this.taggedRpslObjectQueue = taggedRpslObjectQueue;
    }

    public RdapObject build () throws Exception {
        if (taggedRpslObjectQueue == null) {
            return rdapObject;
        }

        if (!taggedRpslObjectQueue.isEmpty()) {
            primaryTaggedRpslObject = taggedRpslObjectQueue.poll();
        } else {
            throw new Exception("The rpsl queue is empty");
        }

        System.out.println(primaryTaggedRpslObject.rpslObject.getType());

        add(primaryTaggedRpslObject);

        /*while (!whoisObjectQueue.isEmpty()) {
            add(whoisObjectQueue.poll());
        }*/

        return rdapObject;
    }

    private void add (TaggedRpslObject taggedRpslObject) {
        RpslObject rpslObject = taggedRpslObject.rpslObject;

        ObjectType rpslObjectType = rpslObject.getType();

        if (rpslObjectType.getName().equals(ObjectType.PERSON.getName())) {
            RpslAttribute primaryKey = getPrimaryKey(rpslObject);
            CIString handle = CIString.ciString(stripWhitespace(primaryKey.getValue()));

            debug(rpslObject);

            rdapObject.setHandle(handle);

        } else if (rpslObjectType.equals(ObjectType.ORGANISATION.getName())) {

        } else if (rpslObjectType.equals(ObjectType.ROLE.getName())) {

        } else if (rpslObjectType.equals(ObjectType.IRT.getName())) {

        }
    }

    private static RpslAttribute getPrimaryKey(final RpslObject rpslObject) {
        final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(rpslObject.getType());
        Iterator<AttributeType> iterator = Sets.intersection(objectTemplate.getKeyAttributes(), objectTemplate.getLookupAttributes()).iterator();
        if (iterator.hasNext()) {
            AttributeType primaryKey = iterator.next();
            if (!iterator.hasNext()) {
                return rpslObject.findAttribute(primaryKey);
            }
        }

        throw new IllegalArgumentException("Couldn't find primary key attribute for type: " + rpslObject.getType());
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

    private String stripWhitespace (String str) {
         return str.replace(" ","");
    }
}
