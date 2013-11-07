package net.ripe.db.whois.common.etree;

import net.ripe.db.whois.common.domain.ip.Interval;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown to indicate that an attempt was made to insert an interval that would intersect with its siblings.
 */
public class IntersectingIntervalException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private final Interval<?> interval;

    private final List<? extends Interval<?>> intersections;

    public IntersectingIntervalException(Interval<?> interval, List<? extends Interval<?>> intersections) {
        super(String.format("%s intersects with existing siblings %s", interval, intersections));
        this.interval = interval;
        this.intersections = new ArrayList<>(intersections);
    }

    /**
     * @return the interval that intersects with existing intervals.
     */
    public Interval<?> getInterval() {
        return interval;
    }

    /**
     * @return the existing intervals that intersect with the interval being added.
     */
    public List<? extends Interval<?>> getIntersections() {
        return intersections;
    }
}
