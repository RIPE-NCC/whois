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
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import net.ripe.db.whois.query.dao.AbuseValidationStatusDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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

    public Optional<AbuseContact> getAbuseContact(final RpslObject rpslObject) {
        final RpslObject role = getAbuseContactRole(rpslObject);
        if (role == null) {
            return Optional.empty();
        }

        final boolean suspect = abuseValidationStatusDao.isSuspect(role.getValueForAttribute(AttributeType.ABUSE_MAILBOX));

        return Optional.of(new AbuseContact(
                        role.getKey(),
                        role.getValueForAttribute(AttributeType.ABUSE_MAILBOX),
                        suspect,
                        getOrgToContact(rpslObject, suspect)
                ));
    }

    @Nullable
    private CIString getOrgToContact(final RpslObject rpslObject, final boolean suspect) {
        if (suspect) {
            final CIString lir = findResponsibleLirOrgReference(rpslObject);
            if (lir != null) {
                return lir;
            }
        }

        return findResponsibleOrgReference(rpslObject);
    }

    @Nullable
    private CIString findResponsibleOrgReference(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.SPONSORING_ORG)) {
            return rpslObject.getValueForAttribute(AttributeType.SPONSORING_ORG);
        }

        if (rpslObject.containsAttribute(AttributeType.ORG)) {
            return rpslObject.getValueForAttribute(AttributeType.ORG);
        }

        if (rpslObject.getType() != ObjectType.INETNUM && rpslObject.getType() != ObjectType.INET6NUM) {
            return null;
        }

        final RpslObject parent = getParentObject(rpslObject);
        return parent != null ? findResponsibleOrgReference(parent) : null;
    }

    @Nullable
    private CIString findResponsibleLirOrgReference(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.SPONSORING_ORG)) {
            return rpslObject.getValueForAttribute(AttributeType.SPONSORING_ORG);
        }

        final CIString org = rpslObject.getValueOrNullForAttribute(AttributeType.ORG);
        if (org != null) {
            if (isLir(getByKey(ObjectType.ORGANISATION, org))) {
                return org;
            }
        }

        if (rpslObject.getType() != ObjectType.INETNUM && rpslObject.getType() != ObjectType.INET6NUM) {
            return null;
        }

        final RpslObject parent = getParentObject(rpslObject);
        return parent != null ? findResponsibleLirOrgReference(parent) : null;
    }

    private boolean isLir(final RpslObject rpslObject) {
        return OrgType.getFor(rpslObject.getValueOrNullForAttribute(AttributeType.ORG_TYPE)) == OrgType.LIR;
    }

    @CheckForNull
    @Nullable
    public RpslObject getAbuseContactRole(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case INETNUM:
            case INET6NUM:

                final RpslObject role = getAbuseContactRoleInternal(rpslObject);

                if (role == null) {
                    final RpslObject parentObject = getParentObject(rpslObject);
                    if (parentObject != null && !isMaintainedByRs(rpslObject)) {
                        return getAbuseContactRole(parentObject);
                    }
                }

                return role;

            case AUT_NUM:
                return getAbuseContactRoleInternal(rpslObject);

            default:
                return null;
        }
    }

    @CheckForNull
    @Nullable
    private RpslObject getAbuseContactRoleInternal(final RpslObject rpslObject) {
        try {
            // use the abuse-c from the object if it exists:
            RpslObject abuseContact = getAbuseC(rpslObject);
            if (abuseContact != null) {
                return abuseContact;
            }

            // otherwise see if it can be obtained via an org attribute:
            return getOrgAbuseC(rpslObject);
        } catch (EmptyResultDataAccessException ignored) {
            LOGGER.debug("Ignored invalid reference (object {})", rpslObject.getKey());
        }

        return null;
    }

    @Nullable
    private RpslObject getOrgAbuseC(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.ORG)) {
            final RpslObject organisation = getByKey(ObjectType.ORGANISATION, rpslObject.getValueForAttribute(AttributeType.ORG));
            return getAbuseC(organisation);
        }
        return null;
    }

    @Nullable
    private RpslObject getAbuseC(final RpslObject rpslObject) {
        if (rpslObject.containsAttribute(AttributeType.ABUSE_C)) {
            final RpslObject abuseCRole = getByKey(ObjectType.ROLE, rpslObject.getValueForAttribute(AttributeType.ABUSE_C));
            if (abuseCRole.containsAttribute(AttributeType.ABUSE_MAILBOX)) {
                return abuseCRole;
            }
        }
        return null;
    }

    private boolean isMaintainedByRs(final RpslObject rpslObject) {
        return maintainers.isRsMaintainer(rpslObject.getValuesForAttribute(AttributeType.MNT_BY, AttributeType.MNT_LOWER));
    }

    @Nullable
    private RpslObject getParentObject(final RpslObject rpslObject) {
        final IpEntry ipEntry;

        switch (rpslObject.getType()) {
            case INETNUM:
                ipEntry = CollectionHelper.uniqueResult(ipv4Tree.findFirstLessSpecific(Ipv4Resource.parse(rpslObject.getKey())));
                break;

            case INET6NUM:
                ipEntry = CollectionHelper.uniqueResult(ipv6Tree.findFirstLessSpecific(Ipv6Resource.parse(rpslObject.getKey())));
                break;

            default:
                throw new IllegalArgumentException("Unexpected type: " + rpslObject.getType());
        }

        return (ipEntry != null) ? getById(ipEntry.getObjectId()) : null;
    }

    @Nonnull
    private RpslObject getById(final int objectId) {
        try {
            return objectDao.getById(objectId);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException("Object does not exist: " + objectId);
        }
    }

    @Nonnull
    private RpslObject getByKey(final ObjectType objectType, final CIString key) {
        try {
            return objectDao.getByKey(objectType, key);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalStateException(objectType.getName() + " object does not exist: " + key);
        }
    }
}
