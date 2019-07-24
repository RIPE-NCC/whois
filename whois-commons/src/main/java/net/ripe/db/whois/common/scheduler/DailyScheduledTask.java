package net.ripe.db.whois.common.scheduler;

public interface DailyScheduledTask extends Runnable {
    void run();
}
