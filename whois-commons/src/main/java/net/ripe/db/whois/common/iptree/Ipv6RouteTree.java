package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.domain.ip.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Ipv6RouteTree extends CachedIpTree<Ipv6Resource, Ipv6RouteEntry> {
    @Autowired
    public Ipv6RouteTree(final IpTreeCacheManager ipTreeCacheManager, final SourceContext sourceContext) {
        super(ipTreeCacheManager, sourceContext);
    }

    @Override
    IntervalMap<Ipv6Resource, Ipv6RouteEntry> getIntervalMap(final IpTreeCacheManager.NestedIntervalMaps nestedIntervalMaps) {
        return nestedIntervalMaps.getIpv6RouteTreeCache();
    }
}
