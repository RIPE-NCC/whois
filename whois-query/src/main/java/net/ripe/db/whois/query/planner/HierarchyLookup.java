package net.ripe.db.whois.query.planner;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.IpTree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

abstract class HierarchyLookup<K extends IpInterval<K>, V extends IpEntry<K>> {
    private final IpTree<K, V> ipTree;
    private final RpslObjectDao rpslObjectDao;

    protected HierarchyLookup(final IpTree<K, V> ipTree, final RpslObjectDao rpslObjectDao) {
        this.ipTree = ipTree;
        this.rpslObjectDao = rpslObjectDao;
    }

    public boolean supports(final RpslObject rpslObject) {
        return getSupportedType().equals(rpslObject.getType());
    }

    abstract ObjectType getSupportedType();

    public Collection<RpslObjectInfo> getReferencedIrtsInHierarchy(final RpslObject rpslObject) {
        final Collection<RpslObjectInfo> irts = getReferencedIrts(rpslObject);
        if (!irts.isEmpty()) {
            return irts;
        }

        final K resource = createResource(rpslObject.getKey());
        final List<V> entries = ipTree.findAllLessSpecific(resource);
        for (final V entry : entries) {
            final RpslObject object = rpslObjectDao.getById(entry.getObjectId());
            final Collection<RpslObjectInfo> referencedIrts = getReferencedIrts(object);
            if (!referencedIrts.isEmpty()) {
                return referencedIrts;
            }
        }

        return Collections.emptyList();
    }

    private Collection<RpslObjectInfo> getReferencedIrts(final RpslObject object) {
        final List<RpslObjectInfo> result = Lists.newArrayList();
        for (final CIString irt : object.getValuesForAttribute(AttributeType.MNT_IRT)) {
            result.add(rpslObjectDao.findByKey(ObjectType.IRT, irt.toString()));
        }

        return result;
    }

    final K createResource(CIString key) {
        return createResource(key.toString());
    }

    abstract K createResource(String key);
}
