package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.DummifierLegacy;
import net.ripe.db.whois.common.rpsl.DummifierCurrent;
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

    static class DummifyLegacy implements DecorationStrategy {
        private static final int VERSION = 3;
        private final DummifierLegacy dummifier;
        private final Set<ObjectType> writtenPlaceHolders = Sets.newHashSet();

        public DummifyLegacy(final DummifierLegacy dummifier) {
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
                    return DummifierLegacy.PLACEHOLDER_ROLE_OBJECT;
                } else {
                    return DummifierLegacy.PLACEHOLDER_PERSON_OBJECT;
                }
            }

            return null;
        }
    }

    static class DummifyCurrent implements DecorationStrategy {
        private static final int VERSION = 3;
        private final DummifierCurrent dummifier;

        public DummifyCurrent(final DummifierCurrent dummifier) {
            this.dummifier = dummifier;
        }

        @Override
        public RpslObject decorate(final RpslObject object) {
            if (dummifier.isAllowed(VERSION, object)) {
                return dummifier.dummify(VERSION, object);
            }
            return null;
        }
    }

    @CheckForNull
    RpslObject decorate(RpslObject object);
}
