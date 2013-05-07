package net.ripe.db.whois.scheduler.task.export.dao;

public interface ExportDao {
    int getMaxSerial();

    void exportObjects(ExportCallbackHandler exportCallbackHandler);
}
