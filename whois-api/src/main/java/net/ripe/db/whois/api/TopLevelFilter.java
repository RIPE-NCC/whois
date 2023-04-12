package net.ripe.db.whois.api;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.etree.NestedIntervalMap;
import net.ripe.db.whois.common.ip.Interval;
import net.ripe.db.whois.common.ip.IpInterval;

import java.util.Collections;
import java.util.List;

public class TopLevelFilter<T extends Interval<T>>  {
    private NestedIntervalMap<T, RpslObjectInfo> tree;
    public TopLevelFilter(final List<RpslObjectInfo> rpslObjectInfos){
        this.buildTree(rpslObjectInfos);
    }

    public List<RpslObjectInfo> getTopLevelValues() {
        if(tree == null){
            return Collections.emptyList();
        }
        return tree.mapToValues();

    }
    private void buildTree(final List<RpslObjectInfo> rpslObjectInfos) {
        this.tree = new NestedIntervalMap<>();
        rpslObjectInfos.forEach(rpslObjectInfo -> {
            final T key = (T) IpInterval.parse(rpslObjectInfo.getKey());
            final List<RpslObjectInfo> moreSpecific = tree.findFirstMoreSpecific(key);
            if (!moreSpecific.isEmpty()) {
                final T moreSpecificKey = (T) IpInterval.parse(moreSpecific.get(0).getKey());
                tree.remove(moreSpecificKey);
                tree.put(key, rpslObjectInfo);
            } else {
                if (tree.findExactAndAllLessSpecific(key).isEmpty()) {
                    tree.put(key, rpslObjectInfo);
                }
            }
        });
    }
}
