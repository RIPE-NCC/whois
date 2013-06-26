package net.ripe.db.whois.api.whois.rdap;

public class RdapResponse {

    private Object rdapObject;

    public RdapResponse() {}

    public RdapResponse(Object o) {}

    public void setObject (Object o) {
        this.rdapObject = o;
    }

    public Object getRdapObject () {
        return rdapObject;
    }

}
