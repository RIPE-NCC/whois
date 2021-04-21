package net.ripe.db.whois.common.scheduler;

public interface DailyScheduledTask extends Runnable {
    String runTimezone = "Europe/Amsterdam";
    void run();
}
