package net.ripe.db.whois.common.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.domain.Identifiable;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Collection;

public final class CollectionHelper {
    private static final Function<Identifiable, Integer> IDENTIFIABLE_TO_INTEGER = new Function<Identifiable, Integer>() {
        @Override
        public Integer apply(Identifiable input) {
            return input.getObjectId();
        }
    };

    private CollectionHelper() {
    }

    public static <T> T uniqueResult(final Collection<T> c) {
        switch (c.size()) {
            case 0:
                return null;
            case 1:
                return c.iterator().next();
            default:
                throw new IllegalStateException("Unexpected number of elements in collection: " + c.size());
        }
    }

    public static Iterable<Integer> identifiablesToIds(final Iterable<? extends Identifiable> identifiables) {
        return Iterables.transform(identifiables, IDENTIFIABLE_TO_INTEGER);
    }

    // TODO: [AH] result is wrapped by 3 iterable wrappers in this method - optimize!
    public static Iterable<ResponseObject> iterateProxy(final ProxyLoader<Integer, RpslObject> rpslObjectLoader, final Iterable<? extends Identifiable> identifiables) {
        final Iterable<Integer> ids = identifiablesToIds(identifiables);
        final ProxyIterable<Integer, ? extends ResponseObject> rpslObjects = new ProxyIterable<Integer, RpslObject>(ids, rpslObjectLoader, 100);
        return (Iterable<ResponseObject>) Iterables.filter(rpslObjects, Predicates.notNull());
    }

    public static<T> boolean containsType(T[] array, Class type) {
        if (array == null) return false;

        for (int i = 0; i < array.length; i++) {
            if (type.isAssignableFrom(array[i].getClass())) {
                return true;
            }
        }

        return false;
    }
}
