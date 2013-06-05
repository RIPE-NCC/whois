package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NestedIntervalMapTest {

    private NestedIntervalMap<Ipv4Resource, Ipv4Resource> subject = new NestedIntervalMap<>();
    private Ipv4Resource N1_12 = new Ipv4Resource(1, 12);
    private Ipv4Resource N1_4 = new Ipv4Resource(1, 4);
    private Ipv4Resource N5_10 = new Ipv4Resource(5, 10);
    private Ipv4Resource N1_1 = new Ipv4Resource(1, 1);
    private Ipv4Resource N2_2 = new Ipv4Resource(2, 2);
    private Ipv4Resource N3_3 = new Ipv4Resource(3, 3);
    private Ipv4Resource N4_4 = new Ipv4Resource(4, 4);
    private Ipv4Resource N5_5 = new Ipv4Resource(5, 5);
    private Ipv4Resource N6_6 = new Ipv4Resource(6, 6);
    private Ipv4Resource N7_7 = new Ipv4Resource(7, 7);
    private Ipv4Resource N8_8 = new Ipv4Resource(8, 8);
    private Ipv4Resource N9_9 = new Ipv4Resource(9, 9);
    private Ipv4Resource N10_10 = new Ipv4Resource(10, 10);
    private Ipv4Resource N3_4 = new Ipv4Resource(3, 4);
    private Ipv4Resource N5_8 = new Ipv4Resource(5, 8);
    private Ipv4Resource N9_10 = new Ipv4Resource(9, 10);
    private Ipv4Resource N11_12 = new Ipv4Resource(11, 12);
    private List<Ipv4Resource> all = new ArrayList<>();

    @Before
    public void setup() {
        all.add(N1_12);
        all.add(N1_4);
        all.add(N5_10);
        all.add(N1_1);
        all.add(N2_2);
        all.add(N3_3);
        all.add(N4_4);
        all.add(N5_5);
        all.add(N6_6);
        all.add(N7_7);
//        all.add(N8_8);
        all.add(N9_9);
        all.add(N10_10);
        all.add(N3_4);
        all.add(N5_8);
        all.add(N9_10);
        all.add(N11_12);
        Collections.sort(all);

        for (Ipv4Resource n : all) {
            subject.put(n, n);
        }
    }

    @Test
    public void clear() {
        subject.put(N1_12, N1_1);
        subject.clear();
        assertThat(subject.findExact(N1_12), not(contains(N1_12)));
        assertThat(subject.findExact(N1_12), not(contains(N1_1)));
    }

    @Test
    public void test_replace_n1_10() {
        subject.put(N1_12, N1_1);
        assertThat(subject.findExact(N1_12), contains(N1_1));
    }

    @Test
    public void fail_on_intersecting_siblings() {
        try {
            subject.put(new Ipv4Resource(8, 13), N1_1);
            fail("Exception expected");
        } catch (IntersectingIntervalException expected) {
            assertEquals(new Ipv4Resource(8, 13), expected.getInterval());
            assertEquals(asList(N1_12), expected.getIntersections());
        }
    }

    @Test
    public void test_remove_n1_10() {
        subject.remove(N1_12);
        assertThat(subject.findExact(N1_12), hasSize(0));
        assertThat(subject.findFirstMoreSpecific(N1_12), contains(N1_4, N5_10, N11_12));
    }

    @Test
    public void test_remove_n5_8() {
        assertEquals(asList(N5_5, N6_6, N7_7), subject.findFirstMoreSpecific(N5_8));
        subject.remove(N5_8);
        assertThat(subject.findExact(N5_8), hasSize(0));
        assertThat(subject.findFirstMoreSpecific(N5_8), contains(N5_5, N6_6, N7_7));
    }

    @Test
    public void test_remove_key_value() {
        assertEquals(asList(N5_5, N6_6, N7_7), subject.findFirstMoreSpecific(N5_8));
        subject.remove(N5_8, N5_8);
        assertThat(subject.findExact(N5_8), hasSize(0));
        assertThat(subject.findFirstMoreSpecific(N5_8), contains(N5_5, N6_6, N7_7));
    }

    @Test
    public void test_remove_key_value_nonexistant() {
        NestedIntervalMap<Ipv4Resource, Ipv4Resource> copy = new NestedIntervalMap<>(subject);

        final Ipv4Resource resource = new Ipv4Resource(0, 100);
        subject.remove(resource, resource);
        assertEquals(copy, subject);
    }

    @Test
    public void test_remove_nonexistant() {
        NestedIntervalMap<Ipv4Resource, Ipv4Resource> copy = new NestedIntervalMap<>(subject);

        subject.remove(new Ipv4Resource(0, 100));
        assertEquals(copy, subject);

        subject.remove(new Ipv4Resource(1, 7));
        assertEquals(copy, subject);

        subject.remove(new Ipv4Resource(12, 12));
        assertEquals(copy, subject);
    }

    @Test
    public void test_equals_hashcode() {
        assertFalse(subject.equals(null));
        assertEquals(subject, subject);
        assertFalse(subject.equals(new Object()));
        assertFalse(subject.equals(new NestedIntervalMap<Ipv4Resource, Ipv4Resource>()));

        assertEquals(subject.hashCode(), subject.hashCode());
        assertFalse(subject.hashCode() == new NestedIntervalMap<Ipv4Resource, Ipv4Resource>().hashCode());
    }

    @Test
    public void test_find_all_less_specific() {
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(new Ipv4Resource(0, 100)));
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(new Ipv4Resource(5, 13)));
        assertEquals(Collections.emptyList(), subject.findAllLessSpecific(N1_12));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findAllLessSpecific(N6_6));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findAllLessSpecific(N8_8));
        assertEquals(asList(N1_12, N1_4), subject.findAllLessSpecific(N2_2));
    }

    @Test
    public void test_find_exact_and_all_less_specific() {
        assertEquals(Collections.emptyList(), subject.findExactAndAllLessSpecific(new Ipv4Resource(0, 100)));
        assertEquals(Collections.emptyList(), subject.findExactAndAllLessSpecific(new Ipv4Resource(5, 13)));
        assertEquals(asList(N1_12), subject.findExactAndAllLessSpecific(N1_12));
        assertEquals(asList(N1_12, N5_10, N5_8, N6_6), subject.findExactAndAllLessSpecific(N6_6));
        assertEquals(asList(N1_12, N5_10, N5_8), subject.findExactAndAllLessSpecific(N8_8));
        assertEquals(asList(N1_12, N1_4, N2_2), subject.findExactAndAllLessSpecific(N2_2));
    }

    @Test
    public void test_find_exact_or_first_less_specific() {
        assertThat(subject.findExactOrFirstLessSpecific(new Ipv4Resource(0, 100)), hasSize(0));
        assertThat(subject.findExactOrFirstLessSpecific(new Ipv4Resource(5, 13)), hasSize(0));

        assertThat(subject.findExactOrFirstLessSpecific(N1_12), contains(N1_12));
        assertThat(subject.findExactOrFirstLessSpecific(N6_6), contains(N6_6));
        assertThat(subject.findExactOrFirstLessSpecific(N8_8), contains(N5_8));
        assertThat(subject.findExactOrFirstLessSpecific(N2_2), contains(N2_2));
    }

    @Test
    public void testFindFirstLessSpecific() {
        assertThat(subject.findFirstLessSpecific(N1_12), hasSize(0));

        assertThat(subject.findFirstLessSpecific(N6_6), contains(N5_8));
        assertThat(subject.findFirstLessSpecific(N8_8), contains(N5_8));
        assertThat(subject.findFirstLessSpecific(N2_2), contains(N1_4));
        assertThat(subject.findFirstLessSpecific(new Ipv4Resource(3, 7)), contains(N1_12));
    }

    @Test
    public void testFindEverything() {
        assertEquals(all, subject.findExactAndAllMoreSpecific(Ipv4Resource.MAX_RANGE));
        subject.put(Ipv4Resource.MAX_RANGE, Ipv4Resource.MAX_RANGE);
    }

    @Test
    public void testFindFirstMoreSpecific() {
        assertEquals(asList(N5_8, N9_10), subject.findFirstMoreSpecific(N5_10));
        assertEquals(asList(N1_1, N2_2, N3_4), subject.findFirstMoreSpecific(N1_4));
        assertEquals(asList(N7_7, N9_9), subject.findFirstMoreSpecific(new Ipv4Resource(7, 9)));
        assertEquals(asList(N9_9), subject.findFirstMoreSpecific(new Ipv4Resource(8, 9)));
    }

    @Test
    public void testFindExact() {
        for (Ipv4Resource n : all) {
            assertThat(subject.findExact(n), contains(n));
        }
    }

    @Test
    public void testFindAllMoreSpecific() {
        assertEquals(all.subList(1, all.size()), subject.findAllMoreSpecific(N1_12));
        assertEquals(asList(N3_4, N3_3, N4_4, N5_5, N6_6, N7_7), subject.findAllMoreSpecific(new Ipv4Resource(3, 7)));
        assertEquals(asList(N9_9), subject.findAllMoreSpecific(new Ipv4Resource(8, 9)));
    }

    @Test
    public void testFindExactAndAllMoreSpecific() {
        assertEquals(all, subject.findExactAndAllMoreSpecific(N1_12));
        assertEquals(asList(N1_4, N1_1, N2_2, N3_4, N3_3, N4_4), subject.findExactAndAllMoreSpecific(N1_4));
    }

    @Test
    public void detect_intersect_on_lower_bound_of_new_interval() {
        Ipv4Resource child1 = new Ipv4Resource(1, 10);
        Ipv4Resource child2 = new Ipv4Resource(11, 15);
        Ipv4Resource child3 = new Ipv4Resource(16, 25);
        Ipv4Resource intersect = new Ipv4Resource(8, 30);

        NestedIntervalMap<Ipv4Resource, Ipv4Resource> test = new NestedIntervalMap<>();
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(intersect, intersect);
            fail("Exception expected");
        } catch (IntersectingIntervalException expected) {
            assertEquals(intersect, expected.getInterval());
            assertEquals(asList(child1), expected.getIntersections());
        }

        assertThat(subject.findExact(intersect), hasSize(0));
    }

    @Test
    public void detect_intersect_on_upper_bound_of_new_interval() {
        Ipv4Resource child1 = new Ipv4Resource(1, 10);
        Ipv4Resource child2 = new Ipv4Resource(11, 15);
        Ipv4Resource child3 = new Ipv4Resource(16, 25);
        Ipv4Resource intersect = new Ipv4Resource(1, 21);

        NestedIntervalMap<Ipv4Resource, Ipv4Resource> test = new NestedIntervalMap<>();
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(intersect, intersect);
            fail("Exception expected");
        } catch (IntersectingIntervalException expected) {
            assertEquals(intersect, expected.getInterval());
            assertEquals(asList(child3), expected.getIntersections());
        }

        assertThat(subject.findExact(intersect), hasSize(0));
    }

    @Test
    public void detect_intersect_on_lower_and_upper_bound_of_new_interval() {
        Ipv4Resource child1 = new Ipv4Resource(1, 10);
        Ipv4Resource child2 = new Ipv4Resource(11, 15);
        Ipv4Resource child3 = new Ipv4Resource(16, 25);
        Ipv4Resource intersect = new Ipv4Resource(4, 21);

        NestedIntervalMap<Ipv4Resource, Ipv4Resource> test = new NestedIntervalMap<>();
        test.put(child1, child1);
        test.put(child2, child2);
        test.put(child3, child3);
        try {
            test.put(intersect, intersect);
            fail("Exception expected");
        } catch (IntersectingIntervalException expected) {
            assertEquals(intersect, expected.getInterval());
            assertEquals(asList(child1, child3), expected.getIntersections());
        }

        assertThat(subject.findExact(intersect), hasSize(0));
    }
}
