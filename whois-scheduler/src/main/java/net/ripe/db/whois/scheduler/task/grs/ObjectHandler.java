package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.rpsl.RpslObjectBase;

import java.util.List;

interface ObjectHandler {
    void handle(List<String> lines);

    void handle(RpslObjectBase rpslObjectBase);
}
