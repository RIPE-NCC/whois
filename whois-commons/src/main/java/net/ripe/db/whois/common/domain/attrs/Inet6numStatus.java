package net.ripe.db.whois.common.domain.attrs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.domain.attrs.OrgType.*;

public enum Inet6numStatus implements InetStatus {
    ALLOCATED_BY_RIR("ALLOCATED-BY-RIR", IANA, RIR, LIR),
    ALLOCATED_BY_LIR("ALLOCATED-BY-LIR", LIR, OTHER),
    AGGREGATED_BY_LIR("AGGREGATED-BY-LIR", LIR, OTHER),
    ASSIGNED("ASSIGNED", LIR, OTHER),
    ASSIGNED_ANYCAST("ASSIGNED ANYCAST", LIR, OTHER),
    ASSIGNED_PI("ASSIGNED PI", LIR, OTHER);

    private final CIString literalStatus;
    private final Set<OrgType> allowedOrgTypes;
    private static List<Inet6numStatus> RS_MNTNER_STATUSES = Lists.newArrayList(ASSIGNED_PI, ASSIGNED_ANYCAST, ALLOCATED_BY_RIR);
    private static List<Inet6numStatus> ALLOC_MNTNER_STATUSES = Lists.newArrayList(ALLOCATED_BY_RIR);

    private static final Set<Inet6numStatus> NEEDS_ORG_REFERENCE;
    private static final Map<InetStatus, List<InetStatus>> PARENT_STATUS;

    static {
        PARENT_STATUS = Maps.newHashMap();
        PARENT_STATUS.put(ALLOCATED_BY_RIR, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR));
        PARENT_STATUS.put(ALLOCATED_BY_LIR, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR));
        PARENT_STATUS.put(AGGREGATED_BY_LIR, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR, AGGREGATED_BY_LIR));
        PARENT_STATUS.put(ASSIGNED, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR, ALLOCATED_BY_LIR, AGGREGATED_BY_LIR));
        PARENT_STATUS.put(ASSIGNED_ANYCAST, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR));
        PARENT_STATUS.put(ASSIGNED_PI, Lists.<InetStatus>newArrayList(ALLOCATED_BY_RIR));

        NEEDS_ORG_REFERENCE = Sets.newHashSet(ASSIGNED_ANYCAST, ALLOCATED_BY_RIR, ASSIGNED_PI);
    }

    private Inet6numStatus(final String literalStatus, final OrgType... orgType) {
        this.literalStatus = ciString(literalStatus);
        this.allowedOrgTypes = Collections.unmodifiableSet(Sets.newEnumSet(Lists.newArrayList(orgType), OrgType.class));
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
        return NEEDS_ORG_REFERENCE.contains(getStatusFor(literalStatus));
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
