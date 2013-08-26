package net.ripe.db.whois.common.domain;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

// TODO: [AH] make parseXXXAddress(), parseXXXPrefix(), parseXXXRange() methods, refer to the specific ones from the rest of the code, depending on expectations

/**
 * Efficient representation of an IPv4 address range. Internally IPv4 addresses
 * are stored as signed 32-bit <code>int</code>s. Externally they are
 * represented as <code>long</code>s to avoid issues with the sign-bit.
 */
public final class Ipv4Resource extends IpInterval<Ipv4Resource> implements Comparable<Ipv4Resource> {
    public static final String IPV4_REVERSE_DOMAIN = ".in-addr.arpa";

    private static final Logger LOGGER = LoggerFactory.getLogger(Ipv4Resource.class);

    private static final Splitter IPV4_TEXT_SPLITTER = Splitter.on('.');

    private static final long MINIMUM_NUMBER = 0;
    private static final long MAXIMUM_NUMBER = (1L << 32) - 1;

    /**
     * The IPv4 interval that includes all IPv4 addresses (0.0.0.0/0 in CIDR
     * notation).
     */
    public static final Ipv4Resource MAX_RANGE = new Ipv4Resource(MINIMUM_NUMBER, MAXIMUM_NUMBER);

    private static final Splitter SPLIT_ON_DOT = Splitter.on('.');
    private static final Pattern OCTET_PATTERN = Pattern.compile("^(?:[0-9]|[1-9][0-9]+)(?:-(?:[0-9]|[1-9][0-9]+)+)?$");

    private final int begin;
    private final int end;

    private Ipv4Resource(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Constructs a new IPv4 interval with the specified begin and end (both
     * inclusive).
     *
     * @param begin the first IPv4 address in this address range (inclusive).
     * @param end   the last IPv4 address in this address range (inclusive).
     * @throws IllegalArgumentException if the start or end addresses are invalid or if the start
     *                                  address is greater than the end address.
     */
    public Ipv4Resource(long begin, long end) {
        if (begin > end) {
            throw new IllegalArgumentException("Begin: " + begin + " not before End: " + end);
        }
        if (begin < MINIMUM_NUMBER) {
            throw new IllegalArgumentException("Begin: " + begin + " out of range");
        }
        if (end > MAXIMUM_NUMBER) {
            throw new IllegalArgumentException("End: " + end + " out of range");
        }

        this.begin = (int)begin;
        this.end = (int)end;
    }

    public Ipv4Resource(InetAddress inetAddress) {
        if (!(inetAddress instanceof Inet4Address)) {
            throw new IllegalArgumentException("Not an IPv4 address: "+inetAddress);
        }
        byte[] addressArray = inetAddress.getAddress();
        int address  = addressArray[3] & 0xFF;
        address |= ((addressArray[2] << 8) & 0xFF00);
        address |= ((addressArray[1] << 16) & 0xFF0000);
        address |= ((addressArray[0] << 24) & 0xFF000000);
        begin = address;
        end = address;
    }

    public static Ipv4Resource parse(final CIString resource) {
        return parse(resource.toString());
    }

    public static Ipv4Resource parse(final String resource) {
        final int indexOfSlash = resource.indexOf('/');
        if (indexOfSlash >= 0) {
            int begin = textToNumericFormat(resource.substring(0, indexOfSlash).trim());
            int prefixLength = Integer.parseInt(resource.substring(indexOfSlash+1, resource.length()).trim());
            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("prefix length "+prefixLength+" is invalid");
            }
            int mask = (int)((1L << (32-prefixLength)) - 1);
            int end = begin | mask;
            begin = begin & ~mask;
            return new Ipv4Resource(begin, end);
        }

        final int indexOfDash = resource.indexOf('-');
        if (indexOfDash >= 0) {
            long begin = ((long)textToNumericFormat(resource.substring(0, indexOfDash).trim())) & 0xffffffffL;
            long end = ((long)textToNumericFormat(resource.substring(indexOfDash+1, resource.length()).trim())) & 0xffffffffL;
            return new Ipv4Resource(begin, end);
        }

        return new Ipv4Resource(InetAddresses.forString(resource));
    }

    public static Ipv4Resource parseIPv4Resource(String resource) {
        try {
            return parse(resource.trim());
        } catch (RuntimeException e) {
            LOGGER.debug("Parsing {}", resource, e);
        }
        return null;
    }

    public static Ipv4Resource parsePrefixWithLength(final long prefix, final int prefixLength) {
        long mask = (1L << (32 - prefixLength)) - 1;
        return new Ipv4Resource((prefix & ~mask) & 0xFFFFFFFFL, (prefix | mask) & 0xFFFFFFFFL);
    }

    public static Ipv4Resource parseReverseDomain(final String address) {
        Validate.notEmpty(address);
        String cleanAddress = removeTrailingDot(address.trim());

        Validate.isTrue(cleanAddress.toLowerCase().endsWith(IPV4_REVERSE_DOMAIN), "Invalid reverse domain: ", address);

        cleanAddress = cleanAddress.substring(0, cleanAddress.length() - IPV4_REVERSE_DOMAIN.length());

        final ArrayList<String> reverseParts = Lists.newArrayList(SPLIT_ON_DOT.split(cleanAddress));
        Validate.isTrue(!reverseParts.isEmpty() && reverseParts.size() <= 4, "Reverse address doesn't have between 1 and 4 octets: ", address);

        final List<String> parts = Lists.reverse(reverseParts);

        boolean hasDash = false;
        if (cleanAddress.contains("-")) {
            Validate.isTrue(reverseParts.size() == 4 && reverseParts.get(0).contains("-"), "Dash notation not in 4th octet: ", address);
            Validate.isTrue(cleanAddress.indexOf('-') == cleanAddress.lastIndexOf('-'), "Only one dash allowed: ", address);
            hasDash = true;
        }

        final StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append('.');
            }
            Validate.isTrue(OCTET_PATTERN.matcher(part).matches(), "Invalid octet: ", part);
            // [EB]: Check for A-B && B <= A ?

            builder.append(part);
        }

        if (hasDash) {
            // [EB]: Some magic here, copy the 'start' of the string before the '-'
            // to get an expanded range: [1.1.1.]1-2 becomes 1.1.1.1-[1.1.1.]2
            int range = builder.indexOf("-");
            if (range != -1) {
                builder.insert(range + 1, builder.substring(0, builder.lastIndexOf(".") + 1));
            }
        }

        if (parts.size() < 4) {
            builder.append('/').append(parts.size() * 8);
        }

        return parse(builder.toString());
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.INETNUM;
    }

    /**
     * @return the start address as "unsigned" <code>long</code>.
     */
    public long begin() {
        return ((long) begin) & 0xffffffffL;
    }

    /**
     * @return the end address as "unsigned" <code>long</code>.
     */
    public long end() {
        return ((long) end) & 0xffffffffL;
    }

    @Override
    public boolean contains(Ipv4Resource that) {
        return this.begin() <= that.begin() && this.end() >= that.end();
    }

    @Override
    public boolean intersects(Ipv4Resource that) {
        return (this.begin() >= that.begin() && this.begin() <= that.end())
                || (this.end() >= that.begin() && this.end() <= that.end())
                || (that.begin() >= this.begin() && that.begin() <= this.end());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + begin;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Ipv4Resource that = (Ipv4Resource) obj;
        return this.begin == that.begin && this.end == that.end;
    }

    /** Only if x != 0 */
    private static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    private static String numericToTextFormat(int src) {
        return (src>>24 & 0xff) + "." + (src>>16 & 0xff) + "." + (src>>8 & 0xff) + "." + (src & 0xff);
    }

    private static int textToNumericFormat(String src) {
        int result = 0;
        Iterator<String> it = IPV4_TEXT_SPLITTER.split(src).iterator();
        for (int octet = 0; octet < 4; octet++) {
            result <<= 8;
            int value = it.hasNext() ? Integer.parseInt(it.next()) : 0;
            if (value < 0 || value > 255) {
                throw new IllegalArgumentException(src+" is not a valid ipv4 address");
            }
            result |= value & 0xff;
        }
        if (it.hasNext()) {
            throw new IllegalArgumentException(src+" has more than 4 octets");
        }
        return result;
    }

    @Override
    public String toString() {
        int prefixLength = getPrefixLength();
        if (prefixLength < 0) {
            return toRangeString();
        } else {
            return numericToTextFormat(begin) + "/" + prefixLength;
        }
    }

    public String toRangeString() {
        return numericToTextFormat(begin) + " - " + numericToTextFormat(end);
    }

    /**
     * Orders on {@link #begin} ASCENDING and {@link #end} DESCENDING. This puts
     * less-specific ranges before more-specific ranges.
     */
    @Override
    public int compareTo(Ipv4Resource that) {
        if (this.begin() < that.begin()) {
            return -1;
        } else if (this.begin() > that.begin()) {
            return 1;
        } else if (that.end() < this.end()) {
            return -1;
        } else if (that.end() > this.end()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public Ipv4Resource singletonIntervalAtLowerBound() {
        return new Ipv4Resource(this.begin(), this.begin());
    }

    @Override
    public int compareUpperBound(Ipv4Resource that) {
        long thisEnd = this.end();
        long thatEnd = that.end();
        return thisEnd < thatEnd ? -1 : thisEnd > thatEnd ? 1 : 0;
    }

    @Override
    public InetAddress beginAsInetAddress() {
        return InetAddresses.fromInteger(begin);
    }

    @Override
    public int getPrefixLength() {
        // see if we can convert to nice prefix
        if (isPowerOfTwo(end - begin + 1)) {
            return 32-Integer.numberOfTrailingZeros(end - begin + 1);
        } else {
            return -1;
        }
    }
}
