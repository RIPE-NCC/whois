package net.ripe.db.whois.update.autokey.dao;

import net.ripe.db.whois.update.domain.NicHandle;

public interface NicHandleRepository extends AutoKeyRepository<NicHandle> {

    boolean isAvailable(NicHandle nicHandle);

    void createRange(String space, String suffix, int start, int end);
}
