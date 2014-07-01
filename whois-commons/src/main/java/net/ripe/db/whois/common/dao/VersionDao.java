package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.Nullable;
import java.util.Set;

public interface VersionDao {
    RpslObject getRpslObject(VersionInfo info);

    @Nullable
    VersionLookupResult findByKey(ObjectType type, String searchKey);

    Set<ObjectType> getObjectType(String searchKey);

    @Nullable
    RpslObject findHistoricalObject(ObjectType next, String searchValue, int objectVersion);
}
