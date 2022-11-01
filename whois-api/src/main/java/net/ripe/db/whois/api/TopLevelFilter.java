package net.ripe.db.whois.api;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TopLevelFilter<T extends Interval<T>> {
    private static final HashMap<ObjectType, TopLevelFilter> resourceFilterMap;
    static {
        resourceFilterMap = Maps.newHashMap();
        resourceFilterMap.put(ObjectType.INETNUM, new TopLevelFilter(Ipv4Resource.parse("0/0")));
        resourceFilterMap.put(ObjectType.INET6NUM, new TopLevelFilter(Ipv6Resource.parse("::/0")));
    }


    private final T root;

    public TopLevelFilter(final T root) {
        this.root = root;
    }

    private List<RpslObject> filter(final List<RpslObject> rpslObjects) {
        final NestedIntervalMap<T, CIString> tree = new NestedIntervalMap<>();

        for (RpslObject rpslObject : rpslObjects) {
            final T key = (T) IpInterval.parse(rpslObject.getKey());
            tree.put(key, rpslObject.getKey());
        }

        final List<CIString> firstMoreSpecifics = tree.findFirstMoreSpecific(root);

        return rpslObjects.stream()
                .filter(rpslObject -> firstMoreSpecifics.contains(rpslObject.getKey()))
                .collect(Collectors.toList());
    }

    public static List<RpslObject> filter(final List<RpslObject> rpslObjects, final ObjectType objectType) {
        return resourceFilterMap.get(objectType).filter(rpslObjects);
    }
}
