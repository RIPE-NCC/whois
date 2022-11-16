package net.ripe.db.whois.api;

import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TopLevelFilter<T extends Interval<T>>  {
    private NestedIntervalMap<T, RpslObject> tree;
    public TopLevelFilter(Stream<RpslObject> rpslObjectStream){
        this.buildTree(rpslObjectStream);
    }

    public List<RpslObject> filter() {
        if(tree == null){
            return Collections.emptyList();
        }
        return tree.mapToValues();

    }
    private void buildTree(Stream<RpslObject> rpslObjects) {
        rpslObjects.forEach(rpslObject -> {
            this.tree = new NestedIntervalMap<>();
            final T key = (T) IpInterval.parse(rpslObject.getKey());
            List<RpslObject> moreSpecific = tree.findFirstMoreSpecific(key);
            if (!moreSpecific.isEmpty()) {
                final T moreSpecificKey = (T) IpInterval.parse(moreSpecific.get(0).getKey());
                tree.remove(moreSpecificKey);
                tree.put(key, rpslObject);
            } else {
                if (tree.findExactAndAllLessSpecific(key).isEmpty()) {
                    tree.put(key, rpslObject);
                }
            }
        });
    }

}
