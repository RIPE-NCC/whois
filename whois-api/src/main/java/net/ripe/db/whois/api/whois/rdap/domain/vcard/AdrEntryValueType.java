package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import net.ripe.db.whois.api.whois.rdap.VCardProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adrEntryValueType", propOrder = {
    "pobox",
    "ext",
    "street",
    "locality",
    "region",
    "code",
    "country"
})
public class AdrEntryValueType
    extends VCardProperty
    implements Serializable
{

    @XmlElement(defaultValue = "")
    protected String pobox;
    @XmlElement(defaultValue = "")
    protected String ext;
    @XmlElement(defaultValue = "")
    protected String street;
    @XmlElement(defaultValue = "")
    protected String locality;
    @XmlElement(defaultValue = "")
    protected String region;
    @XmlElement(defaultValue = "")
    protected String code;
    @XmlElement(defaultValue = "")
    protected String country;

    public void setPobox(String value) {
        this.pobox = value;
    }

    public void setExt(String value) {
        this.ext = value;
    }

    public void setStreet(String value) {
        this.street = value;
    }

    public void setLocality(String value) {
        this.locality = value;
    }

    public void setRegion(String value) {
        this.region = value;
    }

    public void setCode(String value) {
        this.code = value;
    }

    public void setCountry(String value) {
        this.country = value;
    }

    public String getPobox() {
        if (null == pobox) {
            return "";
        }
        return pobox;
    }

    public String getExt() {
        if (null == ext) {
            return "";
        }
        return ext;
    }

    public String getStreet() {
        if (null == street) {
            return "";
        }
        return street;
    }

    public String getLocality() {
        if (null == locality) {
            return "";
        }
        return locality;
    }

    public String getRegion() {
        if (null == region) {
            return "";
        }
        return region;
    }

    public String getCode() {
        if (null == code) {
            return "";
        }
        return code;
    }

    public String getCountry() {
        if (null == country) {
            return "";
        }
        return country;
    }

}
