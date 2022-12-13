package net.ripe.db.nrtm4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "action", "object_class", "primary_key", "object"})
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

    @JsonIgnore
    private final int serialId; // Only set if action is ADD_MODIFY
    private final Action action;
    @JsonProperty("object_class")
    private final ObjectType objectType;
    @JsonProperty("primary_key")
    private final String primaryKey;
    @JsonSerialize(using = RpslObjectSerializer.class)
    private final RpslObject object;

    private DeltaChange(
        final int serialId,
        final Action action,
        final ObjectType objectType,
        final String primaryKey,
        final RpslObject rpslObject
    ) {
        this.serialId = serialId;
        this.action = action;
        this.objectType = objectType;
        this.primaryKey = primaryKey;
        this.object = rpslObject;
    }

    public static DeltaChange addModify(final int serialId, final RpslObject rpslObject) {
        return new DeltaChange(serialId, Action.ADD_MODIFY, null, null, rpslObject);
    }

    public static DeltaChange delete(final ObjectType objectClass, final String primaryKey) {
        return new DeltaChange(0, Action.DELETE, objectClass, primaryKey, null);
    }

    public int getSerialId() {
        return serialId;
    }

    public Action getAction() {
        return action;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public RpslObject getObject() {
        return object;
    }

}
