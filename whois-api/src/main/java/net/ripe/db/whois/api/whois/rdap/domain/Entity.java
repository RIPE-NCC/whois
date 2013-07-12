package net.ripe.db.whois.api.whois.rdap.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.VCard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
    "handle",
    "vcardArray",
    "roles",
    "publicIds",
    "port43"
})
public class Entity extends RdapObject implements Serializable, Comparable<Entity> {
    @XmlElement(required = true)
    protected String handle;
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> vcardArray;
    protected List<String> roles;
    protected Map publicIds;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public List<Object> getVCardArray() {
        return vcardArray;
    }

    public void setVCardArray(final VCard... vCards) {
        this.vcardArray = Lists.newArrayList();
        this.vcardArray.add("vcard");
        for (VCard next : vCards) {
            this.vcardArray.add(next.getValues());
        }
    }

    public List<String> getRoles() {
        if (roles == null) {
            roles = Lists.newArrayList();
        }
        return this.roles;
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

    @Override
    public int compareTo(Entity o) {
        return this.getHandle().compareTo(o.getHandle());
    }
}
