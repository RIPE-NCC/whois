package net.ripe.db.whois.common.rpsl.attrs;

import java.util.Set;

public interface InetStatus {
    boolean needsOrgReference();

    Set<OrgType> getAllowedOrgTypes();

    boolean isValidOrgType(OrgType orgType);

    boolean requiresRsMaintainer();

    boolean requiresAllocMaintainer();

    boolean worksWithParentStatus(InetStatus parent, boolean objectHasRsMaintainer);

    boolean worksWithParentInHierarchy(InetStatus parentInHierarchyMaintainedByRs, final boolean parentHasRsMntLower);
}
