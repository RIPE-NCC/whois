package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
    "handle",
    "vcardArray",
    "roles",
    "publicIds",
    "port43"
})
public class Entity
    extends RdapObject
    implements Serializable
{
    @XmlElement(required = true)
    protected String handle;
    @XmlSchemaType(name = "anySimpleType")
    protected Object vcardArray;
    protected List<String> roles;
    protected HashMap publicIds;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(String value) {
        this.handle = value;
    }

    public Object getVcardArray() {
        return vcardArray;
    }

    public void setVcardArray(Object value) {
        this.vcardArray = value;
    }

    public List<String> getRoles() {
        if (roles == null) {
            roles = new ArrayList<String>();
        }
        return this.roles;
    }

    public HashMap getPublicIds() {
        return publicIds;
    }

    public void setPublicIds(HashMap value) {
        this.publicIds = value;
    }

    public String getPort43() {
        return port43;
    }

    public void setPort43(String value) {
        this.port43 = value;
    }
}
