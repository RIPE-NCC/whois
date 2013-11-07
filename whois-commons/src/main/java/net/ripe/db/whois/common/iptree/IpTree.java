package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.etree.NestedIntervalMap;

import java.util.List;

public interface IpTree<K extends IpInterval<K>, V extends NestedIntervalMap.Key<K>> {
    List<V> findAllLessSpecific(K key);

    List<V> findFirstLessSpecific(K key);

    List<V> findExact(K key);

    List<V> findExactAndAllLessSpecific(K key);

    List<V> findExactOrFirstLessSpecific(K key);

    List<V> findFirstMoreSpecific(K key);

    List<V> findAllMoreSpecific(K key);
}
