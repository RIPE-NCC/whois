package net.ripe.db.whois.api.rest.search;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class ResourceHolderSearch {

    private static final CIString RIPE_NCC_HOSTMASTER_MNTNER = CIString.ciString("RIPE-NCC-HM-MNT");
    private static final CIString RIPE_NCC_LEGACY_MNTNER = CIString.ciString("RIPE-NCC-LEGACY-MNT");

    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final RpslObjectDao rpslObjectDao;

    @Autowired
    public ResourceHolderSearch(
            final Ipv4Tree ipv4Tree,
            final Ipv6Tree ipv6Tree,
            final RpslObjectDao rpslObjectDao) {
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.rpslObjectDao = rpslObjectDao;
    }

    /**
     * Find the resource holder (organisation id and name) for a given resource (inetnum, inet6num or aut-num).
     */
    @Nullable
    public ResourceHolder findResourceHolder(final RpslObject rpslObject) {
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
        if ((ObjectType.INETNUM != rpslObject.getType()) && (ObjectType.INET6NUM != rpslObject.getType())) {
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
            if (parent != null) {
                // TODO: [ES] logic may not handle user default maintainer properly
                if (hasUserMntBy(parent) || hasUserMntLower(parent)) {
                    final RpslObject org = lookupOrganisation(parent.getValueOrNullForAttribute(AttributeType.ORG));
                    if (org != null) {
                        return new ResourceHolder(org.getKey(), org.getValueOrNullForAttribute(AttributeType.ORG_NAME));
                    }
                }
            }
        }

        return null;
    }

    private boolean hasUserMntBy(final RpslObject rpslObject) {
        for (RpslAttribute mntBy : rpslObject.findAttributes(AttributeType.MNT_BY)) {
            if (!RIPE_NCC_HOSTMASTER_MNTNER.equals(mntBy.getValue()) && !RIPE_NCC_LEGACY_MNTNER.equals(mntBy.getValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUserMntLower(final RpslObject rpslObject) {
        for (RpslAttribute mntLower : rpslObject.findAttributes(AttributeType.MNT_LOWER)) {
            if (!RIPE_NCC_HOSTMASTER_MNTNER.equals(mntLower.getValue())) {
                return true;
            }
        }
        return false;
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
        if (interval instanceof Ipv4Resource) {
            return ipv4Tree.findAllLessSpecific((Ipv4Resource)interval);
        } else if (interval instanceof Ipv6Resource) {
            return ipv6Tree.findAllLessSpecific((Ipv6Resource)interval);
        } else {
            throw new IllegalStateException();
        }
    }
}
