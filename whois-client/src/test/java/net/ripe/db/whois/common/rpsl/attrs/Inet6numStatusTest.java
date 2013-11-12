package net.ripe.db.whois.common.rpsl.attrs;


import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import org.junit.Test;

import static net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus.*;
import static net.ripe.db.whois.common.rpsl.attrs.OrgType.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class Inet6numStatusTest {
    private final boolean HAS_RS_MAINTAINER = true;
    private final boolean HAS_NOT_RS_MAINTAINER = false;

    @Test
    public void requiresRsMaintainer() {
        assertThat(AGGREGATED_BY_LIR.requiresRsMaintainer(), is(false));
        assertThat(ALLOCATED_BY_LIR.requiresRsMaintainer(), is(false));
        assertThat(ALLOCATED_BY_RIR.requiresRsMaintainer(), is(true));
        assertThat(ASSIGNED.requiresRsMaintainer(), is(false));

        assertThat(ASSIGNED_ANYCAST.requiresRsMaintainer(), is(true));
        assertThat(ASSIGNED_PI.requiresRsMaintainer(), is(true));
    }

    @Test
    public void requiresAllocMaintainer() {
        assertThat(AGGREGATED_BY_LIR.requiresAllocMaintainer(), is(false));
        assertThat(ALLOCATED_BY_LIR.requiresAllocMaintainer(), is(false));

        assertThat(ALLOCATED_BY_RIR.requiresAllocMaintainer(), is(true));

        assertThat(ASSIGNED.requiresAllocMaintainer(), is(false));
        assertThat(ASSIGNED_ANYCAST.requiresAllocMaintainer(), is(false));
        assertThat(ASSIGNED_PI.requiresAllocMaintainer(), is(false));
    }

    @Test
    public void worksWithParentStatus_aggregated_by_lir() {
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(true));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(true));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(AGGREGATED_BY_LIR.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }

    @Test
    public void worksWithParentStatus_allocated_by_lir() {
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_LIR.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }

    @Test
    public void worksWithParentStatus_allocated_by_rir() {
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(ALLOCATED_BY_RIR.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }

    @Test
    public void worksWithParentStatus_assigned() {
        assertThat(ASSIGNED.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ASSIGNED.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ASSIGNED.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ASSIGNED.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ASSIGNED.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ASSIGNED.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }

    @Test
    public void worksWithParentStatus_assigned_anycast() {
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_ANYCAST.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }


    @Test
    public void worksWithParentStatus_assigned_pi() {
        assertThat(ASSIGNED_PI.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_PI.worksWithParentStatus(AGGREGATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_PI.worksWithParentStatus(ALLOCATED_BY_LIR, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_RS_MAINTAINER), is(true));
        assertThat(ASSIGNED_PI.worksWithParentStatus(ALLOCATED_BY_RIR, HAS_NOT_RS_MAINTAINER), is(true));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED_ANYCAST, HAS_NOT_RS_MAINTAINER), is(false));

        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED_PI, HAS_RS_MAINTAINER), is(false));
        assertThat(ASSIGNED_PI.worksWithParentStatus(ASSIGNED_PI, HAS_NOT_RS_MAINTAINER), is(false));
    }

    @Test
    public void needsOrgReference() {
        assertThat(AGGREGATED_BY_LIR.needsOrgReference(), is(false));
        assertThat(ALLOCATED_BY_LIR.needsOrgReference(), is(false));
        assertThat(ALLOCATED_BY_RIR.needsOrgReference(), is(true));
        assertThat(ASSIGNED.needsOrgReference(), is(false));

        assertThat(ASSIGNED_ANYCAST.needsOrgReference(), is(true));
        assertThat(ASSIGNED_PI.needsOrgReference(), is(true));
    }

    @Test
    public void getAllowedOrgTypes() {
        assertThat(AGGREGATED_BY_LIR.getAllowedOrgTypes(), containsInAnyOrder(LIR, OTHER));
        assertThat(ALLOCATED_BY_LIR.getAllowedOrgTypes(), containsInAnyOrder(LIR, OTHER));
        assertThat(ALLOCATED_BY_RIR.getAllowedOrgTypes(), containsInAnyOrder(IANA, RIR, LIR));
        assertThat(ASSIGNED.getAllowedOrgTypes(), containsInAnyOrder(LIR, OTHER));
        assertThat(ASSIGNED_ANYCAST.getAllowedOrgTypes(), containsInAnyOrder(LIR, OTHER));
        assertThat(ASSIGNED_PI.getAllowedOrgTypes(), containsInAnyOrder(LIR, OTHER));
    }

    @Test
    public void isValidOrgType() {
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(LIR), is(true));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(OTHER), is(true));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(NIR), is(false));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(WHITEPAGES), is(false));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(IANA), is(false));
        assertThat(AGGREGATED_BY_LIR.isValidOrgType(RIR), is(false));

        assertThat(ALLOCATED_BY_LIR.isValidOrgType(LIR), is(true));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(OTHER), is(true));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(NIR), is(false));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(WHITEPAGES), is(false));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(IANA), is(false));
        assertThat(ALLOCATED_BY_LIR.isValidOrgType(RIR), is(false));

        assertThat(ALLOCATED_BY_RIR.isValidOrgType(LIR), is(true));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(OTHER), is(false));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(NIR), is(false));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(WHITEPAGES), is(false));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(IANA), is(true));
        assertThat(ALLOCATED_BY_RIR.isValidOrgType(RIR), is(true));

        assertThat(ASSIGNED.isValidOrgType(LIR), is(true));
        assertThat(ASSIGNED.isValidOrgType(OTHER), is(true));
        assertThat(ASSIGNED.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(ASSIGNED.isValidOrgType(NIR), is(false));
        assertThat(ASSIGNED.isValidOrgType(WHITEPAGES), is(false));
        assertThat(ASSIGNED.isValidOrgType(IANA), is(false));
        assertThat(ASSIGNED.isValidOrgType(RIR), is(false));

        assertThat(ASSIGNED_ANYCAST.isValidOrgType(LIR), is(true));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(OTHER), is(true));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(NIR), is(false));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(WHITEPAGES), is(false));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(IANA), is(false));
        assertThat(ASSIGNED_ANYCAST.isValidOrgType(RIR), is(false));

        assertThat(ASSIGNED_PI.isValidOrgType(LIR), is(true));
        assertThat(ASSIGNED_PI.isValidOrgType(OTHER), is(true));
        assertThat(ASSIGNED_PI.isValidOrgType(DIRECT_ASSIGNMENT), is(false));
        assertThat(ASSIGNED_PI.isValidOrgType(NIR), is(false));
        assertThat(ASSIGNED_PI.isValidOrgType(WHITEPAGES), is(false));
        assertThat(ASSIGNED_PI.isValidOrgType(IANA), is(false));
        assertThat(ASSIGNED_PI.isValidOrgType(RIR), is(false));
    }

    @Test
    public void getLiteralStatus() {
        assertThat(AGGREGATED_BY_LIR.getLiteralStatus(), is(CIString.ciString("AGGREGATED-BY-LIR")));
        assertThat(ALLOCATED_BY_LIR.getLiteralStatus(), is(CIString.ciString("ALLOCATED-BY-LIR")));
        assertThat(ALLOCATED_BY_RIR.getLiteralStatus(), is(CIString.ciString("ALLOCATED-BY-RIR")));
        assertThat(ASSIGNED.getLiteralStatus(), is(CIString.ciString("ASSIGNED")));
        assertThat(ASSIGNED_ANYCAST.getLiteralStatus(), is(CIString.ciString("ASSIGNED ANYCAST")));
        assertThat(ASSIGNED_PI.getLiteralStatus(), is(CIString.ciString("ASSIGNED PI")));
    }

    @Test
    public void getStatusFor() {
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("AGGREGATED-BY-LIR")), is(AGGREGATED_BY_LIR));
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("ALLOCATED-BY-LIR")), is(ALLOCATED_BY_LIR));
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("ALLOCATED-BY-RIR")), is(ALLOCATED_BY_RIR));
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("ASSIGNED")), is(ASSIGNED));
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("ASSIGNED ANYCAST")), is(ASSIGNED_ANYCAST));
        assertThat(Inet6numStatus.getStatusFor(CIString.ciString("ASSIGNED PI")), is(ASSIGNED_PI));

        try {
            Inet6numStatus.getStatusFor(CIString.ciString("AGGREGATED-BY-RIR"));
            fail();
        } catch (Exception expected) {}
    }
}
