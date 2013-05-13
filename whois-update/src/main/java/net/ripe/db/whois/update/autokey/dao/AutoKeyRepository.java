package net.ripe.db.whois.update.autokey.dao;

import net.ripe.db.whois.update.domain.AutoKey;

public interface AutoKeyRepository<T extends AutoKey> {
    boolean claimSpecified(T autoKey);

    T claimNextAvailableIndex(String space, String suffix);
}
