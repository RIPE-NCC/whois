package net.ripe.db.whois.common.domain.ip;

import com.google.common.net.InetAddresses;
import com.google.common.primitives.Longs;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

// TODO: [AH] make parseXXXAddress(), parseXXXPrefix() methods, refer to the specific ones from the rest of the code, depending on expectations
public class Ipv6Resource extends IpInterval<Ipv6Resource> implements Comparable<Ipv6Resource> {
    public static final String IPV6_REVERSE_DOMAIN = ".ip6.arpa";

    private static final Logger LOGGER = LoggerFactory.getLogger(Ipv4Resource.class);

    private static final int LONG_BITCOUNT = 64;
    private static final int IPV6_BITCOUNT = 128;

    public static final Ipv6Resource MAX_RANGE = new Ipv6Resource(0, 0, 0);

    private static final BigInteger MASK = BigInteger.ONE.shiftLeft(LONG_BITCOUNT).subtract(BigInteger.ONE);

    private static final Pattern REVERSE_PATTERN = Pattern.compile("(?i)^[0-9a-f](?:[.][0-9a-f]){0,31}$");

    private final long beginMsb;
    private final long beginLsb;
    private final long endMsb;
    private final long endLsb;

    private Ipv6Resource(BigInteger begin, int len) {
        this(msb(begin), lsb(begin), len);
    }

    public static long lsb(BigInteger begin) {
        return begin.and(MASK).longValue();
    }

    public static long msb(BigInteger begin) {
        return begin.shiftRight(LONG_BITCOUNT).longValue();
    }

    public static Ipv6Resource parseFromStrings(final String msb, final String lsb, final int len) {
        final BigInteger begin = new BigInteger(msb).shiftLeft(LONG_BITCOUNT).add(new BigInteger(lsb));
        return new Ipv6Resource(begin, len);
    }

    private Ipv6Resource(long msb, long lsb, int prefixLength) {
        // Special cases -- short circuit
        if (prefixLength == 0) {
            beginMsb = 0;
            beginLsb = 0;
            endMsb = ~0;
            endLsb = ~0;
            return;
        } else if (prefixLength == LONG_BITCOUNT) {
            beginMsb = msb;
            beginLsb = 0;
            endMsb = msb;
            endLsb = ~0;
            return;
        } else if (prefixLength == IPV6_BITCOUNT) {
            beginMsb = msb;
            beginLsb = lsb;
            endMsb = msb;
            endLsb = lsb;
            return;
        }

        long mask = (1L << (LONG_BITCOUNT - (prefixLength % LONG_BITCOUNT))) - 1;

        if (prefixLength < LONG_BITCOUNT) {
            beginLsb = 0;
            endLsb = ~0;

            beginMsb = msb & ~mask;
            endMsb = msb | mask;
        } else {
            beginMsb = msb;
            endMsb = msb;

            beginLsb = lsb & ~mask;
            endLsb = lsb | mask;
        }
    }

    public Ipv6Resource(BigInteger begin, BigInteger end) {
        Validate.isTrue(begin.bitLength() <= IPV6_BITCOUNT, "Begin out of range: ", begin);
        Validate.isTrue(end.bitLength() <= IPV6_BITCOUNT, "End out of range: ", end);

        beginMsb = msb(begin);
        beginLsb = lsb(begin);
        endMsb = msb(end);
        endLsb = lsb(end);

        Validate.isTrue(compare(beginMsb, beginLsb, endMsb, endLsb) <= 0, "Begin must be before end");
    }

    public static Ipv6Resource parse(InetAddress ipv6Address) {
        long[] res = byteArrayToLongArray(ipv6Address.getAddress());
        return new Ipv6Resource(res[0], res[1], IPV6_BITCOUNT);
    }

    private static Ipv6Resource parse(InetAddress ipv6Address, int prefixLength) {
        long[] res = byteArrayToLongArray(ipv6Address.getAddress());
        return new Ipv6Resource(res[0], res[1], prefixLength);
    }

    public static Ipv6Resource parse(final CIString prefixOrAddress) {
        return parse(prefixOrAddress.toString());
    }

    public static Ipv6Resource parse(final String prefixOrAddress) {
        Validate.notNull(prefixOrAddress);
        final String trimmedPrefixOrAddress = prefixOrAddress.trim();
        int slashIndex = trimmedPrefixOrAddress.indexOf('/');

        if (slashIndex > 0) {
            int prefixLength = Integer.parseInt(trimmedPrefixOrAddress.substring(slashIndex + 1));
            if (prefixLength < 0 || prefixLength > 128) {
                throw new IllegalArgumentException("Invalid prefix length: "+prefixOrAddress);
            }
            return parse(InetAddresses.forString(trimmedPrefixOrAddress.substring(0, slashIndex)), prefixLength);
        } else {
            return parse(InetAddresses.forString(trimmedPrefixOrAddress), IPV6_BITCOUNT);
        }
    }

    public static Ipv6Resource parseReverseDomain(final String address) {
        Validate.notEmpty(address, "Address cannot be empty");
        String cleanAddress = removeTrailingDot(address.trim()).toLowerCase();

        Validate.isTrue(cleanAddress.endsWith(IPV6_REVERSE_DOMAIN), "Invalid reverse domain: ", address);

        cleanAddress = cleanAddress.substring(0, cleanAddress.length() - IPV6_REVERSE_DOMAIN.length());

        Validate.isTrue(REVERSE_PATTERN.matcher(cleanAddress).matches(), "Invalid reverse domain: ", address);

        StringBuilder builder = new StringBuilder();
        int netmask = 0;

        for (int index = cleanAddress.length() - 1; index >= 0; index -= 2) {
            builder.append(cleanAddress.charAt(index));

            netmask += 4;

            if (netmask % 16 == 0 && index > 0) {
                builder.append(':');
            }
        }

        if (netmask % 16 != 0) {
            for (int i = 4 - ((netmask / 4) % 4); i > 0; i--) {
                builder.append('0');
            }
        }

        if (netmask <= 112) {
            builder.append("::");
        }

        builder.append('/');
        builder.append(netmask);

        return parse(builder.toString());
    }

    @Override
    public final AttributeType getAttributeType() {
        return AttributeType.INET6NUM;
    }

    public BigInteger begin() {
        return twoUnsignedLongToBigInteger(beginMsb, beginLsb);
    }

    public BigInteger end() {
        return twoUnsignedLongToBigInteger(endMsb, endLsb);
    }

    private static long[] byteArrayToLongArray(byte[] address) {
        Validate.isTrue(address.length == 16, "Address has to be 16 bytes long");
        long[] res = new long[2];

        for (int i = 0; i < 16; i++) {
            res[i >>> 3] = (res[i >>> 3] << 8) + (address[i] & 0xFF);
        }
        return res;
    }

    private static byte[] toByteArray(long msb, long lsb) {
        byte[] data = new byte[16];

        for (int i = 0; i < 16; i++) {
            data[i] = (byte) ((i < 8 ? msb : lsb) >>> (8 * (7 - (i % 8))) & 0xFF);
        }
        return data;
    }

    private static BigInteger twoUnsignedLongToBigInteger(long msb, long lsb) {
        return new BigInteger(1, toByteArray(msb, lsb));
    }

    static int compare(long aMsb, long aLsb, long bMsb, long bLsb) {
        if (aMsb == bMsb) {
            if (aLsb == bLsb) {
                return 0;
            }
            if ((aLsb < bLsb) ^ (aLsb < 0) ^ (bLsb < 0)) {
                return -1;
            }
        } else if ((aMsb < bMsb) ^ (aMsb < 0) ^ (bMsb < 0)) {
            return -1;
        }

        return 1;
    }

    @Override
    public int compareTo(Ipv6Resource that) {
        int comp = compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb);
        if (comp == 0) {
            comp = compare(that.endMsb, that.endLsb, endMsb, endLsb);
        }
        return comp;
    }

    @Override
    public boolean contains(Ipv6Resource that) {
        return compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb) <= 0
                && compare(endMsb, endLsb, that.endMsb, that.endLsb) >= 0;
    }

    @Override
    public boolean intersects(Ipv6Resource that) {
        return (compare(beginMsb, beginLsb, that.beginMsb, that.beginLsb) >= 0 && compare(beginMsb, beginLsb, that.endMsb, that.endLsb) <= 0)
                || (compare(endMsb, endLsb, that.beginMsb, that.beginLsb) >= 0 && compare(endMsb, endLsb, that.endMsb, that.endLsb) <= 0)
                || contains(that);
    }

    @Override
    public Ipv6Resource singletonIntervalAtLowerBound() {
        return new Ipv6Resource(beginMsb, beginLsb, IPV6_BITCOUNT);
    }

    @Override
    public int compareUpperBound(Ipv6Resource that) {
        return compare(endMsb, endLsb, that.endMsb, that.endLsb);
    }

    @Override
    public InetAddress beginAsInetAddress() {
        byte[] bytes = new byte[16];
        System.arraycopy(Longs.toByteArray(beginMsb), 0, bytes, 8, 8);
        System.arraycopy(Longs.toByteArray(beginLsb), 0, bytes, 0, 8);
        try {
            return Inet6Address.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // this will never happen
            return null;
        }
    }

    @Override
    public int getPrefixLength() {
        int res;
        if (beginMsb == endMsb) {
            res = LONG_BITCOUNT + Long.bitCount(~(beginLsb ^ endLsb));
        } else {
            res = Long.bitCount(~(beginMsb ^ endMsb));
        }
        return res;
    }

    @Override
    public String toString() {
        int prefixLength = getPrefixLength();
        int[] nibbles = new int[8];
        int maxZeroIndex = -1, maxZeroCount = -1;
        int actZeroIndex = -1, actZeroCount = 0;
        StringBuilder sb = new StringBuilder();

        // convert to nibbles, mark location of longest nibble
        for (int i = 0; i < prefixLength; i += 16) {
            long act = (i < LONG_BITCOUNT) ? beginMsb : beginLsb;
            int remainingPrefix = prefixLength - i;
            int mask = 0xFFFF;

            if (remainingPrefix < 16) {
                mask &= ~((1 << (16 - remainingPrefix)) - 1);
            }

            nibbles[i >>> 4] = (int) (act >> (48 - (i & 63))) & mask;
        }

        // look for longest nibble location
        for (int i = 0; i < 8; i++) {
            if (nibbles[i] == 0) {
                if (actZeroIndex >= 0) {
                    actZeroCount++;
                } else {
                    actZeroIndex = i;
                    actZeroCount = 1;
                }
            } else {
                if (actZeroIndex >= 0) {
                    if (actZeroCount >= maxZeroCount) {
                        maxZeroCount = actZeroCount;
                        maxZeroIndex = actZeroIndex;
                    }
                }
                actZeroIndex = -1;
            }
        }

        if ((actZeroIndex >= 0) && (actZeroCount >= maxZeroCount)) {
            maxZeroCount = actZeroCount;
            maxZeroIndex = actZeroIndex;
        }

        // convert to string
        for (int i = 0; i < 8; i++) {
            if (maxZeroIndex == i) {
                if (i == 0) {
                    sb.append("::");
                } else {
                    sb.append(':');
                }
                i += maxZeroCount - 1;
            } else {
                sb.append(Integer.toHexString(nibbles[i]));
                if (i < 7) {
                    sb.append(':');
                }
            }
        }

        sb.append('/').append(prefixLength);

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (beginLsb ^ (beginLsb >>> 32));
        result = prime * result + (int) (beginMsb ^ (beginMsb >>> 32));
        result = prime * result + (int) (endLsb ^ (endLsb >>> 32));
        result = prime * result + (int) (endMsb ^ (endMsb >>> 32));
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
        Ipv6Resource other = (Ipv6Resource) obj;
        return compareTo(other) == 0;
    }

    public static Ipv6Resource parseIPv6Resource(String resource) {
        try {
            return Ipv6Resource.parse(resource.trim());
        } catch (RuntimeException e) {
            LOGGER.debug("Parsing {}", resource, e);
        }
        return null;
    }
}
