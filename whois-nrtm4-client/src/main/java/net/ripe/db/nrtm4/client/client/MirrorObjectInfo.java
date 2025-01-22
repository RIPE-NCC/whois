package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.ripe.db.whois.common.rpsl.RpslObject;

public abstract class MirrorObjectInfo {

    @JsonDeserialize(using = RpslObjectDeserializer.class)
    private final RpslObject object;

    public MirrorObjectInfo(RpslObject object) {
        this.object = object;
    }

    public RpslObject getRpslObject() {
        return object;
    }
}
