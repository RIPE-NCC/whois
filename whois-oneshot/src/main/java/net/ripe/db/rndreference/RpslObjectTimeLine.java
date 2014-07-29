package net.ripe.db.rndreference;

import com.google.common.collect.ImmutableMap;
import com.sun.istack.NotNull;
import org.joda.time.Interval;

import java.util.Map;

public class RpslObjectTimeLine {
    private String key;
    public static final String KEY_SEPERATOR = "::";

    private Map<Interval, RpslObjectWithReferences> rplsObjectIntervals;

    public RpslObjectTimeLine(@NotNull final String key) {
        this.key = key;
    }
    public RpslObjectTimeLine(@NotNull final String pkey, @NotNull final Integer objectType) {
        this.key = objectType.toString() + KEY_SEPERATOR + pkey;

    }

    public String getKey() {
        return key;
    }

    public Map<Interval, RpslObjectWithReferences> getRpslObjectIntervals() {
        return ImmutableMap.copyOf(rplsObjectIntervals);
    }

    public void setRplsObjectIntervals(final Map<Interval, RpslObjectWithReferences> rplsObjectIntervals) {
        this.rplsObjectIntervals = rplsObjectIntervals;
    }
}
