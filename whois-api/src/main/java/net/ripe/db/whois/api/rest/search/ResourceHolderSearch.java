package net.ripe.db.whois.api.rest.search;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.ResourceHolder;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class ResourceHolderSearch {

    private static final List<ObjectType> RESOURCE_TYPES = Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM);
    private static final List<ObjectType> RESOURCE_TREE_TYPES = Lists.newArrayList(ObjectType.INETNUM, ObjectType.INET6NUM);

    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public ResourceHolderSearch(
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            final Maintainers maintainers,
            @Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
        this.rpslObjectDao = rpslObjectDao;
    }

    /**
     * Find the resource holder (organisation id and name) for a given resource (inetnum, inet6num or aut-num).
     */
    @Nullable
    public ResourceHolder findResourceHolder(final RpslObject rpslObject) {
        if (!RESOURCE_TYPES.contains(rpslObject.getType())) {
            return null;
        }

        final RpslObject org = lookupOrganisation(rpslObject.getValueOrNullForAttribute(AttributeType.ORG));
        if (org != null) {
            return new ResourceHolder(org.getKey(), org.findAttribute(AttributeType.ORG_NAME).getCleanValue());
        }

        return findInParents(rpslObject);
    }

    /**
     * If there's no referenced org with org-name in the inetnum or inet6num, then check the parent tree
     * @param rpslObject
     * @return
     */
    @Nullable
    private ResourceHolder findInParents(final RpslObject rpslObject) {
        if (! RESOURCE_TREE_TYPES.contains(rpslObject.getType())) {
            return null;
        }

        final IpInterval interval;
        try {
             interval = IpInterval.parse(rpslObject.getKey());
        } catch (IllegalArgumentException e) {
            return null;
        }

        for (IpEntry ipEntry : Lists.reverse(findParentsInTree(interval))) {
            final RpslObject parent = lookup(ipEntry);
            if ((parent != null) && (hasUserMntner(parent) || hasUserMntLower(parent))) {
                final RpslObject org = lookupOrganisation(parent.getValueOrNullForAttribute(AttributeType.ORG));
                if (org != null) {
                    return new ResourceHolder(org.getKey(), org.getValueOrNullForAttribute(AttributeType.ORG_NAME));
                }
            }
        }

        return null;
    }

    private boolean hasUserMntner(final RpslObject rpslObject) {
        return rpslObject.getValuesForAttribute(AttributeType.MNT_BY)
                .stream()
                .anyMatch(mntner -> !maintainers.isRsMaintainer(mntner));
    }

    private boolean hasUserMntLower(final RpslObject rpslObject) {
        return rpslObject.getValuesForAttribute(AttributeType.MNT_LOWER)
                .stream()
                .anyMatch(mntner -> !maintainers.isRsMaintainer(mntner));
    }

    @Nullable
    private RpslObject lookup(final IpEntry ipEntry) {
        try {
            return rpslObjectDao.getById(ipEntry.getObjectId());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Nullable
    private RpslObject lookupOrganisation(final CIString orgKey) {
        if (orgKey == null) {
            return null;
        }

        try {
            return rpslObjectDao.getByKey(ObjectType.ORGANISATION, orgKey);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<? extends IpEntry> findParentsInTree(final IpInterval interval) {
        return switch (interval) {
            case Ipv4Resource ipv4Resource -> ipv4Tree.findAllLessSpecific(ipv4Resource);
            case Ipv6Resource ipv6Resource -> ipv6Tree.findAllLessSpecific(ipv6Resource);
            case null -> throw new IllegalStateException();
        };
    }
}
