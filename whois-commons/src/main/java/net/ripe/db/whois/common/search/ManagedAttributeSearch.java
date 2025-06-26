package net.ripe.db.whois.common.search;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.jdbc.JdbcManagedAttributeDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AutnumStatus;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.common.rpsl.attrs.OrgType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

@Component
public class ManagedAttributeSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedAttributeSearch.class);

    private static final ImmutableSet<AttributeType> ALLOCATION_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.INETNUM, AttributeType.INET6NUM, AttributeType.ORG, AttributeType.STATUS, AttributeType.NETNAME, AttributeType.SOURCE);
    private static final ImmutableSet<AttributeType> ASSIGNMENT_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.INETNUM, AttributeType.INET6NUM, AttributeType.ORG, AttributeType.SPONSORING_ORG, AttributeType.STATUS, AttributeType.SOURCE);
    private static final ImmutableSet<AttributeType> LEGACY_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.INETNUM, AttributeType.ORG, AttributeType.SPONSORING_ORG, AttributeType.STATUS, AttributeType.SOURCE);

    private static final ImmutableSet<AttributeType> ORG_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.ORGANISATION, AttributeType.ORG, AttributeType.ORG_NAME, AttributeType.ORG_TYPE, AttributeType.COUNTRY, AttributeType.SOURCE);
    private static final ImmutableSet<AttributeType> AUT_NUM_ATTRIBUTES = Sets.immutableEnumSet(AttributeType.AUT_NUM, AttributeType.ORG, AttributeType.SPONSORING_ORG, AttributeType.STATUS, AttributeType.SOURCE);

    private static final ImmutableSet<InetnumStatus> INETNUM_ASSIGNMENT_STATUSES = Sets.immutableEnumSet(InetnumStatus.ASSIGNED_PI, InetnumStatus.ASSIGNED_ANYCAST);
    private static final ImmutableSet<InetnumStatus> INETNUM_ALLOCATION_STATUSES = Sets.immutableEnumSet(InetnumStatus.ALLOCATED_PA, InetnumStatus.ALLOCATED_ASSIGNED_PA, InetnumStatus.ALLOCATED_UNSPECIFIED);
    private static final ImmutableSet<InetnumStatus> INETNUM_LEGACY_STATUSES = Sets.immutableEnumSet(InetnumStatus.LEGACY);
    private static final ImmutableSet<Inet6numStatus> INET6NUM_ALLOCATION_STATUSES = Sets.immutableEnumSet(Inet6numStatus.ALLOCATED_BY_RIR);
    private static final ImmutableSet<Inet6numStatus> INET6NUM_ASSIGNMENT_STATUSES = Sets.immutableEnumSet(Inet6numStatus.ASSIGNED_PI, Inet6numStatus.ASSIGNED_ANYCAST);
    private static final ImmutableSet<AutnumStatus> AUT_NUM_STATUSES = Sets.immutableEnumSet(AutnumStatus.ASSIGNED, AutnumStatus.LEGACY);

    private final Maintainers maintainers;
    private final JdbcManagedAttributeDao managedAttributeDao;

    @Autowired
    public ManagedAttributeSearch(
            final Maintainers maintainers,
            final JdbcManagedAttributeDao managedAttributeDao) {
        this.maintainers = maintainers;
        this.managedAttributeDao = managedAttributeDao;
    }

    /**
     * Is the RPSL object co-maintained by the RIPE NCC
     * @param rpslObject
     * @return
     */
    public boolean isCoMaintained(final RpslObject rpslObject) {
        switch (rpslObject.getType()) {
            case INETNUM: {
                final InetnumStatus inetnumStatus = getInetnumStatus(rpslObject);
                return isLegacyInetnumCoMaintained(rpslObject, inetnumStatus) ||
                        isAssignedInetnumCoMaintained(rpslObject, inetnumStatus) ||
                        isAllocatedInetnumCoMaintained(inetnumStatus);
            }
            case INET6NUM: {
                final Inet6numStatus inet6numStatus = getInet6numStatus(rpslObject);
                return isAllocatedInet6numCoMaintained(inet6numStatus) ||
                        isAssignedInet6numCoMaintained(rpslObject, inet6numStatus);
            }
            case AUT_NUM: {
                return isAutNumCoMaintained(rpslObject);
            }
            case ORGANISATION: {
                return isOrganisationCoMaintained(rpslObject);
            }
            default: {
                return false;
            }
        }
    }

    /**
     * Is the RPSL attribute maintained by the RIPE NCC
     * @param rpslObject
     * @param rpslAttribute
     * @return
     */
    public boolean isRipeNccMaintained(final RpslObject rpslObject, final RpslAttribute rpslAttribute) {
        if (AttributeType.MNT_BY == rpslAttribute.getType()) {
            return isRipeNccMntner(rpslAttribute.getCleanValues());
        }

        switch (rpslObject.getType()) {
            case INETNUM: {
                return isRipeMaintainedInetnumAttribute(rpslObject, rpslAttribute.getType());
            }
            case INET6NUM: {
                return isRipeMaintainedInet6numAttribute(rpslObject, rpslAttribute.getType());
            }
            case AUT_NUM: {
                return AUT_NUM_ATTRIBUTES.contains(rpslAttribute.getType());
            }
            case ORGANISATION: {
                return ORG_ATTRIBUTES.contains(rpslAttribute.getType());
            }
            default: {
                return false;
            }
        }
    }

    private boolean isRipeMaintainedInetnumAttribute(final RpslObject rpslObject, final AttributeType attributeType) {
        final InetnumStatus inetnumStatus = getInetnumStatus(rpslObject);
        if (inetnumStatus == null) {
            return false;
        }

        if (isLegacyInetnumCoMaintained(rpslObject, inetnumStatus)) {
            return LEGACY_ATTRIBUTES.contains(attributeType);
        }

        if (isAssignedInetnumCoMaintained(rpslObject, inetnumStatus)) {
            return ASSIGNMENT_ATTRIBUTES.contains(attributeType);
        }

        if (isAllocatedInetnumCoMaintained(inetnumStatus)) {
            return ALLOCATION_ATTRIBUTES.contains(attributeType);
        }

        return false;
    }

    private boolean isRipeMaintainedInet6numAttribute(final RpslObject rpslObject, final AttributeType attributeType) {
        final Inet6numStatus inet6numStatus = getInet6numStatus(rpslObject);
        if (inet6numStatus == null) {
            return false;
        }

        if (isAllocatedInet6numCoMaintained(inet6numStatus)) {
            return ALLOCATION_ATTRIBUTES.contains(attributeType);
        }

        if (isAssignedInet6numCoMaintained(rpslObject, inet6numStatus)) {
            return ASSIGNMENT_ATTRIBUTES.contains(attributeType);
        }

        return false;
    }

    private boolean isAllocatedInetnumCoMaintained(@Nullable final InetnumStatus inetnumStatus) {
        return INETNUM_ALLOCATION_STATUSES.contains(inetnumStatus);
    }

    private boolean isAssignedInetnumCoMaintained(final RpslObject rpslObject, final InetnumStatus inetnumStatus) {
        return INETNUM_ASSIGNMENT_STATUSES.contains(inetnumStatus) &&
                hasRipeNccMntner(rpslObject);
    }

    private boolean isLegacyInetnumCoMaintained(final RpslObject rpslObject, final InetnumStatus inetnumStatus) {
        return INETNUM_LEGACY_STATUSES.contains(inetnumStatus) &&
                hasRipeNccMntner(rpslObject);
    }

    private boolean isAllocatedInet6numCoMaintained(@Nullable final Inet6numStatus inet6numStatus) {
        return INET6NUM_ALLOCATION_STATUSES.contains(inet6numStatus);
    }

    private boolean isAssignedInet6numCoMaintained(final RpslObject rpslObject, final Inet6numStatus inet6numStatus) {
        return INET6NUM_ASSIGNMENT_STATUSES.contains(inet6numStatus) &&
                hasRipeNccMntner(rpslObject);
    }

    private boolean isAutNumCoMaintained(final RpslObject rpslObject) {
        return AUT_NUM_STATUSES.contains(getAutNumStatus(rpslObject)) &&
                hasRipeNccMntner(rpslObject);
    }

    @Nullable
    private AutnumStatus getAutNumStatus(final RpslObject rpslObject) {
        if (rpslObject.getType() != ObjectType.AUT_NUM) {
            return null;
        }

        final CIString statusValue = rpslObject.getValueOrNullForAttribute(AttributeType.STATUS);
        if (statusValue != null) {
            try {
                return AutnumStatus.valueOf(statusValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Invalid status {}", statusValue);
            }
        }

        return null;
    }

    @Nullable
    private Inet6numStatus getInet6numStatus(final RpslObject rpslObject) {
        if (rpslObject.getType() != ObjectType.INET6NUM) {
            return null;
        }

        final CIString statusValue = rpslObject.getValueOrNullForAttribute(AttributeType.STATUS);
        if (statusValue != null) {
            try {
                return Inet6numStatus.getStatusFor(statusValue);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Invalid status {}", statusValue);
            }
        }

        return null;
    }

    @Nullable
    private InetnumStatus getInetnumStatus(final RpslObject rpslObject) {
        if (rpslObject.getType() != ObjectType.INETNUM) {
            return null;
        }

        final CIString statusValue = rpslObject.getValueOrNullForAttribute(AttributeType.STATUS);
        if (statusValue != null) {
            try {
                return InetnumStatus.getStatusFor(statusValue);
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Invalid status {}", statusValue);
            }
        }

        return null;
    }

    private boolean isOrganisationCoMaintained(final RpslObject rpslObject) {
        if (rpslObject.getType() != ObjectType.ORGANISATION) {
            return false;
        }

        final OrgType orgType = OrgType.getFor(rpslObject.getValueOrNullForAttribute(AttributeType.ORG_TYPE));
        if (orgType == OrgType.LIR) {
            return true;
        }

        if (orgType == OrgType.OTHER) {
            return managedAttributeDao.hasManagedResource(rpslObject.getKey());
        }

        return false;
    }

    private boolean hasRipeNccMntner(final RpslObject rpslObject) {
        return maintainers.isRsMaintainer(rpslObject.getValuesForAttribute(AttributeType.MNT_BY));
    }

    private boolean isRipeNccMntner(final Set<CIString> mntner) {
        return maintainers.isRsMaintainer(mntner);
    }
}
