package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.common.domain.BlockEvents;
import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.dao.AccessControlListDao;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.List;

@Component
public class AutomaticPermanentBlocks implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticPermanentBlocks.class);

    private final DateTimeProvider dateTimeProvider;
    private final AccessControlListDao accessControlListDao;
    private final IpResourceConfiguration ipResourceConfiguration;

    @Autowired
    public AutomaticPermanentBlocks(final DateTimeProvider dateTimeProvider, final AccessControlListDao accessControlListDao, final IpResourceConfiguration ipResourceConfiguration) {
        this.dateTimeProvider = dateTimeProvider;
        this.accessControlListDao = accessControlListDao;
        this.ipResourceConfiguration = ipResourceConfiguration;
    }

    @Override
    public void run() {
        final LocalDate now = dateTimeProvider.getCurrentDate();
        final LocalDate checkTemporaryBlockTime = now.minusDays(30);
        final List<BlockEvents> temporaryBlocks = accessControlListDao.getTemporaryBlocks(checkTemporaryBlockTime);
        for (final BlockEvents blockEvents : temporaryBlocks) {
            handleBlockEvents(now, blockEvents);
        }
    }

    private void handleBlockEvents(final LocalDate now, final BlockEvents blockEvents) {
        LOGGER.debug("Reload IP resource configuration to make sure we have the latest ACL");
        ipResourceConfiguration.reload();

        final String prefix = blockEvents.getPrefix();
        if (blockEvents.isPermanentBlockRequired()) {
            try {
                final InetAddress remoteAddress = IpInterval.parse(prefix).beginAsInetAddress();
                if (ipResourceConfiguration.isDenied(remoteAddress)) {
                    LOGGER.debug("Permanent ban already created for prefix: {}", prefix);
                } else {
                    final String comment = String.format("Automatic permanent ban after %s temporary blocks at %s",
                            blockEvents.getTemporaryBlockCount(),
                            FormatHelper.dateToString(now));

                    accessControlListDao.savePermanentBlock(prefix, now, ipResourceConfiguration.getLimit(remoteAddress), comment);
                    LOGGER.debug("Permanent ban created for prefix: {}", prefix);
                }
            } catch (Exception e) {
                LOGGER.error("Creating permanent ban for prefix: {}", prefix, e);
            }
        }
    }
}
