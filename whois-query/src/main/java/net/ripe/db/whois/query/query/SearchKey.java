package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4RouteEntry;
import net.ripe.db.whois.common.iptree.Ipv6RouteEntry;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

class SearchKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchKey.class);
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private String value;

    private IpInterval<?> ipKey;
    private IpInterval<?> ipKeyReverse;

    private String origin;

    private AsBlockRange asBlockRange;
    private boolean parsedAsBlockRange;

    SearchKey(final String value) {
        final String cleanValue = WHITESPACE_PATTERN.matcher(value.trim()).replaceAll(" ");

        try {
            ipKey = IpInterval.parse(cleanValue);
            this.value = ipKey instanceof Ipv4Resource ? ((Ipv4Resource) ipKey).toRangeString() : ipKey.toString();
            return;
        } catch (RuntimeException e) {
            LOGGER.debug(e.getMessage(), e);
        }

        try {
            // TODO: [AH] route parsing should be extracted from iptrees, same way as Ipv4/6Resource
            if (cleanValue.indexOf(':') == -1) {
                final Ipv4RouteEntry routeEntry = Ipv4RouteEntry.parse(cleanValue, 0);
                ipKey = routeEntry.getKey();
                origin = routeEntry.getOrigin();
            } else {
                final Ipv6RouteEntry routeEntry = Ipv6RouteEntry.parse(cleanValue, 0);
                ipKey = routeEntry.getKey();
                origin = routeEntry.getOrigin();
            }
            this.value = cleanValue;
            return;
        } catch (RuntimeException e) {
            LOGGER.debug(e.getMessage(), e);
        }

        try {
            this.value = IpInterval.removeTrailingDot(cleanValue);
            ipKeyReverse = IpInterval.parseReverseDomain(this.value);
            return;
        } catch (RuntimeException e) {
            LOGGER.debug(e.getMessage(), e);
        }

        this.value = cleanValue;
    }

    public String getValue() {
        return value;
    }

    public IpInterval<?> getIpKeyOrNull() {
        return ipKey;
    }

    public IpInterval<?> getIpKeyOrNullReverse() {
        return ipKeyReverse;
    }

    public AsBlockRange getAsBlockRangeOrNull() {
        if (!parsedAsBlockRange) {
            parsedAsBlockRange = true;

            try {
                // support for 'AS222' specification of as-block (meaning 'AS222-AS222')
                final String sanitizedAsBlock = value.indexOf('-') == -1 ? value + "-" + value : value;
                asBlockRange = AsBlockRange.parse(sanitizedAsBlock);
            } catch (AttributeParseException e) {
                asBlockRange = null;
            }
        }

        return asBlockRange;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getOrigin() {
        return origin;
    }
}
