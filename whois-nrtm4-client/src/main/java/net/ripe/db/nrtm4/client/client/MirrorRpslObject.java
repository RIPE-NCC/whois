package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class MirrorRpslObject {

    @JsonDeserialize(using = RpslObjectDeserializer.class)
    private final RpslObject object;

    private MirrorRpslObject() {
        object = null;
    }

    public MirrorRpslObject(final RpslObject rpslObject) {
        this.object = rpslObject;
    }

    public RpslObject getObject() {
        return object;
    }
}
