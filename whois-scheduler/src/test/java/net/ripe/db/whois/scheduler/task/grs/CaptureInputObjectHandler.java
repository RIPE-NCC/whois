package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObjectBase;

import java.util.List;

class CaptureInputObjectHandler implements ObjectHandler {
    private List<List<String>> lines = Lists.newArrayList();
    private List<RpslObjectBase> objects = Lists.newArrayList();

    @Override
    public void handle(final List<String> lines) {
        this.lines.add(lines);
    }

    @Override
    public void handle(final RpslObjectBase rpslObjectBase) {
        this.objects.add(rpslObjectBase);
    }

    public List<List<String>> getLines() {
        return lines;
    }

    public List<RpslObjectBase> getObjects() {
        return objects;
    }
}
