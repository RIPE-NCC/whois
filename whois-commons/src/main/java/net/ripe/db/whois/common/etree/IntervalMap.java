package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.Interval;

import java.util.List;

/**
 * A map with intervals as keys. Intervals are only allowed to intersect if they
 * are fully contained in the other interval (in other words, siblings are not
 * allowed to intersect, but nesting is ok).
 *
 * @param <K> the type of the interval (must implement {@link Interval}).
 * @param <V> the type of the values to store.
 */
public interface IntervalMap<K extends Interval<?>, V> {

    /**
     * Associates the specified value with the specified key in this map If the
     * map previously contained a mapping for the key, the old value is replaced
     * by the specified value.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws IllegalArgumentException     if the key or value is <code>null</code>
     * @throws IntersectingIntervalException if the key intersects (but is not contained within) an existing
     *                                      key
     */
    void put(K key, V value);

    /**
     * Removes the mapping for a key from this map if it is present.
     * <p/>
     * <p/>
     * The map will not contain a mapping for the specified key once the call
     * returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @throws IllegalArgumentException if the specified key is null
     */
    void remove(K key);

    /**
     * Removes the mapping for a key and value from this map if both are present.
     * <p/>
     * <p/>
     * The map will not contain a mapping for the specified key with the specified value
     * once the call returns.
     *
     * @param key   key whose mapping is to be removed from the map
     * @param value value with the key to be removed from the map
     *
     * @throws IllegalArgumentException if the specified key or value is null
     */
    void remove(K key, V value);

    void clear();

    /**
     * Finds the value associated with closest interval that contains
     * <code>key</code> but is not equal to <code>key</code>.
     *
     * @param key the key to find the closest enclosing interval for
     * @return the value associated with the closest enclosing interval of
     *         <code>key</code>, or an empty list if no such mapping exists.
     */
    List<V> findFirstLessSpecific(K key);

    /**
     * Finds the value associated with <code>key</code>, if it exists.
     *
     * @param key the key to find the mapping for
     * @return the value associated with <code>key</code> or an empty list if no
     *         such value exists
     */
    List<V> findExact(K key);

    /**
     * Finds the value associated with <code>key</code>, or its closest
     * enclosing if <code>key</code> is not contained in this map, if it exists.
     *
     * @param key the key to find the mapping for
     * @return the value associated with <code>key</code> or its closest
     *         containing interval, or an empty list if no such value exists
     */
    List<V> findExactOrFirstLessSpecific(K key);

    /**
     * Finds all values that are associated to intervals that contain
     * <code>key</code> but are not equal to <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find all containing intervals for
     * @return the (possibly empty) list of values that are associated to
     *         intervals that contain <code>key</code> but is not equal to
     *         <code>key</code>.
     */
    List<V> findAllLessSpecific(K key);

    /**
     * Finds all values that are associated to intervals that contain
     * <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval. So if a mapping for <code>key</code> exists the last
     * element of the returned list will contain the value associated with
     * <code>key</code>.
     *
     * @param key the key to find all containing intervals for
     * @return the (possibly empty) list of values that are associated to
     *         intervals that contain <code>key</code>
     */
    List<V> findExactAndAllLessSpecific(K key);

    /**
     * Finds all values associated with intervals that are more specific
     * (contained in) <code>key</code>, but excluding the values that are nested
     * inside the matching intervals.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find the first level more specific values for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findFirstMoreSpecific(K key);

    /**
     * Finds all values associated with intervals that are contained within
     * (more specific than) <code>key</code>, but not equal to <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval.
     *
     * @param key the key to find all levels more specific values for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findAllMoreSpecific(K key);

    /**
     * Finds all values associated with intervals that are equal to
     * <code>key</code> or contained within (more specific than)
     * <code>key</code>.
     * <p/>
     * <p/>
     * The resulting values are ordered from least specific interval to most
     * specific interval. So if a mapping for <code>key</code> exists the first
     * element of the returned list will contain the value associated with
     * <code>key</code>.
     *
     * @param key the key to find the exact and all levels more specific values
     *            for
     * @return the (possibly empty) list of values associated with the matching
     *         intervals.
     */
    List<V> findExactAndAllMoreSpecific(K key);
}
