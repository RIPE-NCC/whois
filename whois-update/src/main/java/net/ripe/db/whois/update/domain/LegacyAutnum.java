package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.update.dao.LegacyAutnumDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class LegacyAutnum {
    private List<CIString> cachedLegacyAutnums;
    private final LegacyAutnumDao legacyAutnumDao;

    @Autowired
    public LegacyAutnum(final LegacyAutnumDao legacyAutnumDao) {
        this.legacyAutnumDao = legacyAutnumDao;
    }

    @PostConstruct
    public synchronized void init() {
        cachedLegacyAutnums = legacyAutnumDao.load();
    }

    public boolean contains(final CIString autnum) {
        return cachedLegacyAutnums.contains(autnum);
    }

    public int getTotal() {
        return cachedLegacyAutnums.size();
    }

}
