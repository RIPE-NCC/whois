package net.ripe.db.whois.api.whois.domain;

import net.ripe.db.whois.common.domain.CIString;
import org.codehaus.jackson.annotate.JsonRawValue;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name = "", propOrder = {
        "handle",
        "vcardArray"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RdapEntity extends RdapObject {

    public CIString handle;
    @JsonRawValue
    public String vcardArray;

    public RdapEntity (CIString handle, String vcardArray) {
        this.handle = handle;
        this.vcardArray = vcardArray;
    }

    public RdapEntity() {}
}
