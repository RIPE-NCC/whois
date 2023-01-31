package net.ripe.db.nrtm4.domain;

import java.util.List;


public record InitialSnapshotState(int serialId, List<RpslObjectData> rpslObjectData) {

}
