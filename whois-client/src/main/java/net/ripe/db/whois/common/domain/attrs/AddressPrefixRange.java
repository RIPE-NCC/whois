package net.ripe.db.whois.common.domain.attrs;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ip.IpInterval;
import net.ripe.db.whois.common.domain.ip.Ipv4Resource;
import net.ripe.db.whois.common.domain.ip.Ipv6Resource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AddressPrefixRange {
    private static final Pattern ADDRESS_PREFIX_RANGE_PATTERN = Pattern.compile("^(.*)/(\\d+)(.*)$");

    private final String value;
    private final IpInterval ipInterval;
    private final RangeOperation rangeOperation;

    private AddressPrefixRange(final String value, final IpInterval ipInterval, final RangeOperation rangeOperation) {
        this.value = value;
        this.ipInterval = ipInterval;
        this.rangeOperation = rangeOperation;
    }

    public IpInterval getIpInterval() {
        return ipInterval;
    }

    RangeOperation getRangeOperation() {
        return rangeOperation;
    }

    @Override
    public String toString() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public BoundaryCheckResult checkWithinBounds(final IpInterval bounds) {
        final BoundaryCheckResult boundaryCheckResult = checkType(bounds);
        if (boundaryCheckResult != null) {
            return boundaryCheckResult;
        }

        if (!bounds.contains(ipInterval)) {
            return BoundaryCheckResult.NOT_IN_BOUNDS;
        }

        return BoundaryCheckResult.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public BoundaryCheckResult checkRange(final IpInterval contained) {
        final BoundaryCheckResult boundaryCheckResult = checkType(contained);
        if (boundaryCheckResult != null) {
            return boundaryCheckResult;
        }

        if (!ipInterval.contains(contained) || rangeOperation.getN() > contained.getPrefixLength() || rangeOperation.getM() < contained.getPrefixLength()) {
            return BoundaryCheckResult.NOT_IN_BOUNDS;
        }

        return BoundaryCheckResult.SUCCESS;
    }

    private BoundaryCheckResult checkType(final IpInterval other) {
        if (ipInterval instanceof Ipv4Resource && !(other instanceof Ipv4Resource)) {
            return BoundaryCheckResult.IPV4_EXPECTED;
        }

        if (ipInterval instanceof Ipv6Resource && !(other instanceof Ipv6Resource)) {
            return BoundaryCheckResult.IPV6_EXPECTED;
        }

        return null;
    }

    public static AddressPrefixRange parse(final CIString value) {
        return parse(value.toString());
    }

    public static AddressPrefixRange parse(final String value) {
        final Matcher matcher = ADDRESS_PREFIX_RANGE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final int range = Integer.parseInt(matcher.group(2));
        final String address = matcher.group(1);

        final IpInterval ipInterval = getIpInterval(address);
        if (ipInterval == null) {
            throw new AttributeParseException("Invalid address: " + address, value);
        }

        final int maxRange = ipInterval instanceof Ipv4Resource ? 32 : 128;
        if (range > maxRange) {
            throw new AttributeParseException("Invalid range: " + range, value);
        }

        final RangeOperation rangeOperation = RangeOperation.parse(matcher.group(3), range, maxRange);
        return new AddressPrefixRange(value, IpInterval.parse(address + "/" + range), rangeOperation);
    }

    private static IpInterval getIpInterval(final String value) {
        try {
            return IpInterval.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static enum BoundaryCheckResult {
        SUCCESS,
        IPV4_EXPECTED,
        IPV6_EXPECTED,
        NOT_IN_BOUNDS,
    }
}
