package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import net.ripe.db.whois.api.whois.rdap.VCardProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nEntryValueType", propOrder = {
    "n1",
    "n2",
    "n3",
    "n4",
    "nPost"
})
public class NEntryValueType
    extends VCardProperty
    implements Serializable
{
    @XmlElement(required = true, defaultValue = "")
    protected String n1;
    @XmlElement(required = true, defaultValue = "")
    protected String n2;
    @XmlElement(required = true, defaultValue = "")
    protected String n3;
    @XmlElement(required = true, defaultValue = "")
    protected String n4;
    @XmlElement(required = true)
    protected List<String> nPost;

    public void setN1(String value) {
        this.n1 = value;
    }

    public void setN2(String value) {
        this.n2 = value;
    }

    public void setN3(String value) {
        this.n3 = value;
    }

    public void setN4(String value) {
        this.n4 = value;
    }

    public List<String> getNPost() {
        if (nPost == null) {
            nPost = new ArrayList<String>();
        }
        return this.nPost;
    }

    public String getN1() {
        if (null == n1) {
            return "";
        }
        return n1;
    }

    public String getN2() {
        if (null == n2) {
            return "";
        }
        return n2;
    }

    public String getN3() {
        if (null == n3) {
            return "";
        }
        return n3;
    }

    public String getN4() {
        if (null == n4) {
            return "";
        }
        return n4;
    }
}
