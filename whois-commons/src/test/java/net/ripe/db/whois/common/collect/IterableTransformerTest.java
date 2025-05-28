package net.ripe.db.whois.common.collect;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IterableTransformerTest {

    @Test
    public void empty_input_test() {
        final Iterable subject = getSimpleIterable();

        final Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void add_header_test() {
        final IterableTransformer<Integer> subject = getOddFilteringIterable(4, 5);
        subject.setHeader(1,2,3);

        final Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(1));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(3));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(4));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void null_test() {
        assertThrows(NullPointerException.class, () -> {
            final Iterable<Integer> subject = getSimpleIterable(1, null, 2, null);

            final Iterator<Integer> iterator = subject.iterator();

            assertThat(iterator.hasNext(), is(true));
            assertThat(iterator.next(), is(1));
            assertThat(iterator.hasNext(), is(true));
            assertThat(iterator.next(), is(nullValue()));
            assertThat(iterator.hasNext(), is(true));
            assertThat(iterator.next(), is(2));
            assertThat(iterator.hasNext(), is(true));
            assertThat(iterator.next(), is(nullValue()));
            assertThat(iterator.hasNext(), is(false));
        });
    }

    @Test
    public void simple_test() {
        final Iterable<Integer> subject = getSimpleIterable(1,2,3);

        final Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(1));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(3));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void iterable_resettable() {
        final Iterable<Integer> subject = getSimpleIterable(1,2,3);

        Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(1));
        iterator = subject.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(1));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(3));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void odd_filter_test() {
        final Iterable<Integer> subject = getOddFilteringIterable(1, 2, 3);

        final Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(false));
    }

    @Test
    public void multiple_results_filter_test() {
        final Iterable<Integer> subject = getOddFilteringEvenDoublingIterable(1, 2, 3);

        final Iterator<Integer> iterator = subject.iterator();

        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(2));
        assertThat(iterator.hasNext(), is(false));
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
            }
        };
    }
}
