package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class MirrorObjectInfo {

    @JsonDeserialize(using = RpslObjectDeserializer.class)
    private final RpslObject object;

    public MirrorObjectInfo() {
        this.object = null;
    }

    public MirrorObjectInfo(final RpslObject rpslObject) {
        this.object = rpslObject;
    }

    public RpslObject getObject() {
        return object;
    }
}
