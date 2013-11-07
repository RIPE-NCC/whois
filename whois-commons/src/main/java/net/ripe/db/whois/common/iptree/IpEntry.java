package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.ip.Interval;
import net.ripe.db.whois.common.etree.NestedIntervalMap;

public class IpEntry<K extends Interval<K>> extends NestedIntervalMap.Key<K> implements Identifiable {
    private final int objectId;

    protected IpEntry(final K key, final int objectId) {
        super(key);
        this.objectId = objectId;
    }

    @Override
    public int getObjectId() {
        return objectId;
    }
}
