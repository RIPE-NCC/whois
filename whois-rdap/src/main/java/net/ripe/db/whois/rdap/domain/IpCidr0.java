package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cidr0_cidrs", propOrder = {
    "v4prefix",
    "v6prefix",
    "length"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpCidr0 implements Serializable {

    protected String v4prefix;
    protected String v6prefix;
    @XmlElement(required = true)
    protected int length;

    public void setV4prefix(String v4prefix) {
        this.v4prefix = v4prefix;
    }

    public void setV6prefix(String v6prefix) {
        this.v6prefix = v6prefix;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getV4prefix() {
        return v4prefix;
    }

    public String getV6prefix() {
        return v6prefix;
    }

    public int getLength() {
        return length;
    }

}
