package net.ripe.db.rndreference;

import com.google.common.collect.ImmutableMap;
import com.sun.istack.NotNull;
import net.ripe.db.whois.internal.api.rnd.domain.RpslObjectKey;
import org.joda.time.Interval;

import java.util.Map;

public class RpslObjectTimeLine {
    public static final String KEY_SEPERATOR = "::";
    private RpslObjectKey key;

    private Map<Interval, RevisionWithReferences> rplsObjectIntervals;

    public RpslObjectTimeLine(@NotNull final String pkey, @NotNull final Integer objectType) {
        this.key = new RpslObjectKey(objectType, pkey);
    }

    public String getKey() {
        return key.getPkey();
    }

    public Integer getObjectType() {
        return key.getObjectType();
    }

    public Map<Interval, RevisionWithReferences> getRpslObjectIntervals() {
        return ImmutableMap.copyOf(rplsObjectIntervals);
    }

    public void setRplsObjectIntervals(final Map<Interval, RevisionWithReferences> rplsObjectIntervals) {
        this.rplsObjectIntervals = rplsObjectIntervals;
    }
}
