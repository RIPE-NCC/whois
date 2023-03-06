package net.ripe.db.nrtm4.domain;

import net.ripe.db.nrtm4.domain.RpslObjectData;

import java.util.List;


public record DeltaChanges(int serialIdFrom, int serialIdTo, List<RpslObjectData> list) {

}
