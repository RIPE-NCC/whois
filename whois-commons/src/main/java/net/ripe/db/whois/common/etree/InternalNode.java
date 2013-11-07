package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.ip.Interval;
import org.apache.commons.lang.Validate;

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
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final InternalNode<?, ?> that = (InternalNode<?, ?>) obj;
        return this.interval.equals(that.interval) && this.value.equals(that.value) && this.children.equals(that.children);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + interval.hashCode();
        result = prime * result + value.hashCode();
        result = prime * result + children.hashCode();
        return result;
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
