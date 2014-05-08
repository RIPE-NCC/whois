package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Set;

public interface VersionDao {
    RpslObject getRpslObject(VersionInfo info);

    VersionLookupResult findByKey(ObjectType type, String searchKey);

    Set<ObjectType> getObjectType(String searchKey);
}
