package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

public interface RpslObjectUpdateDao  extends ReferenceDao {

    RpslObjectUpdateInfo lookupObject(ObjectType type, String pkey);

    RpslObjectUpdateInfo deleteObject(int objectId, String pkey);

    RpslObjectUpdateInfo undeleteObject(int objectId);

    RpslObjectUpdateInfo updateObject(int objectId, RpslObject object);

    RpslObjectUpdateInfo createObject(RpslObject object);
}
