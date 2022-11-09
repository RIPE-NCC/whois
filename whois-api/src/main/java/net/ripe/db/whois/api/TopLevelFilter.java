package net.ripe.db.whois.api;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.stream.Collectors;

public class TopLevelFilter<T extends Interval<T>>  {
    private final NestedIntervalMap<T, CIString> tree;
    private final List<RpslObject> rpslObjects = Lists.newArrayList();
    public TopLevelFilter(){
        this.tree = new NestedIntervalMap<>();
    }

    public List<RpslObject> filter() {
        return matchRpslWithTree(this.rpslObjects);

    }

    public void addElementToTree(final RpslObject rpslObject) {
        final T key = (T) IpInterval.parse(rpslObject.getKey());
        final List<CIString> moreSpecific = tree.findFirstMoreSpecific(key);
        if (!moreSpecific.isEmpty()){
            final T moreSpecificKey = (T) IpInterval.parse(moreSpecific.get(0));
            tree.remove(moreSpecificKey);
            tree.put(key, rpslObject.getKey());
        } else {
            if (tree.findExactAndAllLessSpecific(key).isEmpty()){
                tree.put(key, rpslObject.getKey());
            }
        }
        rpslObjects.add(rpslObject);
    }

    private List<RpslObject> matchRpslWithTree(final List<RpslObject> rpslObjects) {
        return rpslObjects.stream().filter(rpslObject -> !tree.findExact((T) IpInterval.parse(rpslObject.getKey())).isEmpty()).collect(Collectors.toList());
    }
}
