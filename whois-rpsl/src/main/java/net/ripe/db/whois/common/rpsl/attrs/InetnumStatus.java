package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.IANA;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.LIR;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.OTHER;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.RIR;

public enum InetnumStatus implements InetStatus {

    ALLOCATED_PA("ALLOCATED PA", RIR, LIR),
    ALLOCATED_UNSPECIFIED("ALLOCATED UNSPECIFIED", IANA, RIR, LIR),
    LIR_PARTITIONED_PA("LIR-PARTITIONED PA", LIR, OTHER),
    SUB_ALLOCATED_PA("SUB-ALLOCATED PA", LIR, OTHER),
    AGGREGATED_BY_LIR("AGGREGATED-BY-LIR", LIR, OTHER),

    ALLOCATED_ASSIGNED_PA("ALLOCATED-ASSIGNED PA", RIR, LIR),
    ASSIGNED_PA("ASSIGNED PA", LIR, OTHER),
    ASSIGNED_PI("ASSIGNED PI", LIR, OTHER, RIR),
    ASSIGNED_ANYCAST("ASSIGNED ANYCAST", LIR, OTHER),
    LEGACY("LEGACY", LIR, OTHER);

    private static final EnumSet<InetnumStatus> RS_MNTNER_STATUSES = EnumSet.of(ALLOCATED_PA, ALLOCATED_ASSIGNED_PA, ASSIGNED_ANYCAST, ALLOCATED_UNSPECIFIED);
    private static final EnumSet<InetnumStatus> NEEDS_ORG_REFERENCE = EnumSet.of(ALLOCATED_PA, ALLOCATED_ASSIGNED_PA, ALLOCATED_UNSPECIFIED);
    private static final EnumSet<InetnumStatus> NEEDS_PARENT_RS_MNTR = EnumSet.of(ALLOCATED_UNSPECIFIED);

    private static final EnumMap<InetnumStatus, EnumSet<InetnumStatus>> PARENT_STATUS;

    static {
        PARENT_STATUS = Maps.newEnumMap(InetnumStatus.class);
        PARENT_STATUS.put(ALLOCATED_PA, EnumSet.of(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(ALLOCATED_ASSIGNED_PA, EnumSet.of(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(AGGREGATED_BY_LIR, EnumSet.of(ALLOCATED_PA, SUB_ALLOCATED_PA, LIR_PARTITIONED_PA, AGGREGATED_BY_LIR));

        PARENT_STATUS.put(ALLOCATED_UNSPECIFIED, EnumSet.of(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(LIR_PARTITIONED_PA, EnumSet.of(ALLOCATED_UNSPECIFIED, ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA));
        PARENT_STATUS.put(SUB_ALLOCATED_PA, EnumSet.of(ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA));
        PARENT_STATUS.put(ASSIGNED_PA, EnumSet.of(ALLOCATED_PA, LIR_PARTITIONED_PA, SUB_ALLOCATED_PA, AGGREGATED_BY_LIR));
        PARENT_STATUS.put(ASSIGNED_ANYCAST, EnumSet.of(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(ASSIGNED_PI, EnumSet.of(ALLOCATED_UNSPECIFIED));
        PARENT_STATUS.put(LEGACY, EnumSet.of(ALLOCATED_UNSPECIFIED, LEGACY));
    }

    private final CIString literalStatus;
    private final Set<OrgType> allowedOrgTypes;

    private InetnumStatus(final String literalStatus, final OrgType... orgType) {
        this.literalStatus = ciString(literalStatus);
        allowedOrgTypes = Sets.immutableEnumSet(Arrays.asList(orgType));
    }

    public static InetnumStatus getStatusFor(@Nullable final CIString status) {
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
        if (this == ASSIGNED_PI && objectHasRsMaintainer) {
            return PARENT_STATUS.get(this).contains(parent) && NEEDS_PARENT_RS_MNTR.contains(parent);
        }

        return PARENT_STATUS.get(this).contains(parent);
    }

    @Override
    public boolean worksWithParentInHierarchy(final InetStatus parentInHierarchyMaintainedByRs, final boolean parentHasRsMntLower) {
        if (this == ASSIGNED_PA && parentInHierarchyMaintainedByRs == ALLOCATED_PA) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isOutOfRegionOrRoot() {
        return this.equals(ALLOCATED_UNSPECIFIED);
    }

    @Override
    public boolean needsOrgReference() {
        return NEEDS_ORG_REFERENCE.contains(this);
    }

    @Override
    public String toString() {
        return literalStatus.toString();
    }
}
