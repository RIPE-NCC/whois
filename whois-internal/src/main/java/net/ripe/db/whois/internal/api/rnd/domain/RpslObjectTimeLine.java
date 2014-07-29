package net.ripe.db.whois.internal.api.rnd.domain;

import com.google.common.collect.ImmutableMap;
import com.sun.istack.NotNull;
import org.joda.time.Interval;

import java.util.Map;

public class RpslObjectTimeLine {
    public static final String KEY_SEPERATOR = "::";
    private RpslObjectKey key;

    private Map<Interval, RpslObjectWithReferences> rplsObjectIntervals;

    public RpslObjectTimeLine(@NotNull final String pkey, @NotNull final Integer objectType) {
        this.key = new RpslObjectKey(objectType, pkey);
    }

    public String getKey() {
        return key.getPkey();
    }

    public Integer getObjectType() {
        return key.getObjectType();
    }

    public Map<Interval, RpslObjectWithReferences> getRpslObjectIntervals() {
        return ImmutableMap.copyOf(rplsObjectIntervals);
    }

    public void setRplsObjectIntervals(final Map<Interval, RpslObjectWithReferences> rplsObjectIntervals) {
        this.rplsObjectIntervals = rplsObjectIntervals;
    }
}
