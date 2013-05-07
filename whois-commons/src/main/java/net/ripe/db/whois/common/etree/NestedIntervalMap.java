package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.collect.CollectionHelper;
import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * A map with intervals as keys. Intervals are only allowed to intersect if they
 * are fully contained in the other interval (in other words, siblings are not
 * allowed to intersect, but nesting is ok).
 * <p/>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally. (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not a
 * structural modification.) This is typically accomplished by synchronizing on
 * some object that naturally encapsulates the map.
 *
 * @param <K> the type of the interval (must implement {@link Interval}).
 * @param <V> the type of the values to store.
 */
public final class NestedIntervalMap<K extends Interval<K>, V> implements IntervalMap<K, V> {
    private final ChildNodeMap<K, V> children;

    /**
     * Construct an empty {@link NestedIntervalMap}.
     */
    public NestedIntervalMap() {
        this.children = new ChildNodeTreeMap<K, V>();
    }

    /**
     * Construct a new {@link NestedIntervalMap} with (key, values) of
     * <code>source</code> copied.
     *
     * @param source the source to copy.
     */
    public NestedIntervalMap(NestedIntervalMap<K, V> source) {
        this.children = new ChildNodeTreeMap<K, V>(source.children);
    }

    @Override
    public void put(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);
        children.addChild(new InternalNode<K, V>(key, value));
    }

    @Override
    public void remove(K key) {
        Validate.notNull(key);
        children.removeChild(key);
    }

    @Override
    public void remove(K key, V value) {
        Validate.notNull(key);
        Validate.notNull(value);

        if (value.equals(CollectionHelper.uniqueResult(findExact(key)))) {
            remove(key);
        }
    }

    @Override
    public List<V> findFirstLessSpecific(K key) {
        Validate.notNull(key);
        InternalNode<K, V> node = internalFindFirstLessSpecific(key);
        return mapToValues(node);
    }

    @Override
    public List<V> findAllLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindAllLessSpecific(key));
    }

    @Override
    public List<V> findExactAndAllLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindExactAndAllLessSpecific(key));
    }

    @Override
    public List<V> findExact(K key) {
        Validate.notNull(key);
        InternalNode<K, V> node = internalFindExact(key);
        return mapToValues(node);
    }

    @Override
    public List<V> findExactOrFirstLessSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindExactOrFirstLessSpecific(key));
    }

    @Override
    public List<V> findFirstMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindFirstMoreSpecific(key));
    }

    @Override
    public List<V> findAllMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindAllMoreSpecific(key));
    }

    @Override
    public List<V> findExactAndAllMoreSpecific(K key) {
        Validate.notNull(key);
        return mapToValues(internalFindExactAndAllMoreSpecific(key));
    }

    /**
     * Clears all values from the map.
     */
    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass() && this.children.equals(((NestedIntervalMap<?, ?>) obj).children);

    }

    @Override
    public int hashCode() {
        return children.hashCode();
    }

    @Override
    public String toString() {
        return children.toString();
    }

    private List<V> mapToValues(InternalNode<K, V> node) {
        if (node == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(node.getValue());
    }

    private List<V> mapToValues(Collection<InternalNode<K, V>> nodes) {
        List<V> result = new ArrayList<V>(nodes.size());
        for (InternalNode<K, V> node : nodes) {
            result.add(node.getValue());
        }
        return result;
    }

    private InternalNode<K, V> internalFindExactOrFirstLessSpecific(K range) {
        List<InternalNode<K, V>> list = internalFindExactAndAllLessSpecific(range);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    private InternalNode<K, V> internalFindFirstLessSpecific(K range) {
        List<InternalNode<K, V>> list = internalFindAllLessSpecific(range);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(list.size() - 1);
        }
    }

    private List<InternalNode<K, V>> internalFindAllLessSpecific(K range) {
        List<InternalNode<K, V>> result = internalFindExactAndAllLessSpecific(range);
        if (result.isEmpty()) {
            return result;
        }
        InternalNode<K, V> last = result.get(result.size() - 1);
        if (last.getInterval().equals(range)) {
            return result.subList(0, result.size() - 1);
        } else {
            return result;
        }
    }

    private List<InternalNode<K, V>> internalFindExactAndAllLessSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        children.findExactAndAllLessSpecific(result, range);
        return result;
    }

    private InternalNode<K, V> internalFindExact(K range) {
        List<InternalNode<K, V>> exactAndAllLessSpecific = internalFindExactAndAllLessSpecific(range);
        if (exactAndAllLessSpecific.isEmpty()) {
            return null;
        }
        InternalNode<K, V> last = exactAndAllLessSpecific.get(exactAndAllLessSpecific.size() - 1);
        if (last.getInterval().equals(range)) {
            return last;
        }
        return null;
    }

    private List<InternalNode<K, V>> internalFindFirstMoreSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        InternalNode<K, V> container = internalFindExactOrFirstLessSpecific(range);
        if (container == null) {
            children.findFirstMoreSpecific(result, range);
        } else {
            container.getChildren().findFirstMoreSpecific(result, range);
        }
        return result;
    }

    private List<InternalNode<K, V>> internalFindAllMoreSpecific(K range) {
        List<InternalNode<K, V>> result = internalFindExactAndAllMoreSpecific(range);
        if (!result.isEmpty() && result.get(0).getInterval().equals(range)) {
            return result.subList(1, result.size());
        } else {
            return result;
        }
    }

    private List<InternalNode<K, V>> internalFindExactAndAllMoreSpecific(K range) {
        List<InternalNode<K, V>> result = new ArrayList<InternalNode<K, V>>();
        InternalNode<K, V> containing = internalFindExactOrFirstLessSpecific(range);
        if (containing == null) {
            children.findExactAndAllMoreSpecific(result, range);
        } else {
            if (containing.getInterval().equals(range)) {
                result.add(containing);
            }
            containing.getChildren().findExactAndAllMoreSpecific(result, range);
        }
        return result;
    }

    public abstract static class Key<K extends Interval<K>> {
        private final K key;

        public Key(K key) {
            Validate.notNull(key);
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key<?> that = (Key<?>) obj;
            return this.key.equals(that.key);
        }

        @Override
        public String toString() {
            return "IpResource(" + key + ")";
        }
    }
}
