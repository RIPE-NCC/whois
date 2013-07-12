package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import net.ripe.db.whois.common.exception.AsBlockParseException;

import java.util.regex.Pattern;

class SearchKey {
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private String value;

    private IpInterval<?> ipKey;
    private IpInterval<?> ipKeyReverse;
    private AsBlockRange asBlockRange;
    private boolean parsedAsBlockRange;

    SearchKey(final String value) {
        final String cleanValue = WHITESPACE_PATTERN.matcher(value.trim()).replaceAll(" ");

        try {
            ipKey = IpInterval.parse(cleanValue);
            this.value = ipKey instanceof Ipv4Resource ? ((Ipv4Resource) ipKey).toRangeString() : ipKey.toString();
            return;
        } catch (RuntimeException e) {}

        try {
            ipKeyReverse = Ipv4Resource.parseReverseDomain(cleanValue);
            this.value = IpInterval.removeTrailingDot(cleanValue);
            return;
        } catch (RuntimeException e) {}

        try {
            ipKeyReverse = Ipv6Resource.parseReverseDomain(cleanValue);
            this.value = IpInterval.removeTrailingDot(cleanValue);
            return;
        } catch (RuntimeException e) {}

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
            } catch (AsBlockParseException e) {
                asBlockRange = null;
            }
        }

        return asBlockRange;
    }

    @Override
    public String toString() {
        return value;
    }
}
