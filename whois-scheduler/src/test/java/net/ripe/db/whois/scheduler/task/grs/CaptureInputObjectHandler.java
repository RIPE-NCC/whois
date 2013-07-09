package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

class CaptureInputObjectHandler implements ObjectHandler {
    private List<List<String>> lines = Lists.newArrayList();
    private List<RpslObject> objects = Lists.newArrayList();

    @Override
    public void handle(final List<String> lines) {
        this.lines.add(lines);
    }

    @Override
    public void handle(final RpslObject rpslObjectBase) {
        this.objects.add(rpslObjectBase);
    }

    public List<List<String>> getLines() {
        return lines;
    }

    public List<RpslObject> getObjects() {
        return objects;
    }
}
