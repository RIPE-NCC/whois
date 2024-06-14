package net.ripe.db.nrtm4.scheduler;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.dao.UpdateNrtmFileRepository;
import net.ripe.db.nrtm4.domain.NrtmKeyRecord;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.mariadb.jdbc.internal.logging.Logger;
import org.mariadb.jdbc.internal.logging.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class NrtmKeyRolloverScheduledTask implements DailyScheduledTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmKeyRolloverScheduledTask.class);
    private final NrtmKeyConfigDao nrtmKeyConfigDao;
    private final NrtmKeyPairService nrtmKeyPairService;
    private final UpdateNrtmFileRepository updateNrtmFileRepository;
    private final DateTimeProvider dateTimeProvider;

    NrtmKeyRolloverScheduledTask(final NrtmKeyPairService nrtmKeyPairService, final NrtmKeyConfigDao nrtmKeyConfigDao, final UpdateNrtmFileRepository updateNrtmFileRepository, final DateTimeProvider dateTimeProvider) {
        this.nrtmKeyPairService = nrtmKeyPairService;
        this.nrtmKeyConfigDao = nrtmKeyConfigDao;
        this.updateNrtmFileRepository = updateNrtmFileRepository;
        this.dateTimeProvider = dateTimeProvider;
    }


    //TODO: check when to run and how frequently to run this job
    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "NrtmKeyRotateScheduledTask")
    public void run() {
        try {
            if(!nrtmKeyConfigDao.isActiveKeyPairExists()) {
                LOGGER.warn("Seems like nrtmv4 is not initialized");
                return;
            }

            final NrtmKeyRecord currentActiveKey = nrtmKeyConfigDao.getActiveKeyPair();
            final LocalDateTime currentDateTime = dateTimeProvider.getCurrentDateTime();

            if(currentActiveKey.expires() > currentDateTime.plusDays(7).toEpochSecond(ZoneOffset.UTC)) {
                return;
            }

            final NrtmKeyRecord nextKey = nrtmKeyPairService.getNextkeyPairRecord();
            if(nextKey == null) {
              nrtmKeyPairService.generateKeyRecord(false);
              return;
            }

            if(currentActiveKey.expires() <=  currentDateTime.toEpochSecond(ZoneOffset.UTC)) {
              //Needs to happen in a transaction
              updateNrtmFileRepository.rotateKey(nextKey, currentActiveKey);
            }

        } catch (final Exception e) {
            LOGGER.error("NRTMv4 key rotation job failed", e);
            throw new RuntimeException(e);
        }
    }
}
