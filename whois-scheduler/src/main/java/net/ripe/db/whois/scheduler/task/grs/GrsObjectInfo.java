package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.concurrent.Immutable;

@Immutable
class GrsObjectInfo {
    private final int objectId;
    private final int sequenceId;
    private final RpslObject rpslObject;

    GrsObjectInfo(final int objectId, final int sequenceId, final RpslObject rpslObject) {
        this.objectId = objectId;
        this.sequenceId = sequenceId;
        this.rpslObject = rpslObject;
    }

    public int getObjectId() {
        return objectId;
    }

    public ObjectType getType() {
        return rpslObject.getType();
    }

    public String getKey() {
        return rpslObject.getKey().toString();
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public RpslObjectUpdateInfo createUpdateInfo() {
        return new RpslObjectUpdateInfo(objectId, sequenceId, rpslObject.getType(), rpslObject.getKey().toString());
    }
}
