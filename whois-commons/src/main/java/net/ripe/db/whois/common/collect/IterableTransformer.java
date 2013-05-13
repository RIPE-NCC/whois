package net.ripe.db.whois.common.collect;

import java.util.*;

/**
 * helper to provide a simple way of mapping an Iterable<T> to Iterable<T> without any extra hoop
 */
public abstract class IterableTransformer<T> implements Iterable<T> {
    final Iterable<? extends T> wrap;
    Collection<T> head;

    public IterableTransformer(final Iterable<? extends T> wrap) {
        this.wrap = wrap;
        head = null;
    }

    /**
     * efficiently add extra headers, as in iterable elements at the beginning of the iterable
     * headers are not fed into apply() but passed down directly
     */
    public void setHeader(T... header) {
        head = Arrays.asList(header);
    }

    /**
     * efficiently add extra headers, as in iterable elements at the beginning of the iterable
     * headers are not fed into apply() but passed down directly
     */
    public void setHeader(Collection<T> header) {
        head = header;
    }

    /**
     * <tt>result</tt> is empty on call, should be filled with returned elements (or left empty).
     * Trying to add null elements will throw NullPointerException.
     */
    public abstract void apply(final T input, final Deque<T> result);

    @Override
    public Iterator<T> iterator() {
        return new IteratorTransformer(head);
    }

    private final class IteratorTransformer implements Iterator<T> {
        final Iterator<? extends T> it = wrap.iterator();

        final Deque<T> results;

        IteratorTransformer(Collection<T> header) {
            if (header != null) {
                results = new ArrayDeque<T>(header);
            } else {
                results = new ArrayDeque<T>();
            }
        }

        @Override
        public boolean hasNext() {
            while (results.isEmpty() && it.hasNext()) {
                apply(it.next(), results);
            }
            return !results.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return results.pop();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
