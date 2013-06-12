package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

public interface VersionDao {
    RpslObject getRpslObject(VersionInfo info);

    List<VersionInfo> findByKey(ObjectType type, String searchKey);

    List<ObjectType> getObjectType(String searchKey);
}
