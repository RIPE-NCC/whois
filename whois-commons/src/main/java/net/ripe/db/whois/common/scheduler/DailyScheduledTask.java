package net.ripe.db.whois.common.scheduler;

public interface DailyScheduledTask extends Runnable {
    String EUROPE_AMSTERDAM = "Europe/Amsterdam";
    void run();
}
