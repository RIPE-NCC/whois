package net.ripe.db.whois.scheduler.task.autnum;

import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.update.domain.LegacyAutnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LegacyAutnumReloadTask implements DailyScheduledTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(LegacyAutnumReloadTask.class);
    private final LegacyAutnum legacyAutnum;

    @Autowired
    public LegacyAutnumReloadTask(final LegacyAutnum legacyAutnum) {
        this.legacyAutnum = legacyAutnum;
    }

    @Override
    @Scheduled(cron = "0 1/15 * * * *")
    public void run() {
        LOGGER.debug("Reloading legacy autnums");
        final int previousTotal = legacyAutnum.getTotal();
        legacyAutnum.init();
        LOGGER.debug("Loaded {} legacy autnums (was {})", legacyAutnum.getTotal(), previousTotal);
    }

}
