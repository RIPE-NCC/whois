package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.serials.SerialEntry;
import net.ripe.db.whois.common.domain.serials.SerialRange;

import java.util.List;
import java.util.stream.Stream;


public interface SerialDao {

    SerialRange getSerials();

    SerialEntry getById(int serialId);

    SerialEntry getByIdForNrtm(int serialId);

    List<SerialEntry> getSerialEntriesSince(int serialId);

    /**
     * Exclude 'from', include 'to'. Find changes between snapshots.
     */
    Stream<SerialEntry> getSerialEntriesBetween(int serialIdFrom, int serialIdTo);

    Integer getAgeOfExactOrNextExistingSerial(int serialId);
}
