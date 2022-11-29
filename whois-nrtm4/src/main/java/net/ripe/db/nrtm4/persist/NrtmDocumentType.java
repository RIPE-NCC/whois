package net.ripe.db.nrtm4.persist;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.ripe.db.nrtm4.EnumToStringSerializer;


@JsonSerialize(using = EnumToStringSerializer.class)
public enum NrtmDocumentType {
    DELTA,
    SNAPSHOT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
