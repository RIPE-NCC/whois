package net.ripe.db.whois.api.whois.rdap.domain;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
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
public class Entity extends RdapObject implements Serializable {
    @XmlElement(required = true)
    protected String handle;
    @XmlSchemaType(name = "anySimpleType")
    protected Object vcardArray;
    protected List<String> roles;
    protected Map publicIds;
    protected String port43;

    public String getHandle() {
        return handle;
    }

    public void setHandle(final String value) {
        this.handle = value;
    }

    public Object getVcardArray() {
        return vcardArray;
    }

    public void setVcardArray(final Object value) {
        this.vcardArray = value;
    }

    public List<String> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
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
}
