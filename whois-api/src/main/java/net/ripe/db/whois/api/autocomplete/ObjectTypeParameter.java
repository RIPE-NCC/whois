package net.ripe.db.whois.api.autocomplete;

import net.ripe.db.whois.common.rpsl.ObjectType;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ObjectTypeParameter {

    private final ObjectType objectType;

    public ObjectTypeParameter(final String objectType) throws WebApplicationException {
        try {
            this.objectType = ObjectType.getByName(objectType);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid object type:" + objectType)
                    .build());
        }
    }

    @NotNull
    public ObjectType getObjectType() {
        return objectType;
    }

    @Override
    public String toString() {
        return objectType.getName();
    }

}
