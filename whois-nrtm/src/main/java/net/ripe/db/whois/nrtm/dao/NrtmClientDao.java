package net.ripe.db.whois.nrtm.dao;

import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;

public interface NrtmClientDao {

    RpslObjectUpdateInfo createObject(RpslObject object, int serialId);

    RpslObjectUpdateInfo updateObject(RpslObject object, RpslObjectUpdateInfo objectInfo, int serialId);

    void deleteObject(RpslObjectUpdateInfo info, int serialId);

    boolean objectExistsWithSerial(int serialId, int objectId);
}
