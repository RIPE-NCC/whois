package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.IANA;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.LIR;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.OTHER;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.RIR;

public enum Inet6numStatus implements InetStatus {
    ALLOCATED_BY_RIR("ALLOCATED-BY-RIR", IANA, RIR, LIR),
    ALLOCATED_BY_LIR("ALLOCATED-BY-LIR", LIR, OTHER),
    AGGREGATED_BY_LIR("AGGREGATED-BY-LIR", LIR, OTHER),
    ASSIGNED("ASSIGNED", LIR, OTHER),
    ASSIGNED_ANYCAST("ASSIGNED ANYCAST", LIR, OTHER),
    ASSIGNED_PI("ASSIGNED PI", LIR, OTHER);

    private static final EnumSet<Inet6numStatus> RS_MNTNER_STATUSES = EnumSet.of(ASSIGNED_PI, ASSIGNED_ANYCAST, ALLOCATED_BY_RIR);
    private static final EnumSet<Inet6numStatus> ALLOC_MNTNER_STATUSES = EnumSet.of(ALLOCATED_BY_RIR);
    private static final EnumSet<Inet6numStatus> NEEDS_ORG_REFERENCE = EnumSet.of(ASSIGNED_ANYCAST, ALLOCATED_BY_RIR, ASSIGNED_PI);

    private static final EnumMap<Inet6numStatus, EnumSet<Inet6numStatus>> PARENT_STATUS;

    static {
        PARENT_STATUS = new EnumMap(Inet6numStatus.class);
        PARENT_STATUS.put(ALLOCATED_BY_RIR, EnumSet.of(ALLOCATED_BY_RIR));
        PARENT_STATUS.put(ALLOCATED_BY_LIR, EnumSet.of(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR));
        PARENT_STATUS.put(AGGREGATED_BY_LIR, EnumSet.of(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR, AGGREGATED_BY_LIR));
        PARENT_STATUS.put(ASSIGNED, EnumSet.of(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR, AGGREGATED_BY_LIR));
        PARENT_STATUS.put(ASSIGNED_ANYCAST, EnumSet.of(ALLOCATED_BY_RIR));
        PARENT_STATUS.put(ASSIGNED_PI, EnumSet.of(ALLOCATED_BY_RIR));
    }

    private final CIString literalStatus;
    private final Set<OrgType> allowedOrgTypes;

    private Inet6numStatus(final String literalStatus, final OrgType... orgType) {
        this.literalStatus = ciString(literalStatus);
        this.allowedOrgTypes = Sets.immutableEnumSet(Arrays.asList(orgType));
    }

    public static Inet6numStatus getStatusFor(final CIString status) {
        for (final Inet6numStatus stat : Inet6numStatus.values()) {
            if (stat.literalStatus.equals(status)) {
                return stat;
            }
        }

        throw new IllegalArgumentException(status + " is not a valid inet6numstatus");
    }

    public CIString getLiteralStatus() {
        return literalStatus;
    }

    @Override
    public boolean requiresRsMaintainer() {
        return RS_MNTNER_STATUSES.contains(this);
    }

    @Override
    public boolean requiresAllocMaintainer() {
        return ALLOC_MNTNER_STATUSES.contains(this);
    }

    @Override
    public boolean worksWithParentStatus(final InetStatus parent, boolean objectHasRsMaintainer) {
        return PARENT_STATUS.get(this).contains(parent);
    }

    @Override
    public boolean worksWithParentInHierarchy(final InetStatus parentInHierarchyMaintainedByRs, final boolean parentHasRsMntLower) {
        return true;
    }

    @Override
    public boolean needsOrgReference() {
        return NEEDS_ORG_REFERENCE.contains(this);
    }

    @Override
    public Set<OrgType> getAllowedOrgTypes() {
        return allowedOrgTypes;
    }

    @Override
    public boolean isValidOrgType(final OrgType orgType) {
        return allowedOrgTypes.contains(orgType);
    }

    @Override
    public String toString() {
        return literalStatus.toString();
    }
}
