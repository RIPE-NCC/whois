package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.dao.DailySchedulerDao;
import net.ripe.db.whois.common.dao.ResourceDataDao;
import net.ripe.db.whois.common.domain.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

import static net.ripe.db.whois.common.grs.AuthoritativeResourceImportTask.TASK_NAME;

@Component
public class AuthoritativeResourceRefreshTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceRefreshTask.class);

    private final static int REFRESH_DELAY_EVERY_HOUR = 60 * 60 * 1000;
    private final static int REFRESH_DELAY_EVERY_MINUTE = 60 * 1000;

    private final DailySchedulerDao dailySchedulerDao;
    private final AuthoritativeResourceData authoritativeResourceData;
    private final ResourceDataDao resourceDataDao;
    private final String source;

    private ResourceDataDao.State state = null;
    private LocalDateTime lastRefresh = null;

    @Autowired
    public AuthoritativeResourceRefreshTask(final DailySchedulerDao dailySchedulerDao,
                                            final AuthoritativeResourceData authoritativeResourceData,
                                            final ResourceDataDao resourceDataDao,
                                            @Value("${whois.source}") final String source) {
        this.dailySchedulerDao = dailySchedulerDao;
        this.authoritativeResourceData = authoritativeResourceData;
        this.resourceDataDao = resourceDataDao;
        this.source = source;
    }

    @Scheduled(fixedDelay = REFRESH_DELAY_EVERY_HOUR)
    synchronized public void refreshAuthoritativeResourceCache() {
        final LocalDateTime lastImportTime;
        try {
            final Optional<Timestamp> optional = dailySchedulerDao.getDailyTaskFinishTime(TASK_NAME);
            if (!optional.isPresent()) {
                return;
            }
            lastImportTime = optional.get().toLocalDateTime();
        } catch (RuntimeException e) {
            LOGGER.warn("Refreshing failed on get finish time due to {}: {}", e.getClass().getName(), e.getMessage());
            return;
        }

        if (lastRefresh == null || (lastImportTime.isAfter(lastRefresh))) {
            LOGGER.info("Authoritative resource data import detected, finished at {} (previous run: {})",
                    lastImportTime.toString(),
                    (lastRefresh != null) ? lastRefresh.toString() : "NONE");
            lastRefresh = lastImportTime;
            authoritativeResourceData.refreshAllSources();
            state = resourceDataDao.getState(source);
        }
    }

    @Scheduled(fixedDelay = REFRESH_DELAY_EVERY_MINUTE)
    synchronized public void refreshAuthoritativeResourceCacheOnChange() {
        final ResourceDataDao.State latestState;
        try {
            latestState = resourceDataDao.getState(source);
        } catch (RuntimeException e) {
            LOGGER.warn("Refreshing failed on get state due to {}: {}", e.getClass().getName(), e.getMessage());
            return;
        }

        if ((state == null) || latestState.compareTo(state) != 0) {
            this.state = latestState;
            authoritativeResourceData.refreshActiveSource();
        }
    }

}
