package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;

import java.util.Map;


public interface SerialDao {

    SerialRange getSerials();

    SerialEntry getById(int serialId);

    SerialEntry getByIdForNrtm(int serialId);

    Integer getAgeOfExactOrNextExistingSerial(int serialId);

    Map<Integer, Integer> getMaxSerialIdWithObjectCount();

    Integer getObjectCountUntilObjectId(final int objectId);
}
