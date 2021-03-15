package net.ripe.db.whois.api.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.vcard.VCard;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "entity", propOrder = {
    "handle",
    "vcardArray",
    "roles",
    "publicIds"
})
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Entity extends RdapObject implements Serializable, Comparable<Entity> {
    @XmlElement(required = true)
    protected String handle;
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> vcardArray;
    protected List<Role> roles;
    protected Map publicIds;

    public Entity() {
        super();
        super.setObjectClassName("entity");
    }

    public Entity(final String handle, final List<Object> vcardArray, final List<Role> roles, final Map publicIds) {
        this();
        this.handle = handle;
        this.vcardArray = vcardArray;
        this.roles = roles;
        this.publicIds = publicIds;
    }

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

    public List<Role> getRoles() {
        if (roles == null) {
            roles = Lists.newArrayList();
        }
        return this.roles;
    }

    public Map getPublicIds() {
        return publicIds;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        return Objects.equals(((Entity)object).handle, handle) &&
            Objects.equals(((Entity)object).vcardArray, vcardArray) &&
            equalsInAnyOrder(roles, ((Entity)object).roles) &&
            Objects.equals(((Entity)object).publicIds, publicIds);
    }

    private boolean equalsInAnyOrder(final List first, final List second) {
        return (first == null && second == null) ||
                ( (first != null && second != null) &&
                    first.size() == second.size() &&
                    first.containsAll(second));
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle, vcardArray, roles, publicIds);
    }

    @Override
    public int compareTo(Entity o) {
        return this.handle.compareTo(o.handle);
    }
}
