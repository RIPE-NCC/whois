package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.IANA;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.LIR;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.OTHER;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.RIR;

public enum InetnumStatus implements InetStatus {
    ALLOCATED_PA("ALLOCATED PA", IANA, RIR, LIR),
    ALLOCATED_PI("ALLOCATED PI", IANA, RIR, LIR),
    ALLOCATED_UNSPECIFIED("ALLOCATED UNSPECIFIED", IANA, RIR, LIR),
    LIR_PARTITIONED_PA("LIR-PARTITIONED PA", LIR, OTHER),
    LIR_PARTITIONED_PI("LIR-PARTITIONED PI", LIR, OTHER),
    SUB_ALLOCATED_PA("SUB-ALLOCATED PA", LIR, OTHER),
    ASSIGNED_PA("ASSIGNED PA", LIR, OTHER),
    ASSIGNED_PI("ASSIGNED PI", LIR, OTHER, RIR),
    ASSIGNED_ANYCAST("ASSIGNED ANYCAST", LIR, OTHER),
    EARLY_REGISTRATION("EARLY-REGISTRATION", LIR, OTHER),
    NOT_SET("NOT-SET", LIR, OTHER),
    LEGACY("LEGACY", LIR, OTHER);

    private static final List<InetnumStatus> RS_MNTNER_STATUSES = Lists.newArrayList(ASSIGNED_ANYCAST, EARLY_REGISTRATION, ALLOCATED_UNSPECIFIED);

    private static final Set<InetnumStatus> NEEDS_ORG_REFERENCE;
    private static final Map<InetStatus, List<InetStatus>> PARENT_STATUS;
    private static final Map<InetStatus, List<InetStatus>> NEEDS_PARENT_RS_MNTR;

    static {
        PARENT_STATUS = Maps.newHashMap();
        PARENT_STATUS.put(ALLOCATED_PI, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(ALLOCATED_PA, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(ALLOCATED_UNSPECIFIED, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(LIR_PARTITIONED_PA, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA, EARLY_REGISTRATION));
        PARENT_STATUS.put(LIR_PARTITIONED_PI, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PI, LIR_PARTITIONED_PI, EARLY_REGISTRATION));
        PARENT_STATUS.put(SUB_ALLOCATED_PA, Lists.<InetStatus>newArrayList(ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA, EARLY_REGISTRATION));
        PARENT_STATUS.put(ASSIGNED_PA, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA, EARLY_REGISTRATION, ASSIGNED_PA));
        PARENT_STATUS.put(ASSIGNED_ANYCAST, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PI));
        PARENT_STATUS.put(EARLY_REGISTRATION, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, EARLY_REGISTRATION));
        PARENT_STATUS.put(ASSIGNED_PI, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PI, LIR_PARTITIONED_PI, EARLY_REGISTRATION, ASSIGNED_PI));

        NEEDS_PARENT_RS_MNTR = Maps.newHashMap();
        NEEDS_PARENT_RS_MNTR.put(ASSIGNED_PI, Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PI));
        NEEDS_ORG_REFERENCE = Sets.newHashSet(ALLOCATED_PI, ALLOCATED_PA, ALLOCATED_UNSPECIFIED);
    }

    private final CIString literalStatus;
    private final Set<OrgType> allowedOrgTypes;

    private InetnumStatus(final String literalStatus, final OrgType... orgType) {
        this.literalStatus = ciString(literalStatus);
        allowedOrgTypes = Collections.unmodifiableSet(Sets.newEnumSet(Lists.newArrayList(orgType), OrgType.class));
    }

    public static InetnumStatus getStatusFor(final CIString status) {
        for (final InetnumStatus stat : InetnumStatus.values()) {
            if (stat.literalStatus.equals(status)) {
                return stat;
            }
        }

        throw new IllegalArgumentException(status + " is not a valid inetnumstatus");
    }

    @Override
    public boolean isValidOrgType(final OrgType orgType) {
        return allowedOrgTypes.contains(orgType);
    }

    @Override
    public Set<OrgType> getAllowedOrgTypes() {
        return allowedOrgTypes;
    }

    @Override
    public boolean requiresRsMaintainer() {
        return RS_MNTNER_STATUSES.contains(this);
    }

    @Override
    public boolean requiresAllocMaintainer() {
        return false;
    }

    @Override
    public boolean worksWithParentStatus(final InetStatus parent, final boolean objectHasRsMaintainer) {
        if (this.equals(InetnumStatus.ASSIGNED_PI) && objectHasRsMaintainer) {
            ArrayList allowedParentStatus = Lists.<InetStatus>newArrayList(ALLOCATED_UNSPECIFIED, ALLOCATED_PI);
            return allowedParentStatus.contains(parent);
        }
        return PARENT_STATUS.get(this).contains(parent);
    }

    @Override
    public boolean worksWithParentInHierarchy(final InetStatus parentInHierarchyMaintainedByRs, final boolean parentHasRsMntLower) {
        if (this.equals(InetnumStatus.ASSIGNED_PI) && parentInHierarchyMaintainedByRs.equals(InetnumStatus.ALLOCATED_PI)) {
            if (parentHasRsMntLower) {
                return false;
            }
        } else if (this.equals(InetnumStatus.ASSIGNED_PA) && parentInHierarchyMaintainedByRs.equals(InetnumStatus.ALLOCATED_PA)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean needsOrgReference() {
        return NEEDS_ORG_REFERENCE.contains(getStatusFor(literalStatus));
    }

    @Override
    public String toString() {
        return literalStatus.toString();
    }
}
