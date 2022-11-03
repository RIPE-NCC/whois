package net.ripe.db.whois.api;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;
import java.util.stream.Collectors;

public class TopLevelFilter<T extends Interval<T>>  {


    public List<RpslObject> filter(final List<RpslObject> rpslObjects) {
        final NestedIntervalMap<T, CIString> tree = new NestedIntervalMap<>();

        buildTree(rpslObjects, tree);
        return matchRpslWithTree(rpslObjects, tree);

    }

    private List<RpslObject> matchRpslWithTree(List<RpslObject> rpslObjects, NestedIntervalMap<T, CIString> tree) {
        return rpslObjects.stream().filter(rpslObject -> !tree.findExact((T) IpInterval.parse(rpslObject.getKey())).isEmpty()).collect(Collectors.toList());
    }

    private void buildTree(List<RpslObject> rpslObjects, NestedIntervalMap<T, CIString> tree) {
        for (RpslObject rpslObject : rpslObjects) {
            final T key = (T) IpInterval.parse(rpslObject.getKey());
            List<CIString> moreSpecific = tree.findFirstMoreSpecific(key);
            if (!moreSpecific.isEmpty()){
                final T moreSpecificKey = (T) IpInterval.parse(moreSpecific.get(0));
                tree.remove(moreSpecificKey);
                tree.put(key, rpslObject.getKey());
            } else {
                if (tree.findExactAndAllLessSpecific(key).isEmpty()){
                    tree.put(key, rpslObject.getKey());
                }
            }
        }
    }

}
