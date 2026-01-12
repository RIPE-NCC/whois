package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.Validate;

import java.util.Objects;

final class InternalNode<K extends Interval<K>, V> {

    private final K interval;
    private V value;
    private ChildNodeMap<K, V> children = ChildNodeTreeMap.empty();

    public InternalNode(K interval, V value) {
        Validate.notNull(interval, "interval");
        Validate.notNull(value, "value");
        this.interval = interval;
        this.value = value;
    }

    public InternalNode(InternalNode<K, V> source) {
        this.interval = source.interval;
        this.value = source.value;
        this.children = source.children == ChildNodeTreeMap.EMPTY ? ChildNodeTreeMap.<K, V>empty() : new ChildNodeTreeMap<>(source.children);
    }

    public K getInterval() {
        return interval;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final InternalNode<?, ?> that = (InternalNode<?, ?>) obj;

        return Objects.equals(interval, that.interval) &&
                Objects.equals(value, that.value) &&
                Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval, value, children);
    }

    @Override
    public String toString() {
        return "Node(" + interval + ", " + value + ", " + children + ")";
    }

    ChildNodeMap<K, V> getChildren() {
        return children;
    }

    void addChild(InternalNode<K, V> nodeToAdd) {
        if (interval.equals(nodeToAdd.getInterval())) {
            this.value = nodeToAdd.getValue();
        } else if (!interval.contains(nodeToAdd.getInterval())) {
            throw new IllegalArgumentException(nodeToAdd.getInterval() + " not properly contained in " + interval);
        } else {
            if (children == ChildNodeTreeMap.EMPTY) {
                children = new ChildNodeTreeMap<>();
            }
            children.addChild(nodeToAdd);
        }
    }

    public void removeChild(K range) {
        if (!interval.contains(range) || interval.equals(range)) {
            throw new IllegalArgumentException(range + " not properly contained in " + interval);
        }
        if (children != ChildNodeTreeMap.EMPTY) {
            children.removeChild(range);
            if (children.isEmpty()) {
                children = ChildNodeTreeMap.empty();
            }
        }
    }
}
