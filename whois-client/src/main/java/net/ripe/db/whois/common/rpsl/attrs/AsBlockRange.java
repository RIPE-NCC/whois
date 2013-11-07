package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.exception.AsBlockParseException;
import net.ripe.db.whois.common.exception.InvalidAsBlockRangeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AsBlockRange {

    private static final Pattern AS_BLOCK_PATTERN = Pattern.compile("^[Aa][Ss](\\d+)\\s*-\\s*[Aa][Ss](\\d+)$");

    private final long begin;

    private final long end;

    private AsBlockRange(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public static AsBlockRange parse(final String range) {
        final Matcher match = AS_BLOCK_PATTERN.matcher(range);
        if (match.find()) {
            long begin = Long.valueOf(match.group(1));
            long end = Long.valueOf(match.group(2));

            if (end < begin) {
                throw new InvalidAsBlockRangeException(end + " < " + begin);
            }

            return new AsBlockRange(begin, end);
        }

        throw new AsBlockParseException("invalid asblock");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        final AsBlockRange that = (AsBlockRange) o;
        return begin == that.begin && end == that.end;
    }

    @Override
    public int hashCode() {
        int result = (int) (begin ^ (begin >>> 32));
        result = 31 * result + (int) (end ^ (end >>> 32));
        return result;
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
}
