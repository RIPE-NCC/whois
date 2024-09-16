package net.ripe.db.whois.scheduler.task.acl;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AutomaticSSOPermanentBlocksCleanup implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticSSOPermanentBlocksCleanup.class);

    private final DateTimeProvider dateTimeProvider;
    private final SSOAccessControlListDao ssoAccessControlListDao;

    @Autowired
    public AutomaticSSOPermanentBlocksCleanup(final DateTimeProvider dateTimeProvider, final SSOAccessControlListDao ssoAccessControlListDao) {
        this.dateTimeProvider = dateTimeProvider;
        this.ssoAccessControlListDao = ssoAccessControlListDao;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "AutomaticPermanentBlocksCleanup")
    public void run() {
        final LocalDate eventRemoveDate = dateTimeProvider.getCurrentDate().minusMonths(3);
        LOGGER.debug("Removing block events before {}", eventRemoveDate);
        ssoAccessControlListDao.removeBlockEventsBefore(eventRemoveDate);

        final LocalDate blockRemoveDate = dateTimeProvider.getCurrentDate().minusYears(1);
        LOGGER.debug("Removing permanent bans before {}", blockRemoveDate);
        ssoAccessControlListDao.removePermanentBlocksBefore(blockRemoveDate);
    }
}
