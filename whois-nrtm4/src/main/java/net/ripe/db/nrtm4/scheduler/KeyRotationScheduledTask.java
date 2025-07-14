package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.Nrtmv4Condition;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.nrtm4.generator.UpdateNotificationFileGenerator;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(Nrtmv4Condition.class)
public class KeyRotationScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyRotationScheduledTask.class);
    private final NrtmKeyPairService nrtmKeyPairService;

    KeyRotationScheduledTask(final NrtmKeyPairService nrtmKeyPairService) {
        this.nrtmKeyPairService = nrtmKeyPairService;
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "NrtmKeyRotationTask")
    public void run() {
        try {
            nrtmKeyPairService.generateOrRotateNextKey();
        } catch (final Exception e) {
            LOGGER.error("NRTM key rotation job failed", e);
            throw new RuntimeException(e);
        }
    }

}
