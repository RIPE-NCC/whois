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

    protected static final String SOURCE_NAME_RIPE = "RIPE";

    private final boolean enabled;
    private final ResourceDataDao resourceDataDao;

    @Autowired
    public AbstractAutoritativeResourceImportTask(@Value("${grs.import.enabled:false}") final boolean enabled,
                                                  final ResourceDataDao resourceDataDao) {
        this.resourceDataDao = resourceDataDao;
        this.enabled = enabled;
    }

    void doImport(final Set<String> sourceNames) {
        if (!enabled) {
            return;
        }

        for (final String sourceName : sourceNames) {
            try {
                final AuthoritativeResource authoritativeResource = fetchAuthoritativeResource(sourceName);
                if (authoritativeResource != null && !authoritativeResource.getResources().isEmpty()) {
                    resourceDataDao.store(sourceName, authoritativeResource);
                }
            } catch (Exception e) {
                LOGGER.warn("Exception processing {} due to {}: {}", sourceName, e.getClass().getName(), e.getMessage());
            }
        }
    }

    protected abstract AuthoritativeResource fetchAuthoritativeResource(final String sourceName) throws IOException;
}
