package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.Dummifier;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import javax.annotation.CheckForNull;
import java.util.Set;

interface DecorationStrategy {
    static class None implements DecorationStrategy {
        @Override
        public RpslObject decorate(final RpslObject object) {
            return object;
        }
    }

    static class Dummify implements DecorationStrategy {
        private static final int VERSION = 3;
        private final Dummifier dummifier;
        private final Set<ObjectType> writtenPlaceHolders = Sets.newHashSet();

        public Dummify(final Dummifier dummifier) {
            this.dummifier = dummifier;
        }

        @Override
        public RpslObject decorate(final RpslObject object) {
            if (dummifier.isAllowed(VERSION, object)) {
                return dummifier.dummify(VERSION, object);
            }

            final ObjectType objectType = object.getType();
            if (writtenPlaceHolders.add(objectType)) {
                if (objectType.equals(ObjectType.ROLE)) {
                    return Dummifier.PLACEHOLDER_ROLE_OBJECT;
                } else {
                    return Dummifier.PLACEHOLDER_PERSON_OBJECT;
                }
            }

            return null;
        }
    }

    @CheckForNull
    RpslObject decorate(RpslObject object);
}
