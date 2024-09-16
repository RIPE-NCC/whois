package net.ripe.db.nrtm4.domain;

import java.util.List;


public record SnapshotState(int serialId, List<WhoisObjectData> whoisObjectData) {

}
