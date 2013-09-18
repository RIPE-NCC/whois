package net.ripe.db.whois.common.grs;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.scheduler.DailyScheduler;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthoritativeResourceData {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceData.class);
    private final static int REFRESH_DELAY = 60 * 60 * 1000;

    private final ResourceDataDao resourceDataDao;
    private final DailyScheduler dailyScheduler;
    private long lastRefresh = Long.MIN_VALUE;

    private final Set<String> sourceNames;
    private final Map<String, AuthoritativeResource> authoritativeResourceCache = Maps.newHashMap();

    @Autowired
    public AuthoritativeResourceData(@Value("${grs.sources}") final List<String> grsSourceNames,
                                     final ResourceDataDao resourceDataDao,
                                     final DailyScheduler dailyScheduler) {
        this.resourceDataDao = resourceDataDao;
        this.dailyScheduler = dailyScheduler;
        this.sourceNames = Sets.newHashSet(Iterables.transform(grsSourceNames, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable String input) {
                return input.toLowerCase().replace("-grs", "");
            }
        }));
    }

    @PostConstruct
    void init() {
        refreshAuthoritativeResourceCache();
    }

    @Scheduled(fixedDelay = REFRESH_DELAY)
    synchronized public void refreshAuthoritativeResourceCache() {
        final long lastImportTime = dailyScheduler.getDailyTaskFinishTime(AuthoritativeResourceImportTask.class);
        if (lastImportTime > lastRefresh) {
            LOGGER.debug("Authoritative resource data import detected, finished at {} (previous run: {})", new LocalDateTime(lastImportTime), new LocalDateTime(lastRefresh));
            lastRefresh = lastImportTime;
            for (final String sourceName : sourceNames) {
                try {
                    LOGGER.debug("Refresh: {}", sourceName);
                    authoritativeResourceCache.put(sourceName, resourceDataDao.load(LOGGER, sourceName));
                } catch (RuntimeException e) {
                    LOGGER.error("Refreshing: {}", sourceName, e);
                }
            }
        }
    }

    public AuthoritativeResource getAuthoritativeResource(final CIString source) {
        final String sourceName = source.toLowerCase().replace("-grs", "");
        final AuthoritativeResource authoritativeResource = authoritativeResourceCache.get(sourceName);
        if (authoritativeResource == null) {
            throw new IllegalSourceException(source);
        }

        return authoritativeResource;
    }
}
