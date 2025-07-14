package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.annotation.JsonValue;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class MirrorDeltaInfo extends MirrorObjectInfo {

    public enum Action {
        DELETE,
        ADD_MODIFY;

        @JsonValue
        public String toLowerCaseName() {
            return name().toLowerCase();
        }
    }

    private final Action action;
    private final ObjectType objectType;
    private final String primaryKey;

    public MirrorDeltaInfo() {
        super(null);
        this.objectType = null;
        this.primaryKey = null;
        this.action = null;
    }

    public MirrorDeltaInfo(final RpslObject rpslObject) {
        super(rpslObject);
        this.objectType = null;
        this.primaryKey = null;
        this.action = null;
    }

    public MirrorDeltaInfo(final RpslObject object,
                           final String action,
                           final String objectType,
                           final String primaryKey) {
        super(object);
        this.action = Action.valueOf(action.toUpperCase());
        this.objectType = objectType != null ? ObjectType.getByName(objectType) : null;
        this.primaryKey = primaryKey;
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
}
