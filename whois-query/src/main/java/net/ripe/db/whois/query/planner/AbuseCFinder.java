package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@Component
public class AbuseCFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseCFinder.class);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;

    @Autowired
    public AbuseCFinder(final RpslObjectDao objectDao,
                        final Ipv4Tree ipv4Tree,
                        final Ipv6Tree ipv6Tree,
                        final Maintainers maintainers) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
    }

    @CheckForNull
    @Nullable
    public String getAbuseContact(final RpslObject object) {
        final RpslObject role = getAbuseContactRole(object);
        return (role != null) ? role.getValueForAttribute(AttributeType.ABUSE_MAILBOX).toString() : null;
    }

    @CheckForNull
    @Nullable
    public RpslObject getAbuseContactRole(final RpslObject object) {
        switch (object.getType()) {
            case INETNUM:
            case INET6NUM:

                final RpslObject role = getAbuseContactRoleInternal(object);

                if (role == null) {
                    final RpslObject parentObject = getParentObject(object);
                    if (parentObject != null && !isMaintainedByRs(object)) {
                        return getAbuseContactRole(parentObject);
                    }
                }

                return role;

            case AUT_NUM:
                return getAbuseContactRoleInternal(object);

            default:
                return null;
        }
    }

    @CheckForNull
    @Nullable
    private RpslObject getAbuseContactRoleInternal(final RpslObject object) {
        try {
            if (object.containsAttribute(AttributeType.ORG)) {
                final RpslObject organisation = objectDao.getByKey(ObjectType.ORGANISATION, object.getValueForAttribute(AttributeType.ORG));
                if (organisation.containsAttribute(AttributeType.ABUSE_C)) {
                    final RpslObject abuseCRole = objectDao.getByKey(ObjectType.ROLE, organisation.getValueForAttribute(AttributeType.ABUSE_C));
                    if (abuseCRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                        return abuseCRole;
                    }
                }
            }
        } catch (EmptyResultDataAccessException ignored) {
            LOGGER.debug("Ignored invalid reference (object {})", object.getKey());
        }
        return null;
    }

    private boolean isMaintainedByRs(final RpslObject inetObject) {
        return maintainers.isRsMaintainer(inetObject.getValuesForAttribute(AttributeType.MNT_BY, AttributeType.MNT_LOWER));
    }

    @Nullable
    private RpslObject getParentObject(final RpslObject object) {
        final IpEntry ipEntry;

        switch (object.getType()) {
            case INETNUM:
                ipEntry = CollectionHelper.uniqueResult(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(object.getKey())));
                break;

            case INET6NUM:
                ipEntry = CollectionHelper.uniqueResult(ipv6Tree.findFirstLessSpecific(Ipv6Resource.parse(object.getKey())));
                break;

            default:
                throw new IllegalArgumentException("Unexpected type: " + object.getType());
        }

        try {
            return (ipEntry != null) ? objectDao.getById(ipEntry.getObjectId()) : null;
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException("Parent does not exist: " + ipEntry.getObjectId());
        }
    }
}
