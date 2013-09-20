package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AutomaticPermanentBlocksCleanup implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticPermanentBlocksCleanup.class);

    private final DateTimeProvider dateTimeProvider;
    private final AccessControlListDao accessControlListDao;

    @Autowired
    public AutomaticPermanentBlocksCleanup(final DateTimeProvider dateTimeProvider, final AccessControlListDao accessControlListDao) {
        this.dateTimeProvider = dateTimeProvider;
        this.accessControlListDao = accessControlListDao;
    }

    @Override
    public void run() {
        final LocalDate eventRemoveDate = dateTimeProvider.getCurrentDate().minusMonths(3);
        LOGGER.debug("Removing block events before {}", eventRemoveDate);
        accessControlListDao.removeBlockEventsBefore(eventRemoveDate);

        final LocalDate blockRemoveDate = dateTimeProvider.getCurrentDate().minusYears(1);
        LOGGER.debug("Removing permanent bans before {}", blockRemoveDate);
        accessControlListDao.removePermanentBlocksBefore(blockRemoveDate);
    }
}
