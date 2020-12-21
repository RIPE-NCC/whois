package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.CIString;

public interface StatusDao {

    CIString getStatus(int objectId);

}
