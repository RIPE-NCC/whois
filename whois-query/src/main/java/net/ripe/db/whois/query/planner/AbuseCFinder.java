package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;
import net.ripe.db.whois.common.iptree.IpEntry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.dao.AbuseValidationStatusDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Optional;

@Component
public class AbuseCFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbuseCFinder.class);

    private final RpslObjectDao objectDao;
    private final Ipv4Tree ipv4Tree;
    private final Ipv6Tree ipv6Tree;
    private final Maintainers maintainers;
    private final AbuseValidationStatusDao abuseValidationStatusDao;

    @Autowired
    public AbuseCFinder(final RpslObjectDao objectDao,
                        final Ipv4Tree ipv4Tree,
                        final Ipv6Tree ipv6Tree,
                        final Maintainers maintainers,
                        final AbuseValidationStatusDao abuseValidationStatusDao) {
        this.objectDao = objectDao;
        this.ipv4Tree = ipv4Tree;
        this.ipv6Tree = ipv6Tree;
        this.maintainers = maintainers;
        this.abuseValidationStatusDao = abuseValidationStatusDao;
    }

    public Optional<AbuseContact> getAbuseContact(final RpslObject object) {
        final RpslObject role = getAbuseContactRole(object);
        return role != null?
                Optional.of(new AbuseContact(
                        role.getKey(),
                        role.getValueForAttribute(AttributeType.ABUSE_MAILBOX),
                        abuseValidationStatusDao.isSuspect(role.getValueForAttribute(AttributeType.ABUSE_MAILBOX)),
                        getOrgToContact(object)
                )) : Optional.empty();
    }

    @Nullable
    private CIString getOrgToContact(final RpslObject object) {
        if (object.containsAttribute(AttributeType.SPONSORING_ORG)) {
            return object.getValueForAttribute(AttributeType.SPONSORING_ORG);
        }

        final RpslObject responsibleOrg = findResponsibleOrgObject(object);
        return responsibleOrg != null? responsibleOrg.getValueForAttribute(AttributeType.ORG) : null;
    }

    @Nullable
    private RpslObject findResponsibleOrgObject(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.ORG)) {
            return rpslObject;
        }

        if (rpslObject.getType() != ObjectType.INETNUM && rpslObject.getType() != ObjectType.INET6NUM) {
            return null;
        }

        final RpslObject parent = getParentObject(rpslObject);
        return parent != null? findResponsibleOrgObject(parent) : null;
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
            // use the abuse-c from the object if it exists:
            RpslObject abuseContact = getAbuseC(object);
            if (abuseContact != null) {
                return abuseContact;
            }

            // otherwise see if it can be obtained via an org attribute:
            return getOrgAbuseC(object);
        } catch (EmptyResultDataAccessException ignored) {
            LOGGER.debug("Ignored invalid reference (object {})", object.getKey());
        }

        return null;
    }

    @CheckForNull
    @Nullable
    private RpslObject getOrgAbuseC(final RpslObject object) {
        if (object.containsAttribute(AttributeType.ORG)) {
            final RpslObject organisation = objectDao.getByKey(ObjectType.ORGANISATION, object.getValueForAttribute(AttributeType.ORG));
            return getAbuseC(organisation);
        }
        return null;
    }

    @CheckForNull
    @Nullable
    private RpslObject getAbuseC(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.ABUSE_C)) {
            final RpslObject abuseCRole = objectDao.getByKey(ObjectType.ROLE, rpslObject.getValueForAttribute(AttributeType.ABUSE_C));
            if (abuseCRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                return abuseCRole;
            }
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
