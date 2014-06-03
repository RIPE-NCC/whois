package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class RndVersionDao implements VersionDao {
    @Override
    public RpslObject getRpslObject(VersionInfo info) {
        return null;
    }

    @Override
    public VersionLookupResult findByKey(ObjectType type, String searchKey) {
        return null;
    }

    @Override
    public Set<ObjectType> getObjectType(String searchKey) {
        return null;
    }
}
