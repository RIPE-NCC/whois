package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "action", "object_class", "primary_key", "object"})
public class DeltaFileRecord implements NrtmFileRecord {

    public enum Action {
        DELETE,
        ADD_MODIFY;

        @JsonValue
        public String toLowerCaseName() {
            return name().toLowerCase();
        }
    }

    private final Action action;
    @JsonProperty("object_class")
    private final ObjectType objectType;
    @JsonProperty("primary_key")
    private final String primaryKey;
    @JsonSerialize(using = RpslObjectJsonSupport.Serializer.class)
    @JsonDeserialize(using = RpslObjectJsonSupport.Deserializer.class)
    private final RpslObject object;

    private DeltaFileRecord() {
        action = null;
        objectType = null;
        primaryKey = null;
        object = null;
    }

    private DeltaFileRecord(
        final Action action,
        final ObjectType objectType,
        final String primaryKey,
        final RpslObject rpslObject
    ) {
        this.action = action;
        this.objectType = objectType;
        this.primaryKey = primaryKey;
        this.object = rpslObject;
    }

    public static DeltaFileRecord addModify(final RpslObject rpslObject) {
        return new DeltaFileRecord(Action.ADD_MODIFY, null, null, rpslObject);
    }

    public static DeltaFileRecord delete(final ObjectType objectType, final String primaryKey) {
        return new DeltaFileRecord(Action.DELETE, objectType, primaryKey, null);
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
