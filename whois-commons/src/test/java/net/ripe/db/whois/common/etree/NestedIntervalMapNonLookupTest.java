package net.ripe.db.whois.common.etree;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import org.apache.commons.lang.Validate;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NestedIntervalMapNonLookupTest {

    private static final Map<String, Ipv4Resource> cache = new HashMap<String, Ipv4Resource>();

    private static final Ipv4Resource node(Object toStringify) {
        return node(toStringify.toString());
    }

    private static final Ipv4Resource node(String node) {
        Validate.isTrue(cache.containsKey(node), "Node " + node + " not in cache");
        return cache.get(node);
    }

    static {
        for (int i = 0; i <= 10; i++) {
            for (int j = i; j <= 10; j++) {
                Ipv4Resource resource = new Ipv4Resource(i, j);
                cache.put(String.format("%d-%d", i, j), resource);
                if (i == j) {
                    cache.put(Integer.toString(i), resource);
                }
            }
        }
    }

    private NestedIntervalMap<Ipv4Resource, Ipv4Resource> subject = new NestedIntervalMap<Ipv4Resource, Ipv4Resource>();

    private List<Ipv4Resource> allNodes() {
        return subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE);
    }

    private Ipv4Resource add(Ipv4Resource node) {
        subject.put(node, node);
        return node;
    }

    /* Single node */
    @Test(expected = IllegalArgumentException.class)
    public void addingNullShouldFail() {
        add(null);
    }

    @Test
    public void addSingleEntryNode() {
        Ipv4Resource node = node(1);
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertNotNull(subject.findExact(node));
    }

    @Test
    public void addRangedNode() {
        Ipv4Resource node = node("1-2");
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertNotNull(subject.findExact(node));
    }

    /* Siblings - root - simple */
    @Test
    public void addSiblingToRoot() {
        Ipv4Resource node1 = add(node(1));
        Ipv4Resource node2 = add(node(2));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
    }

    @Test
    public void addSiblingRangeLeftOfExisting() {
        add(node("3-4"));
        Ipv4Resource node = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeRightOfExisting() {
        add(node("1-2"));
        Ipv4Resource node = add(node("3-4"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeBetweenExisting() {
        add(node("1-2"));
        add(node("5-6"));
        Ipv4Resource node = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    /* Children */
    @Test
    public void addChild() {
        Ipv4Resource parent = add(node("1-10"));
        Ipv4Resource node = add(node("2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToFirstExisting() {
        Ipv4Resource parent = add(node("1-2"));
        add(node("3-4"));
        Ipv4Resource node = add(node("2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToLastExisting() {
        add(node("1-2"));
        Ipv4Resource parent = add(node("3-4"));
        Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToMiddleExisting() {
        add(node("1-2"));
        Ipv4Resource parent = add(node("3-4"));
        add(node("5-6"));
        Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(4));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildrenClearBounds() {
        List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-7")));
        parents.add(add(node("2-6")));
        parents.add(add(node("3-5")));
        Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));
        for (Ipv4Resource parent : parents) {
            assertNotNull(subject.findExact(parent));
        }
        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenLeftBounds() {
        List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-4")));
        parents.add(add(node("1-3")));
        parents.add(add(node("1-2")));
        Ipv4Resource node = add(node("1"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenRightBounds() {
        List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-4")));
        parents.add(add(node("2-4")));
        parents.add(add(node("3-4")));
        Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    /* Create parent */
    @Test
    public void addParentForSingleNode() {
        Ipv4Resource node = add(node("1"));
        Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentForMultipleNode() {
        Ipv4Resource node1 = add(node(1));
        Ipv4Resource node2 = add(node(2));
        Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node1), hasItems(parent));
        assertThat(subject.findAllLessSpecific(node2), hasItems(parent));
    }

    @Test
    public void addParentInbetweenTwoNodes() {
        Ipv4Resource parent = add(node("1-4"));
        add(node(2));
        Ipv4Resource node = add(node("2-3"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(node));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentStartIntersect() {
        Ipv4Resource node = add(node(2));
        add(node(3));
        Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(parent));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentEndIntersect() {
        add(node(2));
        Ipv4Resource node = add(node(3));
        Ipv4Resource parent = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertNotNull(subject.findExact(parent));
        assertNotNull(subject.findExact(node));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentMiddleIntersect() {
        add(node(1));
        Ipv4Resource node1 = add(node(2));
        Ipv4Resource node2 = add(node(3));
        add(node(4));
        Ipv4Resource parent = add(node("2-3"));

        assertThat(allNodes(), hasSize(5));
        assertNotNull(subject.findExact(node1));
        assertNotNull(subject.findExact(node2));
        assertNotNull(subject.findExact(parent));

        assertThat(subject.findAllLessSpecific(node1), hasItems(parent));
        assertThat(subject.findAllLessSpecific(node2), hasItems(parent));
    }

    /* Test clone + equals */
    @Test
    public void cloneTree() {
        add(node(1));
        add(node(2));
        add(node(3));
        add(node("1-2"));

        assertThat(allNodes(), hasSize(4));

        NestedIntervalMap<Ipv4Resource, Ipv4Resource> original = subject;
        subject = new NestedIntervalMap<Ipv4Resource, Ipv4Resource>(original);

        assertThat(allNodes(), hasSize(4));
        assertThat(original, is(subject));
        for (Ipv4Resource node : allNodes()) {
            assertNotNull(subject.findExact(node));
        }

        add(node(4));

        assertFalse(original.equals(subject));
        assertFalse(original.equals(null));
        assertFalse(original.equals("foo"));
    }

    /* Test intersects */
    @Test(expected = IntersectingIntervalException.class)
    public void leftIntersect() {
        add(node("2-3"));
        add(node("1-2"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void rightIntersect() {
        add(node("2-3"));
        add(node("3-4"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void fullLeftIntersect() {
        add(node("2-4"));
        add(node("1-3"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void fullRightIntersect() {
        add(node("2-4"));
        add(node("3-5"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void childLeftIntersect() {
        add(node("2"));
        add(node("3-4"));
        add(node("1-3"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void childRightIntersect() {
        add(node("2-3"));
        add(node("4"));
        add(node("3-5"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void childMiddleLeftIntersect() {
        add(node("2-3"));
        add(node("4"));
        add(node("5-6"));
        add(node("2-5"));
    }

    @Test(expected = IntersectingIntervalException.class)
    public void childMiddleRightIntersect() {
        add(node("2-3"));
        add(node("4"));
        add(node("5-6"));
        add(node("3-6"));
    }

    @Test
    public void clear() {
        add(node("1-5"));
        add(node("6-10"));
        add(node("2"));
        add(node("5"));
        add(node("6"));
        add(node("7"));
        add(node("8"));
        assertThat(allNodes(), hasSize(7));

        subject.clear();

        assertThat(allNodes(), hasSize(0));
    }
}
