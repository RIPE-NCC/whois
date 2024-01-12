package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Test the {@link net.ripe.db.whois.common.etree.NestedIntervalMap} using random data so we can flush out bugs
 * that we didn't expect.
 */
public class NestedIntervalMapRandomTest {

    private final long seed = System.currentTimeMillis();
    private final Random random = new Random(seed);

    private List<Ipv4Resource> everything;
    private Map<Ipv4Resource, List<Ipv4Resource>> childrenByParent;
    private NestedIntervalMap<Ipv4Resource, Ipv4Resource> subject;

    private List<Ipv4Resource> generateRandomSiblings(Ipv4Resource parent, int count) {
        List<Ipv4Resource> result = new ArrayList<>();
        if (count == 0)
            return result;

        long size = parent.end() - parent.begin() + 1;
        long sizePerChild = size / count;

        long start = parent.begin();
        for (int i = 0; i < count; ++i) {
            long gapBefore = sizePerChild * random.nextInt(4) / 10L;
            long gapAfter = sizePerChild * random.nextInt(1) / 10L;
            Ipv4Resource child = new Ipv4Resource(start + gapBefore, start + sizePerChild - gapAfter - 1);
            start += sizePerChild;
            assertThat(parent.contains(child), is(true)); //  "generated child not inside parent (seed = " + seed + ")");
            if (!parent.equals(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private void generateRandomTree(Map<Ipv4Resource, List<Ipv4Resource>> result, Ipv4Resource parent, int depth, int siblingCount) {
        List<Ipv4Resource> children = generateRandomSiblings(parent, siblingCount * (7 + random.nextInt(7)) / 10);
        result.put(parent, children);

        if (depth > 0) {
            for (Ipv4Resource child : children) {
                generateRandomTree(result, child, depth - 1, siblingCount);
            }
        }
    }

    @BeforeEach
    public void setup() {
        everything = new ArrayList<>();
        childrenByParent = new HashMap<>();
        subject = new NestedIntervalMap<>();

        List<Ipv4Resource> roots = generateRandomSiblings(Ipv4Resource.MAX_RANGE, random.nextInt(3) + 5);
        for (Ipv4Resource root : roots) {
            generateRandomTree(childrenByParent, root, random.nextInt(4), random.nextInt(4) + 3);
        }
        everything.addAll(roots);
        for (List<Ipv4Resource> children : childrenByParent.values()) {
            everything.addAll(children);
        }
        for (Ipv4Resource interval : everything) {
            subject.put(interval, interval);
        }

        Collections.sort(everything);
        System.err.println("RANDOM NESTED INTERVAL MAP TEST: Generated " + everything.size() + " intervals with seed " + seed);
    }

    @Test
    public void should_find_everything() {
        assertThat("failed with seed: " + seed, subject.findExactAndAllMoreSpecific(Ipv4Resource.MAX_RANGE), equalTo(everything));
    }

    @Test
    public void should_find_every_interval_individually() {
        for (Ipv4Resource interval : everything) {
            assertThat("failed with seed: " + seed, subject.findExact(interval), contains(interval));
        }
    }

    @Test
    public void should_find_all_more_specific() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Resource range = randomIpv4Interval();
            List<Ipv4Resource> actual = subject.findExactAndAllMoreSpecific(range);
            List<Ipv4Resource> expected = new ArrayList<>();
            for (Ipv4Resource interval : everything) {
                if (range.contains(interval)) {
                    expected.add(interval);
                }
            }
            assertThat("failed with seed: " + seed, actual, equalTo(expected));
        }
    }

    @Test
    public void should_find_all_less_specific() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Resource range = randomIpv4Interval();
            List<Ipv4Resource> actual = subject.findExactAndAllLessSpecific(range);
            List<Ipv4Resource> expected = new ArrayList<>();
            for (Ipv4Resource interval : everything) {
                if (interval.contains(range)) {
                    expected.add(interval);
                }
            }

            assertThat("failed with seed: " + seed, actual, equalTo(expected));
        }
    }

    @Test
    public void should_find_first_more_specific_for_every_contained_interval() {
        for (Ipv4Resource interval : childrenByParent.keySet()) {
            assertThat("interval: " + interval + ", seed = " + seed, subject.findFirstMoreSpecific(interval), equalTo(childrenByParent.get(interval)));
        }
    }

    @Test
    public void should_promote_children_of_delete_node_to_parent() {
        for (int i = 0; i < 10; ) {
            NestedIntervalMap<Ipv4Resource, Ipv4Resource> copy = new NestedIntervalMap<>(subject);
            Ipv4Resource interval = everything.get(random.nextInt(everything.size()));
            if (childrenByParent.containsKey(interval)) {
                Ipv4Resource parent = CollectionHelper.uniqueResult(copy.findFirstLessSpecific(interval));
                if (parent != null) {
                    copy.remove(interval);
                    List<Ipv4Resource> actual = copy.findFirstMoreSpecific(parent);
                    assertThat("interval " + interval + " did not move all children to parent " + parent + " on deletion (seed = " + seed + "): " + actual, actual.containsAll(childrenByParent.get(interval)), is(true));
                    ++i;
                }
            }

        }
    }

    @Test
    public void should_contain_first_more_specific_for_random_intervals() {
        for (int i = 0; i < 100; ++i) {
            Ipv4Resource range = randomIpv4Interval();
            List<Ipv4Resource> actual = subject.findFirstMoreSpecific(range);
            List<Ipv4Resource> allMoreSpecific = subject.findAllMoreSpecific(range);
            assertThat(allMoreSpecific.containsAll(actual), is(true));  // "first more specific is subset of all more specific"
            for (Ipv4Resource moreSpecific : allMoreSpecific) {
                boolean covered = false;
                for (Ipv4Resource firstMoreSpecific : actual) {
                    if (firstMoreSpecific.contains(moreSpecific)) {
                        covered = true;
                        break;
                    }
                }
                assertThat("All more specific " + moreSpecific + " must be contained by first more specific", covered, is(true));
            }
        }
    }

    @Test
    public void should_remove_all_intervals_starting_with_child_nodes() {
        Collections.reverse(everything);
        for (Ipv4Resource interval : everything) {
            subject.remove(interval);
        }
        assertThat(subject.findAllMoreSpecific(Ipv4Resource.MAX_RANGE), is(empty()));
    }

    private Ipv4Resource randomIpv4Interval() {
        return new Ipv4Resource(random.nextInt(Integer.MAX_VALUE), random.nextInt(Integer.MAX_VALUE) + (Ipv4Resource.MAX_RANGE.end() / 2));
    }

}
