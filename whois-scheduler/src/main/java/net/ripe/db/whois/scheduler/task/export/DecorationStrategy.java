package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.rpsl.DummifierNrtm;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.transform.FilterChangedFunction;
import net.ripe.db.whois.scheduler.task.autnum.LegacyAutnumReloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            //Here PERSON and ROLE objects will be ignored for VERSION 3
            if (dummifier.isAllowed(VERSION, object)) {
                return dummifier.dummify(VERSION, object);
            }

            final ObjectType objectType = object.getType();

            //Just PERSON and ROLE objects
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

    @CheckForNull
    RpslObject decorate(RpslObject object);
}
