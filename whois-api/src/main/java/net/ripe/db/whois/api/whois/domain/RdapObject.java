package net.ripe.db.whois.api.whois.domain;

import ezvcard.VCard;
import net.ripe.db.whois.common.domain.CIString;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        //"type",
        //"link",
        //"source",
        "primaryKey",
        //"attributes",
        //"tags"
})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@XmlRootElement(name = "")
public class RdapObject {

    private VCard [] vCards;
    private CIString objectType;

    @XmlElement(name = "handle")
    protected PrimaryKey primaryKey;

    public List<Attribute> getPrimaryKey() {
        return primaryKey != null ? primaryKey.attributes : null;
    }

    public void setPrimaryKey(List<Attribute> value) {
        this.primaryKey = new PrimaryKey(value);
    }

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
