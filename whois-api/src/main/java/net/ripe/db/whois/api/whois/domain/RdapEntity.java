package net.ripe.db.whois.api.whois.domain;

import org.codehaus.jackson.annotate.JsonRawValue;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "", propOrder = {
        "handle",
        "vcardArray"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RdapEntity extends RdapObject {

    public String handle;
    @JsonRawValue
    public String vcardArray;

    public RdapEntity (String handle, String vcardArray) {
        this.handle = handle;
        this.vcardArray = vcardArray;
    }

    public RdapEntity() {}
}
