package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.annotate.JsonUnwrapped;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "", propOrder = {
        "rdapConformance",
        "notices",
        "object"
})
public class RdapResponse {

    public RdapNotice[] notices = {
            new RdapNotice()
    };
    public String[] rdapConformance = {"rdap_level_0"};

    @JsonUnwrapped
    public RdapObject object;

    public RdapResponse(RdapObject object) {
        this.object = object;
    }

    public RdapResponse() {}

    public void setRdapObject (RdapObject rdapObject) {
        this.object = rdapObject;
    }
}
