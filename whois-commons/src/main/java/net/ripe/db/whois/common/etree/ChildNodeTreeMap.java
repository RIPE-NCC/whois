package net.ripe.db.whois.common.etree;

import java.util.*;
import java.util.Map.Entry;

/**
 * Keeps a sorted map of child nodes ordered by the upper-bound of each child
 * interval. Intervals are not allowed to intersect (a
 * {@link IntersectingIntervalException} is thrown if an attempt is made to
 * insert siblings with intersecting intervals). This allows quick lookup of
 * matching intervals by comparing an interval's <em>lower-bound</em> with the
 * existing interval's <em>upper-bounds</em>.
 */
class ChildNodeTreeMap<K extends Interval<K>, V> extends TreeMap<K, InternalNode<K, V>> implements ChildNodeMap<K, V> {

    private static final long serialVersionUID = 1L;

    /*
     * Shared instance of empty child node map. This reduces memory usage, since
     * many nodes will not have any children.
     */
    @SuppressWarnings("rawtypes")
    static final ChildNodeMap EMPTY = new Empty();

    /**
     * @return an unmodifiable, empty {@link ChildNodeMap}.
     */
    @SuppressWarnings("unchecked")
    static <K extends Interval<K>, T> ChildNodeMap<K, T> empty() {
        return (ChildNodeMap<K, T>) EMPTY;
    }

    /*
     * Compares the upper-bound of two intervals. This comparator is not
     * consistent with {@link Interval#equals(Object)}.
     */
    @SuppressWarnings("rawtypes")
    private static final Comparator<Interval> UPPER_BOUND_COMPARATOR = new Comparator<Interval>() {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Interval o1, Interval o2) {
            return o1.compareUpperBound(o2);
        }
    };

    ChildNodeTreeMap() {
        super(UPPER_BOUND_COMPARATOR);
    }

    public ChildNodeTreeMap(ChildNodeMap<K, V> source) {
        this();
        for (InternalNode<K, V> node : source.values()) {
            this.put(node.getInterval(), new InternalNode<>(node));
        }
    }

    @Override
    public void addChild(InternalNode<K, V> nodeToAdd) {
        K range = nodeToAdd.getInterval();
        InternalNode<K, V> containingChild = getChildContaining(range);
        if (containingChild != null) {
            containingChild.addChild(nodeToAdd);
            return;
        }

        List<K> intersections = getIntersectingChildren(range);
        if (!intersections.isEmpty()) {
            throw new IntersectingIntervalException(range, intersections);
        }

        transferChildNodes(nodeToAdd);

        this.put(range, nodeToAdd);
    }

    private void transferChildNodes(InternalNode<K, V> nodeToAdd) {
        K range = nodeToAdd.getInterval();
        for (Iterator<InternalNode<K, V>> it = this.tailMap(range.singletonIntervalAtLowerBound()).values().iterator(); it.hasNext(); ) {
            InternalNode<K, V> child = it.next();
            if (range.contains(child.getInterval())) {
                nodeToAdd.addChild(child);
                it.remove();
            } else {
                break;
            }
        }
    }

    @Override
    public void removeChild(K interval) {
        final InternalNode<K, V> containing = getChildContaining(interval);
        if (containing == null) {
            return;
        }

        if (interval.equals(containing.getInterval())) {
            this.remove(interval);
            for (InternalNode<K, V> node : containing.getChildren().values()) {
                put(node.getInterval(), node);
            }
        } else {
            containing.removeChild(interval);
        }
    }

    private List<K> getIntersectingChildren(K range) {
        List<K> result = Collections.emptyList();
        K lowerCandidate = this.ceilingKey(range.singletonIntervalAtLowerBound());
        if (lowerCandidate != null && intersectsButNotContained(range, lowerCandidate)) {
            result = new ArrayList<>(result);
            result.add(lowerCandidate);
        }
        K upperCandidate = this.ceilingKey(range);
        if (upperCandidate != null && intersectsButNotContained(range, upperCandidate)) {
            result = new ArrayList<>(result);
            result.add(upperCandidate);
        }
        return result;
    }

    private boolean intersectsButNotContained(K left, K right) {
        return left.intersects(right) && !left.contains(right) && !right.contains(left);
    }

    private InternalNode<K, V> getChildContaining(K range) {
        Entry<K, InternalNode<K, V>> entry = this.ceilingEntry(range.singletonIntervalAtLowerBound());
        if (entry != null && entry.getKey().contains(range)) {
            return entry.getValue();
        } else {
            return null;
        }
    }

    @Override
    public void findExactAndAllLessSpecific(List<InternalNode<K, V>> result, K range) {
        InternalNode<K, V> node = getChildContaining(range);
        if (node != null) {
            result.add(node);
            node.getChildren().findExactAndAllLessSpecific(result, range);
        }
    }

    @Override
    public void findExactAndAllMoreSpecific(List<InternalNode<K, V>> result, K range) {
        for (InternalNode<K, V> node : this.tailMap(range.singletonIntervalAtLowerBound()).values()) {
            if (range.contains(node.getInterval())) {
                result.add(node);
                node.getChildren().addAllChildrenToList(result);
            } else if (range.intersects(node.getInterval())) {
                node.getChildren().findExactAndAllMoreSpecific(result, range);
            } else {
                break;
            }
        }
    }

    @Override
    public void findFirstMoreSpecific(List<InternalNode<K, V>> result, K range) {
        for (InternalNode<K, V> node : this.tailMap(range.singletonIntervalAtLowerBound()).values()) {
            if (range.contains(node.getInterval())) {
                result.add(node);
            } else if (range.intersects(node.getInterval())) {
                node.getChildren().findFirstMoreSpecific(result, range);
            } else {
                break;
            }
        }
    }

    @Override
    public void addAllChildrenToList(List<InternalNode<K, V>> list) {
        for (InternalNode<K, V> node : this.values()) {
            list.add(node);
            node.getChildren().addAllChildrenToList(list);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class Empty extends TreeMap implements ChildNodeMap {
        private static final long serialVersionUID = 1L;

        @Override
        public void addChild(InternalNode childToAdd) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeChild(Interval interval) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findExactAndAllLessSpecific(List list, Interval range) {
        }

        @Override
        public void findExactAndAllMoreSpecific(List list, Interval range) {
        }

        @Override
        public void findFirstMoreSpecific(List list, Interval interval) {
        }

        @Override
        public void addAllChildrenToList(List list) {
        }
    }

}
