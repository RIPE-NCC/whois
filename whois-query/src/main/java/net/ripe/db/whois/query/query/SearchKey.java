package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.domain.IpInterval;
import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.domain.Ipv6Resource;
import net.ripe.db.whois.common.domain.attrs.AsBlockRange;
import net.ripe.db.whois.common.exception.AsBlockParseException;

class SearchKey {
    private final String value;

    private boolean initIpKey = true;
    private IpInterval<?> ipKey;
    private boolean initIpKeyReverse = true;
    private IpInterval<?> ipKeyReverse;
    private boolean initAsBlockRange = true;
    private AsBlockRange asBlockRange;

    SearchKey(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public IpInterval<?> getIpKeyOrNull() {
        if (initIpKey) {
            initIpKey = false;

            try {
                ipKey = IpInterval.parse(value);
            } catch (RuntimeException e) {
                ipKey = null;
            }
        }

        return ipKey;
    }

    public IpInterval<?> getIpKeyOrNullReverse() {
        if (initIpKeyReverse) {
            initIpKeyReverse = false;

            try {
                ipKeyReverse = Ipv4Resource.parseReverseDomain(value);
            } catch (IllegalArgumentException e) {
                ipKeyReverse = null;
            }

            if (ipKeyReverse == null) {
                try {
                    ipKeyReverse = Ipv6Resource.parseReverseDomain(value);
                } catch (IllegalArgumentException e) {
                    ipKeyReverse = null;
                }
            }
        }

        return ipKeyReverse;
    }


    public AsBlockRange getAsBlockRangeOrNull() {
        if (initAsBlockRange) {
            initAsBlockRange = false;

            try {
                asBlockRange = AsBlockRange.parse(value);
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
