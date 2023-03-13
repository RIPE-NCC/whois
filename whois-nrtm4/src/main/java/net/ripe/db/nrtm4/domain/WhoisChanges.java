package net.ripe.db.nrtm4.domain;

import java.util.List;


public record WhoisChanges(int serialIdFrom, int serialIdTo, List<RpslObjectData> list) {

}
