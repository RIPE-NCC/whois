package net.ripe.db.whois.common.collect;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Deque;
import java.util.Iterator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class IterableTransformerTest {

    @Test
    public void empty_input_test() {
        final Iterable subject = getSimpleIterable();
        final Iterator<Integer> iterator = subject.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    public void add_header_test() {
        final IterableTransformer<Integer> subject = getOddFilteringIterable(4, 5);
        subject.setHeader(1,2,3);
        final Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(1));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(3));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(4));
        assertFalse(iterator.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void null_test() {
        final Iterable<Integer> subject = getSimpleIterable(1, null, 2, null);
        final Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(1));
        assertTrue(iterator.hasNext());
        assertNull(iterator.next());
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertTrue(iterator.hasNext());
        assertNull(iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void simple_test() {
        final Iterable<Integer> subject = getSimpleIterable(1,2,3);
        final Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(1));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(3));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void iterable_resettable() {
        final Iterable<Integer> subject = getSimpleIterable(1,2,3);
        Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(1));

        iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(1));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(3));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void odd_filter_test() {
        final Iterable<Integer> subject = getOddFilteringIterable(1, 2, 3);
        final Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void multiple_results_filter_test() {
        final Iterable<Integer> subject = getOddFilteringEvenDoublingIterable(1, 2, 3);
        final Iterator<Integer> iterator = subject.iterator();

        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertTrue(iterator.hasNext());
        assertThat(iterator.next(), is(2));
        assertFalse(iterator.hasNext());
    }

    private IterableTransformer<Integer> getSimpleIterable(Integer... values) {
        return new IterableTransformer<Integer>(Lists.newArrayList(values)) {
            @Override
            public void apply(Integer input, Deque<Integer> result) {
                result.add(input);
                return;
            }
        };
    }

    private IterableTransformer<Integer> getOddFilteringEvenDoublingIterable(Integer... values) {
        return new IterableTransformer<Integer>(Lists.newArrayList(values)) {
            @Override
            public void apply(Integer input, Deque<Integer> result) {
                if ((input & 1) == 0) {
                    result.add(input);
                    result.add(input);
                }
                return;
            }
        };
    }

    private IterableTransformer<Integer> getOddFilteringIterable(Integer... values) {
        return new IterableTransformer<Integer>(Lists.newArrayList(values)) {
            @Override
            public void apply(Integer input, Deque<Integer> result) {
                if ((input & 1) == 0) {
                    result.add(input);
                }
                return;
            }
        };
    }
}
