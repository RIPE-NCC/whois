package net.ripe.db.whois.common.scheduler;

public interface DailyScheduledTask extends Runnable {
    String RUN_TIMEZONE = "Europe/Amsterdam";
    void run();
}
