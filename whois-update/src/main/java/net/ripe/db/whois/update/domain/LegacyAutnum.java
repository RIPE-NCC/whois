package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.update.dao.LegacyAutnumDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class LegacyAutnum {
    private List<CIString> cachedLegacyAutnums;
    private final LegacyAutnumDao legacyAutnumDao;

    @Autowired
    public LegacyAutnum(final LegacyAutnumDao legacyAutnumDao) {
        this.legacyAutnumDao = legacyAutnumDao;
    }

    public List<CIString> getLegacyAutnums() {
        if (cachedLegacyAutnums == null) {
            cachedLegacyAutnums = legacyAutnumDao.readLegacyAutnums();
        }
        return cachedLegacyAutnums;
    }

    public boolean contains(final CIString autnumKey) {
        return cachedLegacyAutnums.contains(autnumKey);
    }

    /**
     * Expects a file with an AS number on each line
     */
    public void importLegacyAutnums(@Value("legacy.autnum.file.path:") final String filePath) throws IOException {
        legacyAutnumDao.store(Files.readAllLines(Paths.get(filePath), Charset.defaultCharset()));
    }
}
