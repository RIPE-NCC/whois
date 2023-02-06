package net.ripe.db.nrtm4.domain;

import net.ripe.db.nrtm4.domain.RpslObjectData;

import java.util.List;


public record InitialSnapshotState(int serialId, List<RpslObjectData> rpslObjectData) {

}
