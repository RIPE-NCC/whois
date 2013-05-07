package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.domain.Ipv4Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Ipv4RouteTree extends CachedIpTree<Ipv4Resource, Ipv4RouteEntry> {
    @Autowired
    public Ipv4RouteTree(final IpTreeCacheManager ipTreeCacheManager, final SourceContext sourceContext) {
        super(ipTreeCacheManager, sourceContext);
    }

    @Override
    IntervalMap<Ipv4Resource, Ipv4RouteEntry> getIntervalMap(final IpTreeCacheManager.NestedIntervalMaps nestedIntervalMaps) {
        return nestedIntervalMaps.getIpv4RouteTreeCache();
    }
}
