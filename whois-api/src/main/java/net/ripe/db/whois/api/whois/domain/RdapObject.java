package net.ripe.db.whois.api.whois.domain;

import net.ripe.db.whois.common.domain.CIString;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;

/*@XmlAccessorType(XmlAccessType.FIELD)*/
@XmlType(name = "", propOrder = {
        //"type",
        //"link",
        "rdapConformance",
        "handle",
        "vcardArray"
        //"attributes",
        //"tags"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RdapObject {

    public String[] rdapConformance;
    public RdapVCard[] vcardArray;
    public CIString handle;

    public void setHandle(CIString handle) {
        this.handle = handle;
    }

    public void setvCards(RdapVCard[] vcardArray) {
        this.vcardArray = vcardArray;
    }

    public void decoratePrimary() {
        rdapConformance = new String[] {"rdap_level_0"};
    }

    //public void addVCard(VCard vcard) {

    //}

    // TODO work out how to remove the root element

    /*

            "person:  Pauleth Palthen\n" +
            "address: Singel 258\n" +
            "phone:   +31-1234567890\n" +
            "e-mail:  noreply@ripe.net\n" +
            "mnt-by:  OWNER-MNT\n" +
            "nic-hdl: PP1-TEST\n" +
            "changed: noreply@ripe.net 20120101\n" +
            "remarks: remark\n" +
            "source:  TEST\n");

     */


}
