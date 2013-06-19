package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.whois.domain.WhoisObject;

import java.util.Queue;

public class RdapObjectFactory {
    private WhoisObject primaryWhoisObject;
    private Queue<WhoisObject> whoisObjectQueue;

    public RdapObjectFactory (Queue<WhoisObject> whoisObjectQueue) {
        this.whoisObjectQueue = whoisObjectQueue;
    }

    public RdapObject build () throws Exception {
        if (whoisObjectQueue == null) {
            return new RdapObject();
        }

        if (!whoisObjectQueue.isEmpty()) {
            primaryWhoisObject = whoisObjectQueue.poll();
        } else {
            throw new Exception("The rpsl queue is empty");
        }

        System.out.println(primaryWhoisObject.getType());

        RdapObject ro = new RdapObject();

        return ro;
    }
}
