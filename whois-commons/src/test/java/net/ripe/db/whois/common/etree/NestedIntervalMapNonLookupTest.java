package net.ripe.db.whois.common.etree;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import org.apache.commons.lang.Validate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NestedIntervalMapNonLookupTest {

    private static final Map<String, Ipv4Resource> cache = new HashMap<>();

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

    private NestedIntervalMap<Ipv4Resource, Ipv4Resource> subject = new NestedIntervalMap<>();

    private List<Ipv4Resource> allNodes() {
        return subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE);
    }

    private Ipv4Resource add(Ipv4Resource node) {
        subject.put(node, node);
        return node;
    }

    /* Single node */
    @Test
    public void addingNullShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            add(null);
        });
    }

    @Test
    public void addSingleEntryNode() {
        final Ipv4Resource node = node(1);
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertThat(subject.findExact(node), not(nullValue()));
    }

    @Test
    public void addRangedNode() {
        final Ipv4Resource node = node("1-2");
        add(node);

        assertThat(allNodes(), hasSize(1));
        assertThat(subject.findExact(node), not(nullValue()));
    }

    /* Siblings - root - simple */
    @Test
    public void addSiblingToRoot() {
        final Ipv4Resource node1 = add(node(1));
        final Ipv4Resource node2 = add(node(2));

        assertThat(allNodes(), hasSize(2));
        assertThat(subject.findExact(node1), not(nullValue()));
        assertThat(subject.findExact(node2), not(nullValue()));
    }

    @Test
    public void addSiblingRangeLeftOfExisting() {
        add(node("3-4"));
        final Ipv4Resource node = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeRightOfExisting() {
        add(node("1-2"));
        final Ipv4Resource node = add(node("3-4"));

        assertThat(allNodes(), hasSize(2));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    @Test
    public void addSiblingRangeBetweenExisting() {
        add(node("1-2"));
        add(node("5-6"));
        final Ipv4Resource node = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findAllLessSpecific(node), hasSize(0));
    }

    /* Children */
    @Test
    public void addChild() {
        final Ipv4Resource parent = add(node("1-10"));
        final Ipv4Resource node = add(node("2"));

        assertThat(allNodes(), hasSize(2));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToFirstExisting() {
        final Ipv4Resource parent = add(node("1-2"));
        add(node("3-4"));
        final Ipv4Resource node = add(node("2"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToLastExisting() {
        add(node("1-2"));
        final Ipv4Resource parent = add(node("3-4"));
        final Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildToMiddleExisting() {
        add(node("1-2"));
        final Ipv4Resource parent = add(node("3-4"));
        add(node("5-6"));
        final Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(4));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addChildrenClearBounds() {
        final List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-7")));
        parents.add(add(node("2-6")));
        parents.add(add(node("3-5")));
        final Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertThat(subject.findExact(node), not(nullValue()));
        for (Ipv4Resource parent : parents) {
            assertThat(subject.findExact(parent), not(nullValue()));
        }
        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenLeftBounds() {
        final List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-4")));
        parents.add(add(node("1-3")));
        parents.add(add(node("1-2")));
        final Ipv4Resource node = add(node("1"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertThat(subject.findExact(node), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    @Test
    public void addChildrenRightBounds() {
        final List<Ipv4Resource> parents = Lists.newArrayList();
        parents.add(add(node("1-4")));
        parents.add(add(node("2-4")));
        parents.add(add(node("3-4")));
        final Ipv4Resource node = add(node("4"));

        assertThat(allNodes(), hasSize(parents.size() + 1));
        assertThat(subject.findExact(node), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), is(parents));
    }

    /* Create parent */
    @Test
    public void addParentForSingleNode() {
        final Ipv4Resource node = add(node("1"));
        final Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(2));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentForMultipleNode() {
        final Ipv4Resource node1 = add(node(1));
        final Ipv4Resource node2 = add(node(2));
        final Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(node1), not(nullValue()));
        assertThat(subject.findExact(node2), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node1), hasItems(parent));
        assertThat(subject.findAllLessSpecific(node2), hasItems(parent));
    }

    @Test
    public void addParentInbetweenTwoNodes() {
        final Ipv4Resource parent = add(node("1-4"));
        add(node(2));
        final Ipv4Resource node = add(node("2-3"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(node), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentStartIntersect() {
        final Ipv4Resource node = add(node(2));
        add(node(3));
        final Ipv4Resource parent = add(node("1-2"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(parent), not(nullValue()));
        assertThat(subject.findExact(node), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentEndIntersect() {
        add(node(2));
        final Ipv4Resource node = add(node(3));
        final Ipv4Resource parent = add(node("3-4"));

        assertThat(allNodes(), hasSize(3));
        assertThat(subject.findExact(parent), not(nullValue()));
        assertThat(subject.findExact(node), not(nullValue()));

        assertThat(subject.findAllLessSpecific(node), hasItems(parent));
    }

    @Test
    public void addParentMiddleIntersect() {
        add(node(1));
        final Ipv4Resource node1 = add(node(2));
        final Ipv4Resource node2 = add(node(3));
        add(node(4));
        final Ipv4Resource parent = add(node("2-3"));

        assertThat(allNodes(), hasSize(5));
        assertThat(subject.findExact(node1), not(nullValue()));
        assertThat(subject.findExact(node2), not(nullValue()));
        assertThat(subject.findExact(parent), not(nullValue()));

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

        final NestedIntervalMap<Ipv4Resource, Ipv4Resource> original = subject;
        subject = new NestedIntervalMap<>(original);

        assertThat(allNodes(), hasSize(4));
        assertThat(original, is(subject));
        for (Ipv4Resource node : allNodes()) {
            assertThat(subject.findExact(node), not(nullValue()));
        }

        add(node(4));

        assertThat(original, not(equalTo(subject)));
        assertThat(original, not(equalTo(null)));
        assertThat(original, not(equalTo("foo")));
    }

    /* Test intersects */
    @Test
    public void leftIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-3"));
            add(node("1-2"));
        });
    }

    @Test
    public void rightIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-3"));
            add(node("3-4"));
        });
    }

    @Test
    public void fullLeftIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-4"));
            add(node("1-3"));
        });
    }

    @Test
    public void fullRightIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-4"));
            add(node("3-5"));
        });
    }

    @Test
    public void childLeftIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2"));
            add(node("3-4"));
            add(node("1-3"));
        });
    }

    @Test
    public void childRightIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-3"));
            add(node("4"));
            add(node("3-5"));
        });

    }

    @Test
    public void childMiddleLeftIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-3"));
            add(node("4"));
            add(node("5-6"));
            add(node("2-5"));
        });

    }

    @Test
    public void childMiddleRightIntersect() {
        assertThrows(IntersectingIntervalException.class, () -> {
            add(node("2-3"));
            add(node("4"));
            add(node("5-6"));
            add(node("3-6"));
        });

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
