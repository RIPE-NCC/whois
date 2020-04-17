package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.ResourceDataDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Set;

public abstract class AbstractAutoritativeResourceImportTask {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private boolean enabled;

    private final ResourceDataDao resourceDataDao;

    @Autowired
    public AbstractAutoritativeResourceImportTask(@Value("${grs.import.enabled}") final boolean enabled,
                                                  final ResourceDataDao resourceDataDao) {
        this.resourceDataDao = resourceDataDao;
        this.enabled = enabled;
    }

    protected void doImport(final Set<String> sourceNames) {
        if (!enabled) {
            LOGGER.info("Authoritative resource import task is disabled");
            return;
        }

        for (final String sourceName : sourceNames) {
            try {
                final AuthoritativeResource authoritativeResource = fetchAuthoritativeResource(sourceName);
                resourceDataDao.store(sourceName, authoritativeResource);
            } catch (Exception e) {
                LOGGER.warn("Exception processing " + sourceName, e);
            }
        }
    }

    protected abstract AuthoritativeResource fetchAuthoritativeResource(final String sourceName) throws IOException;
}
