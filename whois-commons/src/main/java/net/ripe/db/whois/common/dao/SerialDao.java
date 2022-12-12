package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;

import java.util.List;


public interface SerialDao {

    SerialRange getSerials();

    SerialEntry getById(int serialId);

    SerialEntry getByIdForNrtm(int serialId);

    List<SerialEntry> getSerialEntriesSince(int serialId);

    List<SerialEntry> getSerialEntriesBetweenInclusive(int serialIdFrom, int serialIdTo);

    List<SerialEntry> getSerialEntriesFromLast();

    Integer getAgeOfExactOrNextExistingSerial(int serialId);
}
