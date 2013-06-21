package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "", propOrder = {
        "handle",
        "startAddress",
        "endAddress",
        "ipVersion",
        "entities"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RdapIpNetwork extends RdapObject {
    public String handle = "202.12.28.0-202.12.29.255";

    public String startAddress = "202.12.28.0";
    public String endAddress   = "202.12.29.255";
    public String ipVersion    = "v4";

    public RdapEntity[] entities = {
            new RdapEntity()
    };
}
