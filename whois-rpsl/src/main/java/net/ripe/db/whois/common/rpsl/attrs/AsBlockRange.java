package net.ripe.db.whois.common.rpsl.attrs;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AsBlockRange {

    private static final Pattern AS_BLOCK_PATTERN = Pattern.compile("^[Aa][Ss](\\d+)\\s*-\\s*[Aa][Ss](\\d+)$");
    private static final String AS_PREFIX_FORMAT = "AS%s";

    private final long begin;

    private final long end;

    private AsBlockRange(final long begin, final long end) {
        this.begin = begin;
        this.end = end;
    }

    public static AsBlockRange parse(final String range) {
        final Matcher match = AS_BLOCK_PATTERN.matcher(range);
        if (match.find()) {
            long begin = Long.valueOf(match.group(1));
            long end = Long.valueOf(match.group(2));

            if (end < begin) {
                throw new AttributeParseException(end + " < " + begin, range);
            }

            return new AsBlockRange(begin, end);
        }

        throw new AttributeParseException("invalid asblock", range);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AsBlockRange that = (AsBlockRange) o;

        return Objects.equals(begin, that.begin) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }

    public boolean contains(final AsBlockRange asBlockRange) {
        return asBlockRange.begin >= begin && asBlockRange.end <= end;
    }

    public long getBegin() {
        return begin;
    }

    public long getEnd() {
        return end;
    }

    public String getEndWithPrefix() {
        return String.format(AS_PREFIX_FORMAT, end);
    }

    public String getBeginWithPrefix() {
        return String.format(AS_PREFIX_FORMAT, begin);
    }

    @Override
    public String toString() {
        return String.join("-", getBeginWithPrefix(), getEndWithPrefix());
    }
}
