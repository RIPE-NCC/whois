package net.ripe.db.whois.scheduler.task.update;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PendingUpdatesCleanup implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUpdatesCleanup.class);

    private static final int CLEANUP_THRESHOLD_DAYS = 7;

    private final PendingUpdateDao pendingUpdateDao;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public PendingUpdatesCleanup(final PendingUpdateDao pendingUpdateDao,
                                 final DateTimeProvider dateTimeProvider) {
        this.pendingUpdateDao = pendingUpdateDao;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Override
    public void run() {
        final LocalDateTime before = dateTimeProvider.getCurrentDateTime().minusDays(CLEANUP_THRESHOLD_DAYS);
        LOGGER.debug("Removing pending updates before {}", before);
        pendingUpdateDao.removePendingUpdatesBefore(before);
    }
}
