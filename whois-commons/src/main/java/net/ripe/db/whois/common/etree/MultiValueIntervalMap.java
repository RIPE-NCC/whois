package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.domain.ip.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public final class MultiValueIntervalMap<K extends Interval<K>, V> implements IntervalMap<K, V> {
    private final IntervalMap<K, SortedSet<V>> wrapped;

    public MultiValueIntervalMap() {
        this.wrapped = new NestedIntervalMap<>();
    }

    @Override
    public void put(K key, V value) {
        SortedSet<V> set = CollectionHelper.uniqueResult(wrapped.findExact(key));
        if (set == null) {
            set = new TreeSet<>();
            wrapped.put(key, set);
        }

        set.add(value);
    }

    @Override
    public void remove(K key) {
        wrapped.remove(key);
    }

    @Override
    public void remove(K key, V value) {
        SortedSet<V> set = CollectionHelper.uniqueResult(wrapped.findExact(key));
        if (set == null) {
            return;
        }

        set.remove(value);

        if (set.isEmpty()) {
            wrapped.remove(key);
        }
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    private static <V> List<V> unroll(final List<SortedSet<V>> sets) {
        int size = 0;
        for (final SortedSet<V> set : sets) {
            size += set.size();
        }

        final List<V> result = new ArrayList<>(size);
        for (final SortedSet<V> set : sets) {
            result.addAll(set);
        }

        return result;
    }

    @Override
    public List<V> findFirstLessSpecific(K key) {
        return unroll(wrapped.findFirstLessSpecific(key));
    }

    @Override
    public List<V> findExact(K key) {
        return unroll(wrapped.findExact(key));
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(K key) {
        return unroll(wrapped.findExactOrFirstLessSpecific(key));
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        return unroll(wrapped.findAllLessSpecific(key));
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        return unroll(wrapped.findExactAndAllLessSpecific(key));
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        return unroll(wrapped.findFirstMoreSpecific(key));
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        return unroll(wrapped.findAllMoreSpecific(key));
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        return unroll(wrapped.findExactAndAllMoreSpecific(key));
    }
}
