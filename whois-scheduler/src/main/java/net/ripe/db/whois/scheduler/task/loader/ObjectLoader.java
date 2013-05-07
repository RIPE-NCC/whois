package net.ripe.db.whois.scheduler.task.loader;

public interface ObjectLoader {
    void processObject(String fullObject, Result result, int pass);
}
