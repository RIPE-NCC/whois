package net.ripe.db.whois.common.collect;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ProxyIterable<P, R> implements Iterable<R> {
    private final ProxyLoader<P, R> loader;
    private final int prefetch;

    private final List<R> initialBatch;
    private final Iterable<P> source;

    public ProxyIterable(final Iterable<P> source, final ProxyLoader<P, R> loader, final int prefetch) {
        this.loader = loader;
        this.prefetch = prefetch;

        final List<P> initialProxyBatch = nextBatch(source.iterator());
        this.initialBatch = load(initialProxyBatch);
        this.source = Iterables.skip(source, initialProxyBatch.size());
    }

    @Override
    public Iterator<R> iterator() {
        return new Iterator<R>() {
            private final Iterator<P> sourceIterator = source.iterator();
            private List<R> batch = initialBatch;
            private int idx;

            @Override
            public boolean hasNext() {
                return idx < batch.size() || sourceIterator.hasNext();
            }

            @Override
            public R next() {
                if (idx == batch.size()) {
                    idx = 0;
                    batch = load(nextBatch(sourceIterator));
                }

                if (idx >= batch.size()) {
                    throw new NoSuchElementException();
                }

                return batch.get(idx++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private List<R> load(final List<P> proxyBatch) {
        if (proxyBatch.isEmpty()) {
            return Collections.emptyList();
        }

        final List<R> result = Lists.newArrayListWithExpectedSize(proxyBatch.size());
        loader.load(proxyBatch, result);
        if (result.isEmpty()) {
            result.add(null);
        }

        return result;
    }

    private List<P> nextBatch(final Iterator<P> sourceIterator) {
        if (!sourceIterator.hasNext()) {
            return Collections.emptyList();
        }

        final List<P> result = Lists.newArrayListWithExpectedSize(prefetch);
        while (sourceIterator.hasNext() && result.size() < prefetch) {
            result.add(sourceIterator.next());
        }

        return result;
    }
}
