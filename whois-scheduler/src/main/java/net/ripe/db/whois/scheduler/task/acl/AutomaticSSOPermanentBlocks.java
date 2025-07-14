package net.ripe.db.whois.scheduler.task.acl;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.SSOResourceConfiguration;
import net.ripe.db.whois.query.dao.IpAccessControlListDao;
import net.ripe.db.whois.query.dao.SSOAccessControlListDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;

@Component
public class AutomaticSSOPermanentBlocks implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticSSOPermanentBlocks.class);

    private final DateTimeProvider dateTimeProvider;
    private final SSOAccessControlListDao ssoAccessControlListDao;
    private final SSOResourceConfiguration ssoResourceConfiguration;

    @Autowired
    public AutomaticSSOPermanentBlocks(final DateTimeProvider dateTimeProvider,
                                       final SSOAccessControlListDao ssoAccessControlListDao,
                                       final SSOResourceConfiguration ssoResourceConfiguration) {
        this.dateTimeProvider = dateTimeProvider;
        this.ssoAccessControlListDao = ssoAccessControlListDao;
        this.ssoResourceConfiguration = ssoResourceConfiguration;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "AutomaticPermanentBlocks")
    public void run() {
        final LocalDate now = dateTimeProvider.getCurrentDate();
        final LocalDate checkTemporaryBlockTime = now.minusDays(30);
        final List<BlockEvents> temporaryBlocks = ssoAccessControlListDao.getTemporaryBlocks(checkTemporaryBlockTime);
        for (final BlockEvents blockEvents : temporaryBlocks) {
            handleBlockEvents(now, blockEvents);
        }
    }

    private void handleBlockEvents(final LocalDate now, final BlockEvents blockEvents) {
        LOGGER.debug("Reload IP resource configuration to make sure we have the latest ACL");
        ssoResourceConfiguration.reload();

        final String prefix = blockEvents.getPrefix();
        if (blockEvents.isPermanentBlockRequired()) {
            try {
                if (ssoResourceConfiguration.isDenied(prefix)) {
                    LOGGER.debug("Permanent ban already created for prefix: {}", prefix);
                } else {
                    final String comment = String.format("Automatic permanent ban after %s temporary blocks at %s",
                            blockEvents.getTemporaryBlockCount(),
                            FormatHelper.dateToString(now));

                    ssoAccessControlListDao.savePermanentBlock(prefix, now, ssoResourceConfiguration.getLimit(), comment);
                    LOGGER.debug("Permanent ban created for prefix: {}", prefix);
                }
            } catch (Exception e) {
                LOGGER.error("Creating permanent ban for prefix: {}", prefix, e);
            }
        }
    }
}
