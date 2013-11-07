package net.ripe.db.whois.common.iptree;

import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.etree.IntervalMap;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Ipv6DomainTree extends CachedIpTree<Ipv6Resource, Ipv6Entry> {
    @Autowired
    public Ipv6DomainTree(final IpTreeCacheManager ipTreeCacheManager, final SourceContext sourceContext) {
        super(ipTreeCacheManager, sourceContext);
    }

    @Override
    IntervalMap<Ipv6Resource, Ipv6Entry> getIntervalMap(final IpTreeCacheManager.NestedIntervalMaps nestedIntervalMaps) {
        return nestedIntervalMaps.getIpv6DomainTreeCache();
    }
}
