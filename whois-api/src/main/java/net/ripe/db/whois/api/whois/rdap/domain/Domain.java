package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "handle",
    "ldhName",
    "unicodeName",
    "nameservers",
    "publicIds",
    "port43"
})
@XmlRootElement(name = "domain")
public class Domain extends RdapObject implements Serializable {

    @XmlElement(required = true)
    protected String handle;
    @XmlElement(required = true)
    protected String ldhName;
    @XmlElement(required = true)
    protected String unicodeName;
    @XmlElement(required = true)
    protected List<Nameserver> nameservers;
    protected Map publicIds;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public String getLdhName() {
        return ldhName;
    }

    public void setLdhName(final String value) {
        this.ldhName = value;
    }

    public String getUnicodeName() {
        return unicodeName;
    }

    public void setUnicodeName(final String value) {
        this.unicodeName = value;
    }

    public List<Nameserver> getNameservers() {
        if (nameservers == null) {
            nameservers = Lists.newArrayList();
        }
        return this.nameservers;
    }

    public Map getPublicIds() {
        return publicIds;
    }

    public void setPublicIds(final Map value) {
        this.publicIds = value;
    }

    public String getPort43() {
        return port43;
    }

    public void setPort43(final String value) {
        this.port43 = value;
    }
}
