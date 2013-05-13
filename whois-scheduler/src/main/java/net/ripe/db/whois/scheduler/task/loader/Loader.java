package net.ripe.db.whois.scheduler.task.loader;

public interface Loader {
    String loadSplitFiles(String... entries);
    void resetDatabase();
}
