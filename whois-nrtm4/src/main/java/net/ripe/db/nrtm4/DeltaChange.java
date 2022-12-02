package net.ripe.db.nrtm4;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaChange {

    enum Action {
        DELETE,
        ADD_MODIFY;

        @Override
        @JsonValue
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final Action action;
    @JsonProperty("object_class")
    private final ObjectType objectClass;
    @JsonProperty("primary_key")
    private final String primaryKey;
    private final RpslObject object;

    public DeltaChange(
        final Action action,
        final ObjectType objectClass,
        final String primaryKey,
        final RpslObject rpslObject
    ) {
        this.action = action;
        this.objectClass = objectClass;
        this.primaryKey = primaryKey;
        this.object = rpslObject;
    }

    public Action getAction() {
        return action;
    }

    public ObjectType getObjectClass() {
        return objectClass;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public RpslObject getObject() {
        return object;
    }

}
