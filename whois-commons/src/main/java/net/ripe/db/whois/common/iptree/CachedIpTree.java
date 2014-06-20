package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.source.SourceContext;

import java.util.List;

public abstract class CachedIpTree<K extends IpInterval<K>, V extends IpEntry<K>> implements IpTree<K, V> {
    private final IpTreeCacheManager ipTreeCacheManager;
    private final SourceContext sourceContext;

    protected CachedIpTree(final IpTreeCacheManager ipTreeCacheManager, final SourceContext sourceContext) {
        this.ipTreeCacheManager = ipTreeCacheManager;
        this.sourceContext = sourceContext;
    }

    abstract IntervalMap<K, V> getIntervalMap(final IpTreeCacheManager.NestedIntervalMaps nestedIntervalMaps);

    private IpTreeCacheManager.NestedIntervalMaps getNestedIntervalMaps() {
        return ipTreeCacheManager.get(sourceContext.getCurrentSourceConfiguration().getSource().getName());
    }

    private IntervalMap<K, V> getIntervalMap() {
        return getIntervalMap(getNestedIntervalMaps());
    }

    @Override
    public List<V> findAllLessSpecific(final K key) {
        return getIntervalMap().findAllLessSpecific(key);
    }

    @Override
    public List<V> findFirstLessSpecific(final K key) {
        return getIntervalMap().findFirstLessSpecific(key);
    }

    @Override
    public List<V> findExact(final K key) {
        return getIntervalMap().findExact(key);
    }

    @Override
    public List<V> findExactAndAllLessSpecific(final K key) {
        return getIntervalMap().findExactAndAllLessSpecific(key);
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(final K key) {
        return getIntervalMap().findExactOrFirstLessSpecific(key);
    }

    @Override
    public List<V> findFirstMoreSpecific(final K key) {
        return getIntervalMap().findFirstMoreSpecific(key);
    }

    @Override
    public List<V> findAllMoreSpecific(final K key) {
        return getIntervalMap().findAllMoreSpecific(key);
    }
}
