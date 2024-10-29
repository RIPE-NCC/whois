package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;

import javax.annotation.CheckForNull;
import java.util.Set;

public interface DecorationStrategy {
    class None implements DecorationStrategy {
        @Override
        public RpslObject decorate( final RpslObject object) {
            return new FilterChangedFunction().apply(object);
        }
    }

    class DummifySplitFiles implements DecorationStrategy {
        private static final int VERSION = 3;
        private final DummifierNrtm dummifier;
        private final Set<ObjectType> writtenPlaceHolders = Sets.newHashSet();

        public DummifySplitFiles(final DummifierNrtm dummifier) {
            this.dummifier = dummifier;
        }

        @Override
        public RpslObject decorate(final RpslObject object) {
            if (dummifier.isAllowed(VERSION, object) && !isPlaceHolder(object)) {
                return dummifier.dummify(VERSION, object);
            }

            final ObjectType objectType = object.getType();

            //Just PERSON and ROLE without abuseMailbox objects
            if (writtenPlaceHolders.add(objectType)) {
                if (objectType.equals(ObjectType.PERSON)) {
                    return DummifierNrtm.getPlaceholderPersonObject();
                }
                if (objectType.equals(ObjectType.ROLE)) {
                    return DummifierNrtm.getPlaceholderRoleObject();
                }
            }
            return null;
        }
    }

    private static boolean isPlaceHolder(RpslObject object) {
        return object.getType().equals(ObjectType.ROLE) && object.getKey().equals(DummifierNrtm.getPlaceholderRoleObject().getKey());
    }

    @CheckForNull
    RpslObject decorate(RpslObject object);
}
