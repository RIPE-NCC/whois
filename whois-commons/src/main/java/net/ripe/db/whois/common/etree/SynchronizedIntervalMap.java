package net.ripe.db.whois.common.etree;

import java.util.List;

public final class SynchronizedIntervalMap<K extends Interval<K>, V> implements IntervalMap<K, V> {

    private final Object mutex;
    private final IntervalMap<K, V> wrapped;

    public static <K extends Interval<K>, V> IntervalMap<K, V> synchronizedMap(IntervalMap<K, V> toWrap) {
        return new SynchronizedIntervalMap<K, V>(toWrap);
    }

    public static <K extends Interval<K>, V> IntervalMap<K, V> synchronizedMap(IntervalMap<K, V> toWrap, final Object mutex) {
        return new SynchronizedIntervalMap<K, V>(toWrap, mutex);
    }

    private SynchronizedIntervalMap(final IntervalMap<K, V> wrapped) {
        this.wrapped = wrapped;
        this.mutex = this;
    }

    private SynchronizedIntervalMap(final IntervalMap<K, V> wrapped, final Object mutex) {
        this.wrapped = wrapped;
        this.mutex = mutex;
    }

    @Override
    public void put(K key, V value) {
        synchronized (mutex) {
            wrapped.put(key, value);
        }
    }

    @Override
    public void remove(K key) {
        synchronized (mutex) {
            wrapped.remove(key);
        }
    }

    @Override
    public void remove(K key, V value) {
        synchronized (mutex) {
            wrapped.remove(key, value);
        }
    }

    @Override
    public List<V> findFirstLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findFirstLessSpecific(key);
        }
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findAllLessSpecific(key);
        }
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactAndAllLessSpecific(key);
        }
    }

    @Override
    public List<V> findExact(K key) {
        synchronized (mutex) {
            return wrapped.findExact(key);
        }
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactOrFirstLessSpecific(key);
        }
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findFirstMoreSpecific(key);
        }
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findAllMoreSpecific(key);
        }
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        synchronized (mutex) {
            return wrapped.findExactAndAllMoreSpecific(key);
        }
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            wrapped.clear();
        }
    }
}
