package net.ripe.db.whois.common.grs;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.source.IllegalSourceException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

@Component
public class AuthoritativeResourceData {
    private final static Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceData.class);
    private static final Splitter PROPERTY_LIST_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    private final static int REFRESH_DELAY_EVERY_HOUR = 60 * 60 * 1000;
    private final static int REFRESH_DELAY_EVERY_MINUTE = 60 * 1000;

    private final ResourceDataDao resourceDataDao;
    private final DailySchedulerDao dailySchedulerDao;
    private final DateTimeProvider dateTimeProvider;
    private long lastRefresh = Integer.MIN_VALUE;
    private ResourceDataDao.State state = null;

    private final Set<String> sourceNames;
    private final String source;
    private final Map<String, AuthoritativeResource> authoritativeResourceCache = Maps.newHashMap();

    @Autowired
    public AuthoritativeResourceData(@Value("${grs.sources}") final String grsSourceNames,
                                     @Value("${whois.source}") final String source,
                                     final ResourceDataDao resourceDataDao,
                                     final DailySchedulerDao dailySchedulerDao, DateTimeProvider dateTimeProvider) {
        this.resourceDataDao = resourceDataDao;
        this.dailySchedulerDao = dailySchedulerDao;
        this.dateTimeProvider = dateTimeProvider;
        this.source = source.toLowerCase();
        this.sourceNames = Sets.newHashSet(Iterables.transform(PROPERTY_LIST_SPLITTER.split(grsSourceNames), new Function<String, String>() {
            @Override
            public String apply(final String input) {
                return input.toLowerCase().replace("-grs", "");
            }
        }));
    }

    @PostConstruct
    void init() {
        refreshAuthoritativeResourceCache();
    }

    @Scheduled(fixedDelay = REFRESH_DELAY_EVERY_HOUR)
    synchronized public void refreshAuthoritativeResourceCache() {
        final LocalDate date = dateTimeProvider.getCurrentDate();
        final long lastImportTime = dailySchedulerDao.getDailyTaskFinishTime(date, AuthoritativeResourceImportTask.class);
        if (lastImportTime > lastRefresh) {
            LOGGER.info("Authoritative resource data import detected, finished at {} (previous run: {})", new LocalDateTime(lastImportTime), new LocalDateTime(lastRefresh));
            lastRefresh = lastImportTime;
            for (final String sourceName : sourceNames) {
                try {
                    LOGGER.debug("Refresh: {}", sourceName);
                    state = resourceDataDao.getState(source);
                    authoritativeResourceCache.put(sourceName, resourceDataDao.load(sourceName));
                } catch (RuntimeException e) {
                    LOGGER.error("Refreshing: {}", sourceName, e);
                }
            }
        }
    }

    @Scheduled(fixedDelay = REFRESH_DELAY_EVERY_MINUTE)
    synchronized public void refreshAuthoritativeResourceCacheOnChange() {
        final ResourceDataDao.State latestState = resourceDataDao.getState(source);

        if ((state == null) || latestState.compareTo(state) != 0) {
            this.state = latestState;
            try {
                LOGGER.debug("Refresh: {}", source);
                authoritativeResourceCache.put(source, resourceDataDao.load(source));
            } catch (RuntimeException e) {
                LOGGER.error("Refreshing: {}", source, e);
            }
        }
    }

    public AuthoritativeResource getAuthoritativeResource(final CIString source) {
        final String sourceName = StringUtils.removeEnd(source.toLowerCase(), "-grs");
        final AuthoritativeResource authoritativeResource = authoritativeResourceCache.get(sourceName);
        if (authoritativeResource == null) {
            throw new IllegalSourceException(source);
        }

        return authoritativeResource;
    }
}
