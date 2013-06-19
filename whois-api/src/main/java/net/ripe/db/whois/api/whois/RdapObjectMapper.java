package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.RdapObject;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.util.Queue;

public class RdapObjectMapper {
    private WhoisObject primaryWhoisObject;
    private Queue<WhoisObject> whoisObjectQueue;
    private RdapObject rdapObject = new RdapObject();

    public RdapObjectMapper(Queue<WhoisObject> whoisObjectQueue) {
        this.whoisObjectQueue = whoisObjectQueue;
    }

    public RdapObject build () throws Exception {
        if (whoisObjectQueue == null) {
            return rdapObject;
        }

        if (!whoisObjectQueue.isEmpty()) {
            primaryWhoisObject = whoisObjectQueue.poll();
        } else {
            throw new Exception("The rpsl queue is empty");
        }

        System.out.println(primaryWhoisObject.getType());

        add(primaryWhoisObject);

        /*while (!whoisObjectQueue.isEmpty()) {
            add(whoisObjectQueue.poll());
        }*/

        return rdapObject;
    }

    private void add (WhoisObject whoisObject) {
        String whoisObjectType = whoisObject.getType();

        if (whoisObjectType.equals(ObjectType.PERSON.getName())) {
            rdapObject.setPrimaryKey(whoisObject.getPrimaryKey());

        } else if (whoisObjectType.equals(ObjectType.ORGANISATION.getName())) {

        } else if (whoisObjectType.equals(ObjectType.ROLE.getName())) {

        } else if (whoisObjectType.equals(ObjectType.IRT.getName())) {

        }
    }
}
