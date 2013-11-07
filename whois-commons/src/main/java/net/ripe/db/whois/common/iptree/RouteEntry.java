package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.domain.Interval;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteEntry<T extends Interval<T>> extends IpEntry<T> implements Comparable<RouteEntry<T>> {
    private static final Pattern PKEY_PATTERN = Pattern.compile("(?i)(.+)(AS\\d+)");

    private final String origin;

    protected RouteEntry(final T key, final int objectId, final String origin) {
        super(key, objectId);
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return super.equals(o) && origin.equals(((RouteEntry) o).origin);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + origin.hashCode();
    }

    @Override
    public int compareTo(final RouteEntry<T> other) {
        int result = getKey().compareUpperBound(other.getKey());

        if (result == 0) {
            result = origin.compareToIgnoreCase(other.origin);
        }

        return result;
    }

    static Matcher parsePkey(final String pkey) {
        final Matcher pkeyMatcher = PKEY_PATTERN.matcher(pkey);
        if (!pkeyMatcher.matches() || pkeyMatcher.groupCount() != 2) {
            throw new IllegalArgumentException("Invalid pkey: " + pkey);
        }

        return pkeyMatcher;
    }
}
