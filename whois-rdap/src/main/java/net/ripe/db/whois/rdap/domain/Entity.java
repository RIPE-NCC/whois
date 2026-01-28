package net.ripe.db.whois.rdap.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import net.ripe.db.whois.rdap.domain.vcard.VCard;

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
    protected List<PublicIds> publicIds;

    public Entity() {
        super();
        super.setObjectClassName("entity");
    }

    public Entity(final String handle, final List<Object> vcardArray, final List<Role> roles, final List<PublicIds> publicIds) {
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

    public List<PublicIds> getPublicIds() {
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
