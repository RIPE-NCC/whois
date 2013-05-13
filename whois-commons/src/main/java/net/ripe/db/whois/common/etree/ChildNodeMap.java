package net.ripe.db.whois.common.etree;

import java.util.Collection;
import java.util.List;

/**
 * Internal interface to represent a collection of non-intersecting nodes (where
 * each node can have additional child nodes, recursively).
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
interface ChildNodeMap<K extends Interval<K>, V> {

    void addChild(InternalNode<K, V> nodeToAdd);

    void removeChild(K interval);

    void findExactAndAllLessSpecific(List<InternalNode<K, V>> list, K interval);

    void findExactAndAllMoreSpecific(List<InternalNode<K, V>> list, K interval);

    void findFirstMoreSpecific(List<InternalNode<K, V>> list, K interval);

    void addAllChildrenToList(List<InternalNode<K, V>> list);

    boolean isEmpty();

    void clear();

    Collection<InternalNode<K, V>> values();
}
