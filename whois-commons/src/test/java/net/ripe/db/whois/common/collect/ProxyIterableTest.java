package net.ripe.db.whois.common.collect;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProxyIterableTest {
    private ProxyIterable<Integer, String> subject;
    private Iterable<Integer> source;
    private ProxyLoader<Integer, String> loader;

    @Test
    public void test_load_single_fetch() {
        testWithPrefetch(100);
    }

    @Test
    public void test_load_exact_fetch() {
        testWithPrefetch(6);
    }

    @Test
    public void test_load_proxy_multiple_fetches() {
        testWithPrefetch(2);
    }

    @Test
    public void test_load_proxy_all_fetches() {
        testWithPrefetch(1);
    }

    @Test
    public void test_remove() {
        assertThrows(UnsupportedOperationException.class, () -> {
            final ProxyLoader<Integer, String> proxyLoader = Mockito.mock(ProxyLoader.class);
            subject = new ProxyIterable<>(Collections.<Integer>emptyList(), proxyLoader, 1);
            subject.iterator().remove();
        });
    }

    @Test
    public void test_empty_next() {
        assertThrows(NoSuchElementException.class, () -> {
            final ProxyLoader<Integer, String> proxyLoader = Mockito.mock(ProxyLoader.class);
            subject = new ProxyIterable<>(Collections.<Integer>emptyList(), proxyLoader, 1);
            subject.iterator().next();
        });

    }

    @Test
    public void test_load_empty() {
        final ProxyLoader<Integer, String> proxyLoader = Mockito.mock(ProxyLoader.class);
        subject = new ProxyIterable<>(Arrays.asList(1, 2, 3), proxyLoader, 1);
        final Iterator<String> iterator = subject.iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(nullValue()));
    }

    private void testWithPrefetch(final int prefetch) {
        final int total = 6;

        source = Arrays.asList(1, 2, 3, 4, 5, 6);
        loader = new ProxyLoader<Integer, String>() {
            @Override
            public void load(final List<Integer> proxy, final List<String> result) {
                assertThat(proxy, hasSize(Math.min(total, prefetch)));
                assertThat(result, hasSize(0));

                for (final Integer integer : proxy) {
                    result.add(String.valueOf(integer));
                }
            }
        };

        subject = new ProxyIterable<>(source, loader, prefetch);
        final Iterator<String> iterator = subject.iterator();

        int count = 0;
        while (iterator.hasNext()) {
            final String next = iterator.next();
            assertThat(next, is(String.valueOf(++count)));
        }

        assertThat(count, is(total));
    }
}
