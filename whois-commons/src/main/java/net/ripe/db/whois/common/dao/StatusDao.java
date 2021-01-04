package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.domain.CIString;

import java.util.List;
import java.util.Map;

public interface StatusDao {

    CIString getStatus(int objectId);

    Map<Integer, CIString> getStatus(List<Integer> objectIds);
}
