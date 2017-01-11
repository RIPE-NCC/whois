package net.ripe.db.whois.update.dns.zonemaster;


import javax.ws.rs.core.Response;

public abstract class ZonemasterRequestSupport implements ZonemasterRequest {

    @Override
    public Response execute() {
        ZonemasterRestClient zrc = new ZonemasterRestClient();
        return zrc.sendRequest(this);
    }
}
