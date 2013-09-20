package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.query.acl.PersonalObjectAccounting;
import net.ripe.db.whois.common.scheduler.DailyScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResetPersonalObjectAccounting implements DailyScheduledTask {
    private final PersonalObjectAccounting personalObjectAccounting;

    @Autowired
    public ResetPersonalObjectAccounting(final PersonalObjectAccounting personalObjectAccounting) {
        this.personalObjectAccounting = personalObjectAccounting;
    }

    @Override
    public void run() {
        personalObjectAccounting.resetAccounting();
    }
}
