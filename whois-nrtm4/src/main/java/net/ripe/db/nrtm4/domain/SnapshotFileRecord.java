package net.ripe.db.nrtm4.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.whois.common.rpsl.RpslObject;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"object"})
public class SnapshotFileRecord implements NrtmFileRecord {

    @JsonSerialize(using = RpslObjectJsonSupport.Serializer.class)
    @JsonDeserialize(using = RpslObjectJsonSupport.Deserializer.class)
    private final RpslObject object;

    private SnapshotFileRecord() {
        object = null;
    }

    public SnapshotFileRecord(final RpslObject rpslObject) {
        this.object = rpslObject;
    }

    public RpslObject getObject() {
        return object;
    }

}
