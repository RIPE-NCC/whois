package net.ripe.db.whois.common.domain;

/**
 * Contains a single ip resource configuration entry.
 *
 * @param <V> The configuration entry data type.
 */
public class IpResourceEntry<V> {
    private final IpInterval<?> ipInterval;
    private final V value;

    public IpResourceEntry(final IpInterval<?> ipInterval, final V value) {
        this.ipInterval = ipInterval;
        this.value = value;
    }

    public IpInterval<?> getIpInterval() {
        return ipInterval;
    }

    public V getValue() {
        return value;
    }
}