package net.ripe.db.nrtm4.dao;

import java.util.List;


public record DeltaChanges(int serialIdFrom, int serialIdTo, List<ObjectChangeData> list) {

}
