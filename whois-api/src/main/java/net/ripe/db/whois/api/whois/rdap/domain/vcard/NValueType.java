package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import net.ripe.db.whois.api.whois.rdap.VCardProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nValueType", propOrder = {
    "surname",
    "given",
    "prefix",
    "suffix",
    "honorifics"
})
public class NValueType
    extends VCardProperty
    implements Serializable
{

    @XmlElement(defaultValue = "")
    protected String surname;
    @XmlElement(defaultValue = "")
    protected String given;
    @XmlElement(defaultValue = "")
    protected String prefix;
    @XmlElement(defaultValue = "")
    protected String suffix;
    protected NValueType.Honorifics honorifics = new NValueType.Honorifics();

    public void setSurname(String value) {
        this.surname = value;
    }

    public void setGiven(String value) {
        this.given = value;
    }

    public void setPrefix(String value) {
        this.prefix = value;
    }

    public void setSuffix(String value) {
        this.suffix = value;
    }

    public NValueType.Honorifics getHonorifics() {
        return honorifics;
    }

    public void setHonorifics(NValueType.Honorifics value) {
        this.honorifics = value;
    }

    public String getSurname() {
        if (null == surname) {
            return "";
        }
        return surname;
    }

    public String getGiven() {
        if (null == given) {
            return "";
        }
        return given;
    }

    public String getPrefix() {
        if (null == prefix) {
            return "";
        }
        return prefix;
    }

    public String getSuffix() {
        if (null == suffix) {
            return "";
        }
        return suffix;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "prefix",
        "suffix"
    })
    public static class Honorifics
        extends VCardProperty
        implements Serializable
    {

        @XmlElement(defaultValue = "")
        protected String prefix;
        @XmlElement(defaultValue = "")
        protected String suffix;

        public void setPrefix(String value) {
            this.prefix = value;
        }

        public void setSuffix(String value) {
            this.suffix = value;
        }

        public String getPrefix() {
            if (null == prefix) {
                return "";
            }
            return prefix;
        }

        public String getSuffix() {
            if (null == suffix) {
                return "";
            }
            return suffix;
        }
    }
}
