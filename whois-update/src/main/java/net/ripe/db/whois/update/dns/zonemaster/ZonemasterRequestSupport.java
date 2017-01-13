package net.ripe.db.whois.update.dns.zonemaster;


import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;

public abstract class ZonemasterRequestSupport implements ZonemasterRequest {

    @Autowired
    private ZonemasterRestClient zonemasterRestClient;

    public Response execute() {
        System.out.println("XXX XXX XXX zonemasterRestClient " + zonemasterRestClient);
        return zonemasterRestClient.sendRequest(this);
    }
}
