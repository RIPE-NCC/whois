package net.ripe.db.whois.scheduler.task.acl;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import net.ripe.db.whois.query.acl.PersonalObjectAccounting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResetPersonalObjectAccounting implements DailyScheduledTask {
    private final PersonalObjectAccounting personalObjectAccounting;

    @Autowired
    public ResetPersonalObjectAccounting(final PersonalObjectAccounting personalObjectAccounting) {
        this.personalObjectAccounting = personalObjectAccounting;
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "ResetPersonalObjectAccounting")
    public void run() {
        personalObjectAccounting.resetAccounting();
    }
}
