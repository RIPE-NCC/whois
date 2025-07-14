package net.ripe.db.whois.common.rpsl.attrs;


import org.junit.jupiter.api.Test;

import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ALLOCATED_PA;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ALLOCATED_UNSPECIFIED;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ASSIGNED_ANYCAST;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ASSIGNED_PA;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.ASSIGNED_PI;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.LEGACY;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.LIR_PARTITIONED_PA;
import static net.ripe.db.whois.common.rpsl.attrs.InetnumStatus.SUB_ALLOCATED_PA;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InetnumStatusTest {

    @Test
    public void parentVerification() {
        assertThat(ASSIGNED_PA.worksWithParentStatus(SUB_ALLOCATED_PA, true), is(true));
        assertThat(ASSIGNED_PA.worksWithParentStatus(LIR_PARTITIONED_PA, true), is(true));

        assertThat(SUB_ALLOCATED_PA.worksWithParentStatus(ALLOCATED_PA, true), is(true));
        assertThat(SUB_ALLOCATED_PA.worksWithParentStatus(LIR_PARTITIONED_PA, true), is(true));
        assertThat(SUB_ALLOCATED_PA.worksWithParentStatus(SUB_ALLOCATED_PA, true), is(true));

        assertThat(LIR_PARTITIONED_PA.worksWithParentStatus(ALLOCATED_PA, true), is(true));
        assertThat(LIR_PARTITIONED_PA.worksWithParentStatus(LIR_PARTITIONED_PA, true), is(true));
        assertThat(LIR_PARTITIONED_PA.worksWithParentStatus(SUB_ALLOCATED_PA, true), is(false));

        assertThat(ALLOCATED_UNSPECIFIED.worksWithParentStatus(ALLOCATED_UNSPECIFIED, true), is(true));
        assertThat(ALLOCATED_UNSPECIFIED.worksWithParentStatus(LEGACY, true), is(false));
        assertThat(ALLOCATED_PA.worksWithParentStatus(ALLOCATED_UNSPECIFIED, true), is(true));
        assertThat(ALLOCATED_PA.worksWithParentStatus(LIR_PARTITIONED_PA, true), is(false));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ALLOCATED_UNSPECIFIED, true), is(true));
        assertThat(ASSIGNED_PI.worksWithParentStatus(LIR_PARTITIONED_PA, false), is(false));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ALLOCATED_UNSPECIFIED, false), is(true));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(SUB_ALLOCATED_PA, false), is(false));

        assertThat(LEGACY.worksWithParentStatus(ALLOCATED_UNSPECIFIED, false), is(true));
        assertThat(LEGACY.worksWithParentStatus(ALLOCATED_UNSPECIFIED, true), is(true));
        assertThat(LEGACY.worksWithParentStatus(LEGACY, false), is(true));
        assertThat(LEGACY.worksWithParentStatus(LEGACY, true), is(true));
        assertThat(LEGACY.worksWithParentStatus(LIR_PARTITIONED_PA, true), is(false));

    }

    @Test
    public void allowedOrgTypesChecks() {
        assertThat(ALLOCATED_PA.isValidOrgType(OrgType.LIR), is(true));
        assertThat(ALLOCATED_PA.isValidOrgType(OrgType.RIR), is(true));
        assertThat(ALLOCATED_PA.isValidOrgType(OrgType.IANA), is(false));
        assertThat(ALLOCATED_PA.isValidOrgType(OrgType.DIRECT_ASSIGNMENT), is(false));

        assertThat(ALLOCATED_UNSPECIFIED.isValidOrgType(OrgType.LIR), is(true));
        assertThat(ALLOCATED_UNSPECIFIED.isValidOrgType(OrgType.RIR), is(true));
        assertThat(ALLOCATED_UNSPECIFIED.isValidOrgType(OrgType.IANA), is(true));
        assertThat(ALLOCATED_UNSPECIFIED.isValidOrgType(OrgType.OTHER), is(false));

        assertThat(LIR_PARTITIONED_PA.isValidOrgType(OrgType.OTHER), is(true));
        assertThat(LIR_PARTITIONED_PA.isValidOrgType(OrgType.LIR), is(true));
        assertThat(LIR_PARTITIONED_PA.isValidOrgType(OrgType.NIR), is(false));

        assertThat(SUB_ALLOCATED_PA.isValidOrgType(OrgType.OTHER), is(true));
        assertThat(SUB_ALLOCATED_PA.isValidOrgType(OrgType.LIR), is(true));

        assertThat(ASSIGNED_PA.isValidOrgType(OrgType.OTHER), is(true));
        assertThat(ASSIGNED_PA.isValidOrgType(OrgType.LIR), is(true));

        assertThat(ASSIGNED_ANYCAST.isValidOrgType(OrgType.OTHER), is(true));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(OrgType.LIR), is(true));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(OrgType.IANA), is(false));

        assertThat(ASSIGNED_PI.isValidOrgType(OrgType.RIR), is(true));
        assertThat(ASSIGNED_PI.isValidOrgType(OrgType.LIR), is(true));
        assertThat(ASSIGNED_PI.isValidOrgType(OrgType.OTHER), is(true));
        assertThat(ASSIGNED_PI.isValidOrgType(OrgType.NIR), is(false));
        assertThat(ASSIGNED_PI.isValidOrgType(OrgType.DIRECT_ASSIGNMENT), is(false));
    }

    @Test
    public void needsEndMaintainerAuthorisation() {
        assertThat(InetnumStatus.ASSIGNED_ANYCAST.requiresRsMaintainer(), is(true));
        assertThat(InetnumStatus.ASSIGNED_PI.requiresRsMaintainer(), is(false));
        assertThat(InetnumStatus.SUB_ALLOCATED_PA.requiresRsMaintainer(), is(false));
    }

    @Test
    public void needsAllocMaintainerAuthorization() {
        for (final InetnumStatus inetnumStatus : InetnumStatus.values()) {
            assertThat(inetnumStatus.requiresAllocMaintainer(), is(false));
        }
    }

    @Test
    public void needsOrgReference() {
        assertThat(ALLOCATED_PA.needsOrgReference(), is(true));
        assertThat(ALLOCATED_UNSPECIFIED.needsOrgReference(), is(true));
        assertThat(LIR_PARTITIONED_PA.needsOrgReference(), is(false));
        assertThat(SUB_ALLOCATED_PA.needsOrgReference(), is(false));
        assertThat(ASSIGNED_PA.needsOrgReference(), is(false));
        assertThat(ASSIGNED_PI.needsOrgReference(), is(false));
        assertThat(ASSIGNED_ANYCAST.needsOrgReference(), is(false));
        assertThat(LEGACY.needsOrgReference(), is(false));
    }

}
