package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
class RelatedIrtDecorator implements PrimaryObjectDecorator {
    private final List<? extends HierarchyLookup> hierarchyLookups;

    @Autowired
    public RelatedIrtDecorator(final HierarchyLookupIpv4 hierarchyLookupIpv4, final HierarchyLookupIpv6 hierarchyLookupIpv6) {
        this.hierarchyLookups = Arrays.asList(hierarchyLookupIpv4, hierarchyLookupIpv6);
    }

    @Override
    public boolean appliesToQuery(final Query query) {
        return query.isReturningIrt();
    }

    @Override
    public Collection<RpslObjectInfo> decorate(final RpslObject rpslObject) {
        for (final HierarchyLookup hierarchyLookup : hierarchyLookups) {
            if (hierarchyLookup.supports(rpslObject)) {
                return hierarchyLookup.getReferencedIrtsInHierarchy(rpslObject);
            }
        }

        return Collections.emptyList();
    }
}
