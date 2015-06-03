package net.ripe.db.whois.api.autocomplete;

import net.ripe.db.whois.common.rpsl.AttributeType;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class AttributeTypeParameter {

    private final AttributeType attributeType;

    public AttributeTypeParameter(final String attributeType) throws WebApplicationException {
        try {
            this.attributeType = AttributeType.getByName(attributeType);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid attribute type:" + attributeType)
                    .build());
        }
    }

    @NotNull
    public AttributeType getAttributeType() {
        return attributeType;
    }
}
