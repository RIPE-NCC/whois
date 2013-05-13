package net.ripe.db.whois.scheduler.task.export.dao;

import net.ripe.db.whois.common.rpsl.RpslObject;

public interface ExportCallbackHandler {
    void exportObject(RpslObject object);
}
