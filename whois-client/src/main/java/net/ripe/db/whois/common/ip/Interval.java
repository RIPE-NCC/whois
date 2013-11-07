package net.ripe.db.whois.common.ip;

/**
 * An interval with a lower-bound and upper-bound. Both bounds are considered to
 * be <em>inclusive</em>.
 * <p/>
 * Implementations of this interface must be immutable and must correctly
 * override {@link Object#equals(Object)} and {@link Object#hashCode()} based on
 * the bounds.
 *
 * @param <K> the interval type
 */
public interface Interval<K extends Interval<K>> {

    /**
     * Tests if this interval contains <code>that</code>. Note that if two
     * intervals are <em>equal</em>, they also contain each other (and vice
     * versa). An interval always contains itself.
     *
     * @param that the interval to test for containment
     * @return true if <code>this</code> contains <code>that</code> interval
     */
    boolean contains(K that);

    /**
     * Tests if these two intervals intersect. Two intervals intersect if there
     * exists a point which is contained within both intervals. An interval
     * always intersects itself.
     *
     * @param that the other interval to test for intersection
     * @return true if <code>this</code> interval intersects <code>that</code>
     *         interval
     */
    boolean intersects(K that);

    /**
     * Copies this interval into a new interval that has both its lower and
     * upper-bound set to the original's lower-bound. This is used to be able to
     * compare an interval's lower-bound with another interval's upper-bound.
     * <p/>
     * <pre>
     *   Interval a = ...
     *   Interval b = ...
     *   if (a.singletonIntervalAtLowerBound().compareUpperBoundTo(b) < 0) ...
     * </pre>
     *
     * @return a new interval that has both its lower- and upper-bound set to
     *         this interval's lower-bound
     */
    K singletonIntervalAtLowerBound();

    /**
     * Compare two intervals based on their upper-bounds. This is used by the
     * {@link NestedIntervalMap} implementation to quickly find two potentially
     * intersecting intervals by ordering intervals on the upper-bound and
     * searching based on the lower-bound.
     *
     * @param that the interval to compare the upper-bound with
     * @return &lt;0 if this upper-bound is less than that upper-bound,<br> =0 if
     *         this upper-bound equals that upper-bound,<br> &gt;0 if this
     *         upper-bound is greater than that upper-bound
     */
    int compareUpperBound(K that);
}
