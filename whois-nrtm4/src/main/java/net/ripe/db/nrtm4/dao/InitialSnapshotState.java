package net.ripe.db.nrtm4.dao;

import java.util.List;


public record InitialSnapshotState(int serialId, List<RpslObjectData> rpslObjectData) {

}
